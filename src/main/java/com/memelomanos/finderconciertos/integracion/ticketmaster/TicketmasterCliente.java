package com.memelomanos.finderconciertos.integracion.ticketmaster;

import com.memelomanos.finderconciertos.integracion.ticketmaster.dto.RespuestaTicketmaster;
import org.springframework.web.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

final class TicketmasterCliente {

    private static final String URL_BASE = "https://app.ticketmaster.com";
    private static final int TAMANO_PAGINA = 20;
    private static final Logger LOG = LoggerFactory.getLogger(TicketmasterCliente.class);
    private static final int LIMITE_PAGINACION = 1000;

    private final RestClient cliente;
    private final String apiKey;

    TicketmasterCliente(RestClient.Builder builder, String apiKey) {
        this.cliente = builder.baseUrl(URL_BASE).build();
        this.apiKey = apiKey;
    }

    List<RespuestaTicketmaster.Evento> buscarEventos(
        String artista, String ciudad, LocalDate fechaDesde, LocalDate fechaHasta
    ) {
    List<RespuestaTicketmaster.Evento> eventos = new ArrayList<>();

    try {
        for (int pagina = 0; pagina * TAMANO_PAGINA < LIMITE_PAGINACION; pagina++) {
            var respuesta = buscarPagina(artista, ciudad, fechaDesde, fechaHasta, pagina);

            if (respuesta == null || respuesta.contenido() == null
                    || respuesta.contenido().events() == null
                    || respuesta.contenido().events().isEmpty()) {
                break;
            }

            eventos.addAll(respuesta.contenido().events());

            if (respuesta.pagina() == null || pagina + 1 >= respuesta.pagina().totalPages()) {
                break;
            }
        }
    } catch (RestClientResponseException ex) {
        LOG.warn("Ticketmaster rechazó la consulta con HTTP {}", ex.getStatusCode().value());
        return List.of();
    } catch (ResourceAccessException ex) {
        LOG.warn("No se pudo completar la conexión con Ticketmaster");
        return List.of();
    }

        return eventos;
    }
       

    RespuestaTicketmaster buscarPagina(
            String artista, String ciudad, LocalDate fechaDesde, LocalDate fechaHasta, int pagina
    ) {
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
                        valores.put("desde", fechaDesde.atStartOfDay().toInstant(ZoneOffset.UTC).toString());
                    }
                    if (fechaHasta != null) {
                        uri.queryParam("endDateTime", "{hasta}");
                        valores.put("hasta", fechaHasta.atTime(23, 59, 59).toInstant(ZoneOffset.UTC).toString());
                    }

                    return uri.build(valores);
                })
                .retrieve()
                .body(RespuestaTicketmaster.class);
    }
}