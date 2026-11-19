package com.memelomanos.finderconciertos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Concierto guardado en la base de datos con artista, fecha, ciudad y lugar.
 * soundcloudUrl es el perfil opcional del artista para la integración con SoundCloud
 * cuando faltan credenciales de su API (ADR-005).
 * Los eventos importados se identifican por fuente e id externo para actualizar
 * sus datos y conservar sus favoritos.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_concierto_fuente_id_externo",
        columnNames = {"fuente", "id_externo"}
))
public class Concierto {

    private static final int LONGITUD_MAXIMA_URL = 2048;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String artista;
    private LocalDate fecha;
    private String ciudad;
    private String lugar;
    private String soundcloudUrl;

    private String fuente;

    @Column(name = "id_externo")
    private String idExterno;

    @Column(precision = 12, scale = 9)
    private BigDecimal latitud;

    @Column(precision = 12, scale = 9)
    private BigDecimal longitud;

    @Column(length = LONGITUD_MAXIMA_URL)
    private String urlCompra;

    @Column(length = LONGITUD_MAXIMA_URL)
    private String imagenUrl;

    @Column(precision = 12, scale = 2)
    private BigDecimal precioMinimo;

    // JPA necesita un constructor vacio para poder crear los objetos
    // cuando lee de la base de datos.
    public Concierto() {
    }

    public Concierto(String artista, LocalDate fecha, String ciudad, String lugar) {
        this.artista = artista;
        this.fecha = fecha;
        this.ciudad = ciudad;
        this.lugar = lugar;
    }

    public Long getId() {
        return id;
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

    public String getFuente() {
        return fuente;
    }

    public void setFuente(String fuente) {
        this.fuente = fuente;
    }

    public String getIdExterno() {
        return idExterno;
    }

    public void setIdExterno(String idExterno) {
        this.idExterno = idExterno;
    }

    public BigDecimal getLatitud() {
        return latitud;
    }

    public void setLatitud(BigDecimal latitud) {
        this.latitud = latitud;
    }

    public BigDecimal getLongitud() {
        return longitud;
    }

    public void setLongitud(BigDecimal longitud) {
        this.longitud = longitud;
    }

    public String getUrlCompra() {
        return urlCompra;
    }

    public void setUrlCompra(String urlCompra) {
        this.urlCompra = urlCompra;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public BigDecimal getPrecioMinimo() {
        return precioMinimo;
    }

    public void setPrecioMinimo(BigDecimal precioMinimo) {
        this.precioMinimo = precioMinimo;
    }

    // Sin esto, dos objetos Concierto que representan la misma fila
    // en la base de datos no se consideran "iguales" al compararlos
    // (por ejemplo con .contains() en una lista), aunque tengan el
    // mismo id. Comparamos por id porque es lo que identifica de
    // verdad a un concierto ya guardado.
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Concierto concierto)) {
            return false;
        }
        return id != null && id.equals(concierto.getId());
    }

    @Override
    public int hashCode() {
        // Constante a proposito: el id es null antes de guardar y cambia
        // despues, asi el hash no cambia mientras el objeto esta en un Set.
        return Concierto.class.hashCode();
    }
}
