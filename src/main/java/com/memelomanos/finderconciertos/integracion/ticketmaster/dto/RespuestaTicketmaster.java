package com.memelomanos.finderconciertos.integracion.ticketmaster.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Comparator;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RespuestaTicketmaster(
        @JsonProperty("_embedded") Eventos contenido,
        @JsonProperty("page") Pagina pagina
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Eventos(List<Evento> events) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Evento(
            String id,
            String name,
            String url,
            Fechas dates,
            @JsonProperty("_embedded") Recintos recintos,
            List<Imagen> images,
            List<RangoPrecio> priceRanges
    ) {
        public String urlImagenPrincipal() {
            if (images == null) {
                return null;
            }

            return images.stream()
                    .filter(imagen -> "16_9".equals(imagen.ratio()))
                    .filter(imagen -> imagen.url() != null && !imagen.url().isBlank())
                    .max(Comparator.comparingLong(imagen -> (long) imagen.width() * imagen.height()))
                    .map(Imagen::url)
                    .orElse(null);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Fechas(Inicio start) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Inicio(LocalDate localDate) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Recintos(List<Recinto> venues) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Recinto(String name, Ciudad city, Ubicacion location) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Ciudad(String name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Ubicacion(BigDecimal latitude, BigDecimal longitude) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Imagen(String ratio, String url, int width, int height) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RangoPrecio(BigDecimal min) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Pagina(int number, int totalPages) {}
}
