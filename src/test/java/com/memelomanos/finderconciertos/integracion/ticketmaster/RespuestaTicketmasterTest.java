package com.memelomanos.finderconciertos.integracion.ticketmaster;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.memelomanos.finderconciertos.integracion.ticketmaster.dto.RespuestaTicketmaster;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RespuestaTicketmasterTest {

    @Test
    void leeEventoYDatosDelRecinto() throws IOException {
        var jackson = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
        var archivo = new ClassPathResource("ticketmaster/eventos.json");

        try (var contenido = archivo.getInputStream()) {
            var respuesta = jackson.readValue(contenido, RespuestaTicketmaster.class);

            assertEquals(1, respuesta.contenido().events().size());
            var evento = respuesta.contenido().events().getFirst();
            var recinto = evento.recintos().venues().getFirst();

            assertEquals("evento-mx-prueba-001", evento.id());
            assertEquals("Café Tacvba", evento.name());
            assertEquals(LocalDate.of(2027, 3, 20), evento.dates().start().localDate());
            assertEquals("Auditorio Nacional", recinto.name());
            assertEquals("Ciudad de México", recinto.city().name());
            assertEquals(new BigDecimal("19.4241"), recinto.location().latitude());
            assertEquals(new BigDecimal("-99.1949"), recinto.location().longitude());
            assertEquals(3, evento.images().size());
            assertEquals("https://example.com/concierto-grande.jpg", evento.urlImagenPrincipal());
            assertEquals(new BigDecimal("850.0"), evento.priceRanges().getFirst().min());
            assertEquals(1, respuesta.pagina().totalPages());
        }
    }
}