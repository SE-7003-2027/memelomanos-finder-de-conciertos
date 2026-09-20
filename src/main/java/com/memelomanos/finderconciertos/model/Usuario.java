package com.memelomanos.finderconciertos.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa el perfil de un usuario. La relacion con
 * Concierto (favoritos) es ManyToMany porque un usuario puede tener
 * varios conciertos favoritos, y un concierto puede ser favorito de
 * varios usuarios.
 */
@Entity
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String correo;

    @ManyToMany
    private List<Concierto> favoritos = new ArrayList<>();

    public Usuario() {
    }

    public Usuario(String nombre, String correo) {
        this.nombre = nombre;
        this.correo = correo;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public List<Concierto> getFavoritos() {
        return favoritos;
    }

    // Metodo simple para agregar un favorito sin tener que exponer
    // la lista completa para modificarla desde afuera.
    public void agregarFavorito(Concierto concierto) {
        favoritos.add(concierto);
    }
}
