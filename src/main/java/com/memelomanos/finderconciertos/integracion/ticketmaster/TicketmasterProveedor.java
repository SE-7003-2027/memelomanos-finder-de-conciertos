package com.memelomanos.finderconciertos.integracion.ticketmaster;

import com.memelomanos.finderconciertos.integracion.ConciertoExterno;
import com.memelomanos.finderconciertos.integracion.ticketmaster.dto.RespuestaTicketmaster.Evento;
import com.memelomanos.finderconciertos.integracion.ticketmaster.dto.RespuestaTicketmaster.Fechas;
import com.memelomanos.finderconciertos.integracion.ticketmaster.dto.RespuestaTicketmaster.Inicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class TicketmasterProveedor {

    private static final String FUENTE = "TICKETMASTER";
    private static final Duration TIEMPO_CONEXION = Duration.ofSeconds(3);
    private static final Duration TIEMPO_LECTURA = Duration.ofSeconds(5);

    private final TicketmasterCliente cliente;

    @Autowired
    public TicketmasterProveedor(
            RestClient.Builder builder,
            @Value("${proveedores.ticketmaster.habilitado:true}") boolean habilitado,
            @Value("${proveedores.ticketmaster.api-key:}") String apiKey
    ) {
        this(habilitado && apiKey != null && !apiKey.isBlank()
                ? crearCliente(builder, apiKey.strip())
                : null);
    }

    TicketmasterProveedor(TicketmasterCliente cliente) {
        this.cliente = cliente;
    }

    public List<ConciertoExterno> buscar(
            String artista, String ciudad, LocalDate fechaDesde, LocalDate fechaHasta
    ) {
        if (cliente == null) {
            return List.of();
        }

        return cliente.buscarEventos(artista, ciudad, fechaDesde, fechaHasta).stream()
                .filter(Objects::nonNull)
                .map(this::convertir)
                .toList();
    }

    private static TicketmasterCliente crearCliente(RestClient.Builder builder, String apiKey) {
        var peticiones = new SimpleClientHttpRequestFactory();
        peticiones.setConnectTimeout(TIEMPO_CONEXION);
        peticiones.setReadTimeout(TIEMPO_LECTURA);
        return new TicketmasterCliente(builder.clone().requestFactory(peticiones), apiKey);
    }

    private ConciertoExterno convertir(Evento evento) {
        var fecha = Optional.ofNullable(evento.dates())
                .map(Fechas::start)
                .map(Inicio::localDate)
                .orElse(null);
        var recinto = evento.recintos() == null ? null : primero(evento.recintos().venues());
        var ciudad = recinto == null ? null : recinto.city();
        var ubicacion = recinto == null ? null : recinto.location();
        var precio = primero(evento.priceRanges());

        return new ConciertoExterno(
                FUENTE,
                evento.id(),
                evento.name(),
                fecha,
                ciudad == null ? null : ciudad.name(),
                recinto == null ? null : recinto.name(),
                evento.url(),
                evento.urlImagenPrincipal(),
                ubicacion == null ? null : ubicacion.latitude(),
                ubicacion == null ? null : ubicacion.longitude(),
                precio == null ? null : precio.min()
        );
    }

    private static <T> T primero(List<T> elementos) {
        return elementos == null || elementos.isEmpty() ? null : elementos.getFirst();
    }
}
