package com.memelomanos.finderconciertos.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;

// Entidad que representa un concierto. Por ahora solo tiene los
// campos basicos que definimos en el PRD (artista, fecha, ciudad,
// lugar). El campo artista esta pensado para que el issue de
// busqueda (#14) pueda filtrar directo por el.
@Entity
public class Concierto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String artista;
    private LocalDate fecha;
    private String ciudad;
    private String lugar;

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
}
