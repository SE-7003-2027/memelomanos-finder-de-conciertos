package com.memelomanos.finderconciertos.model;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

public class ConciertoRequest {

    @NotBlank
    private String artista;

    @NotNull
    private LocalDate fecha;

    @NotBlank
    private String ciudad;

    @NotBlank
    private String lugar;

    // Opcional. Perfil del artista en SoundCloud, por ejemplo
    // https://soundcloud.com/nombre-del-artista (ADR-005).
    @Pattern(regexp = "https://(www\\.|m\\.)?soundcloud\\.com/\\S+",
            message = "debe ser una URL de soundcloud.com que empiece con https://")
    private String soundcloudUrl;

    public ConciertoRequest() {
    }

    public String getArtista() {
        return artista;
    }

    public void setArtista(String artista) {
        this.artista = artista;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getLugar() {
        return lugar;
    }

    public void setLugar(String lugar) {
        this.lugar = lugar;
    }

    public String getSoundcloudUrl() {
        return soundcloudUrl;
    }

    public void setSoundcloudUrl(String soundcloudUrl) {
        this.soundcloudUrl = soundcloudUrl;
    }

    // Rechaza campos fuera del contrato, tambien un id enviado por el cliente.
    @JsonAnySetter
    public void rechazarCampoDesconocido(String campo, Object valor) {
        throw new IllegalArgumentException("Campo no permitido: " + campo);
    }
}