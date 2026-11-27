package com.memelomanos.finderconciertos.integracion.ticketmaster;

import com.memelomanos.finderconciertos.integracion.ticketmaster.dto.RespuestaTicketmaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class TicketmasterCliente {

    private static final String URL_BASE = "https://app.ticketmaster.com";
    private static final int TAMANO_PAGINA = 20;
    private static final int LIMITE_PAGINACION = 1000;
    private static final ZoneId ZONA_HORARIA = ZoneId.of("America/Mexico_City");
    private static final Duration INTERVALO_PETICIONES = Duration.ofMillis(250);
    private static final Logger LOG = LoggerFactory.getLogger(TicketmasterCliente.class);

    private final RestClient cliente;
    private final String apiKey;
    private long siguientePeticionNanos = System.nanoTime();

    TicketmasterCliente(RestClient.Builder builder, String apiKey) {
        this.cliente = builder.baseUrl(URL_BASE).build();
        this.apiKey = apiKey;
    }

    List<RespuestaTicketmaster.Evento> buscarEventos(
            String artista, String ciudad, LocalDate fechaDesde, LocalDate fechaHasta
    ) {
        return descargar(artista, ciudad, fechaDesde, fechaHasta).eventos();
    }

    record Descarga(List<RespuestaTicketmaster.Evento> eventos, boolean completa) {}

    Descarga descargar(String artista, String ciudad, LocalDate fechaDesde, LocalDate fechaHasta) {
        List<RespuestaTicketmaster.Evento> eventos = new ArrayList<>();
        RespuestaTicketmaster.Pagina primera = null;
        boolean paginacionCoherente = true;

        try {
            for (int pagina = 0; pagina * TAMANO_PAGINA < LIMITE_PAGINACION; pagina++) {
                var respuesta = buscarPagina(artista, ciudad, fechaDesde, fechaHasta, pagina);

                if (respuesta == null) {
                    break;
                }

                var datos = respuesta.contenido() == null ? null : respuesta.contenido().events();
                if (datos != null) {
                    eventos.addAll(datos);
                }

                var meta = respuesta.pagina();
                if (meta == null || meta.totalPages() == null) {
                    break;
                }
                if (pagina == 0) {
                    primera = meta;
                }
                paginacionCoherente &= Objects.equals(meta.number(), pagina)
                        && Objects.equals(meta.totalPages(), primera.totalPages())
                        && Objects.equals(meta.totalElements(), primera.totalElements());

                if (pagina + 1 == meta.totalPages() || (pagina == 0 && meta.totalPages() == 0)) {
                    return new Descarga(eventos, paginacionCoherente
                            && Objects.equals(meta.totalElements(), eventos.size())
                            && eventos.stream().filter(Objects::nonNull)
                                    .map(RespuestaTicketmaster.Evento::id)
                                    .filter(id -> id != null && !id.isBlank())
                                    .distinct().count() == eventos.size()
                            && ((datos != null && !datos.isEmpty())
                                    || (meta.totalPages() == 0 && eventos.isEmpty())));
                }
                if (datos == null || datos.isEmpty() || pagina + 1 > meta.totalPages()) {
                    break;
                }
            }
        } catch (RestClientResponseException ex) {
            LOG.warn("Ticketmaster rechazó la consulta con HTTP {}. Se conservan {} eventos",
                    ex.getStatusCode().value(), eventos.size());
        } catch (ResourceAccessException ex) {
            LOG.warn("No se pudo completar la conexión con Ticketmaster. Se conservan {} eventos",
                    eventos.size());
        } catch (RestClientException ex) {
            LOG.warn("No se pudo leer una página de Ticketmaster. Se conservan {} eventos", eventos.size());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            LOG.warn("La consulta a Ticketmaster fue interrumpida. Se conservan {} eventos",
                    eventos.size());
        }

        return new Descarga(eventos, false);
    }

    synchronized RespuestaTicketmaster buscarPagina(
            String artista, String ciudad, LocalDate fechaDesde, LocalDate fechaHasta, int pagina
    ) throws InterruptedException {
        // Comparte el intervalo entre búsquedas manuales y programadas del mismo cliente.
        long esperaNanos = Math.max(0, siguientePeticionNanos - System.nanoTime());
        Thread.sleep(Duration.ofNanos(esperaNanos));
        siguientePeticionNanos = System.nanoTime() + INTERVALO_PETICIONES.toNanos();

        return cliente.get()
                .uri(uri -> {
                    Map<String, String> valores = new HashMap<>();
                    valores.put("apiKey", apiKey);

                    uri.path("/discovery/v2/events.json")
                            .queryParam("apikey", "{apiKey}")
                            .queryParam("classificationName", "music")
                            .queryParam("countryCode", "MX")
                            .queryParam("size", TAMANO_PAGINA)
                            .queryParam("page", pagina);

                    if (artista != null && !artista.isBlank()) {
                        uri.queryParam("keyword", "{artista}");
                        valores.put("artista", artista.strip());
                    }
                    if (ciudad != null && !ciudad.isBlank()) {
                        uri.queryParam("city", "{ciudad}");
                        valores.put("ciudad", ciudad.strip());
                    }
                    if (fechaDesde != null) {
                        uri.queryParam("startDateTime", "{desde}");
                        valores.put("desde", fechaDesde.atStartOfDay(ZONA_HORARIA).toInstant().toString());
                    }
                    if (fechaHasta != null) {
                        uri.queryParam("endDateTime", "{hasta}");
                        valores.put("hasta", fechaHasta.atTime(LocalTime.MAX.withNano(0))
                                .atZone(ZONA_HORARIA).toInstant().toString());
                    }

                    return uri.build(valores);
                })
                .retrieve()
                .body(RespuestaTicketmaster.class);
    }
}
