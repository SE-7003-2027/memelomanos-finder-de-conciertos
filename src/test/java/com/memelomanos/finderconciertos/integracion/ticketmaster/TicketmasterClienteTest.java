package com.memelomanos.finderconciertos.integracion.ticketmaster;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.net.SocketTimeoutException;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

class TicketmasterClienteTest {

    @Test
    void enviaLosFiltrosYLeeLaRespuesta() throws InterruptedException {
        var builder = RestClient.builder();
        var servidor = MockRestServiceServer.bindTo(builder).build();
        var cliente = new TicketmasterCliente(builder, "clave-de-prueba");

        servidor.expect(requestTo(
                        "https://app.ticketmaster.com/discovery/v2/events.json"
                                + "?apikey=clave-de-prueba&classificationName=music&countryCode=MX"
                                + "&size=20&page=0&keyword=Caf%C3%A9%20Tacvba"
                                + "&city=Ciudad%20de%20M%C3%A9xico"
                                + "&startDateTime=2027-03-01T06%3A00%3A00Z"
                                + "&endDateTime=2027-04-01T05%3A59%3A59Z"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        new ClassPathResource("ticketmaster/eventos.json"),
                        MediaType.APPLICATION_JSON));

        var respuesta = cliente.buscarPagina(
                "Café Tacvba", "Ciudad de México",
                LocalDate.of(2027, 3, 1), LocalDate.of(2027, 3, 31), 0);

        assertEquals("evento-mx-prueba-001", respuesta.contenido().events().getFirst().id());
        servidor.verify();
    }
    @Test
    void juntaLosEventosDeDosPaginas() {
        var builder = RestClient.builder();
        var servidor = MockRestServiceServer.bindTo(builder).build();
        var cliente = new TicketmasterCliente(builder, "clave-de-prueba");

        servidor.expect(requestTo(urlPagina(0)))
            .andRespond(withSuccess(respuestaPagina("evento-1", 0), MediaType.APPLICATION_JSON));
        servidor.expect(requestTo(urlPagina(1)))
            .andRespond(withSuccess(respuestaPagina("evento-2", 1), MediaType.APPLICATION_JSON));

        var eventos = cliente.buscarEventos(null, null, null, null);

        assertEquals(2, eventos.size());
        assertEquals("evento-1", eventos.getFirst().id());
        assertEquals("evento-2", eventos.getLast().id());
        servidor.verify();
    }

    @ParameterizedTest
    @ValueSource(ints = {401, 429})
    void devuelveListaVaciaSiTicketmasterRechazaLaConsulta(int estado) {
        var builder = RestClient.builder();
        var servidor = MockRestServiceServer.bindTo(builder).build();
        var cliente = new TicketmasterCliente(builder, "clave-de-prueba");

        servidor.expect(requestTo(urlPagina(0)))
            .andRespond(withStatus(HttpStatus.valueOf(estado)));

        assertTrue(cliente.buscarEventos(null, null, null, null).isEmpty());
        servidor.verify();
    }

    @Test
    void devuelveListaVaciaSiLaConexionAgotaElTiempo() {
        var builder = RestClient.builder();
        var servidor = MockRestServiceServer.bindTo(builder).build();
        var cliente = new TicketmasterCliente(builder, "clave-de-prueba");

        servidor.expect(requestTo(urlPagina(0))).andRespond(peticion -> {
        throw new SocketTimeoutException("Tiempo de espera simulado");
    });

        assertTrue(cliente.buscarEventos(null, null, null, null).isEmpty());
        servidor.verify();
    }

    @ParameterizedTest
    @ValueSource(ints = {401, 429})
    void conservaLaPrimeraPaginaSiLaSiguienteFalla(int estado) {
        var builder = RestClient.builder();
        var servidor = MockRestServiceServer.bindTo(builder).build();
        var cliente = new TicketmasterCliente(builder, "clave-de-prueba");

        servidor.expect(requestTo(urlPagina(0)))
                .andRespond(withSuccess(respuestaPagina("evento-1", 0), MediaType.APPLICATION_JSON));
        servidor.expect(requestTo(urlPagina(1)))
                .andRespond(withStatus(HttpStatus.valueOf(estado)));

        var eventos = cliente.buscarEventos(null, null, null, null);

        assertEquals(1, eventos.size());
        assertEquals("evento-1", eventos.getFirst().id());
        servidor.verify();
    }

    @Test
    void conservaLaPrimeraPaginaSiLaSiguienteAgotaElTiempo() {
        var builder = RestClient.builder();
        var servidor = MockRestServiceServer.bindTo(builder).build();
        var cliente = new TicketmasterCliente(builder, "clave-de-prueba");

        servidor.expect(requestTo(urlPagina(0)))
                .andRespond(withSuccess(respuestaPagina("evento-1", 0), MediaType.APPLICATION_JSON));
        servidor.expect(requestTo(urlPagina(1))).andRespond(peticion -> {
            throw new SocketTimeoutException("Tiempo de espera simulado");
        });

        var eventos = cliente.buscarEventos(null, null, null, null);

        assertEquals(1, eventos.size());
        assertEquals("evento-1", eventos.getFirst().id());
        servidor.verify();
    }

    @Test
    void respetaLaInterrupcionAlEsperarLaSiguientePagina() {
        var builder = RestClient.builder();
        var servidor = MockRestServiceServer.bindTo(builder).build();
        var cliente = new TicketmasterCliente(builder, "clave-de-prueba");

        servidor.expect(requestTo(urlPagina(0))).andRespond(peticion -> {
            Thread.currentThread().interrupt();
            return withSuccess(respuestaPagina("evento-1", 0), MediaType.APPLICATION_JSON)
                    .createResponse(peticion);
        });

        try {
            var eventos = cliente.buscarEventos(null, null, null, null);

            assertTrue(Thread.currentThread().isInterrupted());
            assertEquals(1, eventos.size());
            assertEquals("evento-1", eventos.getFirst().id());
            servidor.verify();
        } finally {
            Thread.interrupted();
        }
    }

    private static String urlPagina(int pagina) {
        return "https://app.ticketmaster.com/discovery/v2/events.json"
            + "?apikey=clave-de-prueba&classificationName=music&countryCode=MX"
            + "&size=20&page=" + pagina;
    }

    private static String respuestaPagina(String id, int pagina) {
        return """
            {
              "_embedded": { "events": [{ "id": "%s" }] },
              "page": { "number": %d, "totalPages": 2 }
            }
            """.formatted(id, pagina);
    }

}