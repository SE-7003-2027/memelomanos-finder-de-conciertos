package com.memelomanos.finderconciertos.integracion.ticketmaster;

import com.memelomanos.finderconciertos.integracion.ConciertoExterno;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TicketmasterProveedorTest {

    @Test
    void convierteElEventoAlModeloDelProyecto() {
        var builder = RestClient.builder();
        var servidor = MockRestServiceServer.bindTo(builder).build();
        var proveedor = new TicketmasterProveedor(new TicketmasterCliente(builder, "clave-de-prueba"));
        servidor.expect(queryParam("apikey", "clave-de-prueba"))
                .andRespond(withSuccess(new ClassPathResource("ticketmaster/eventos.json"),
                        MediaType.APPLICATION_JSON));

        var resultado = proveedor.buscar("Café Tacvba", "Ciudad de México", null, null);

        assertThat(resultado).containsExactly(new ConciertoExterno(
                "TICKETMASTER", "evento-mx-prueba-001", "Café Tacvba",
                LocalDate.of(2027, 3, 20), "Ciudad de México", "Auditorio Nacional",
                "https://example.com/boletos/cafe-tacvba", "https://example.com/concierto-grande.jpg",
                new BigDecimal("19.4241"), new BigDecimal("-99.1949"), new BigDecimal("850.0")
        ));
        servidor.verify();
    }

    @Test
    void conservaElEventoCuandoTicketmasterNoEnviaDatosOpcionales() {
        var builder = RestClient.builder();
        var servidor = MockRestServiceServer.bindTo(builder).build();
        var proveedor = new TicketmasterProveedor(new TicketmasterCliente(builder, "clave-de-prueba"));
        servidor.expect(queryParam("apikey", "clave-de-prueba"))
                .andRespond(withSuccess("""
                        {
                          "_embedded": {
                            "events": [{ "id": "evento-1", "name": "Artista de prueba" }]
                          },
                          "page": { "number": 0, "totalPages": 1 }
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThat(proveedor.buscar(null, null, null, null)).containsExactly(new ConciertoExterno(
                "TICKETMASTER", "evento-1", "Artista de prueba", null,
                null, null, null, null, null, null, null
        ));
        servidor.verify();
    }

    @ParameterizedTest
    @CsvSource({"true,''", "true,'   '", "false,clave-de-prueba"})
    void noConstruyeElClienteCuandoEstaDeshabilitado(boolean habilitado, String apiKey) {
        var builder = mock(RestClient.Builder.class);

        new ApplicationContextRunner()
                .withUserConfiguration(TicketmasterProveedor.class)
                .withBean(RestClient.Builder.class, () -> builder)
                .withPropertyValues(
                        "proveedores.ticketmaster.habilitado=" + habilitado,
                        "proveedores.ticketmaster.api-key=" + apiKey
                )
                .run(contexto -> {
                    assertThat(contexto).hasNotFailed();
                    List<ConciertoExterno> resultado =
                            contexto.getBean(TicketmasterProveedor.class).buscar(null, null, null, null);
                    assertThat(resultado).isEmpty();
                });

        verifyNoInteractions(builder);
    }
}
