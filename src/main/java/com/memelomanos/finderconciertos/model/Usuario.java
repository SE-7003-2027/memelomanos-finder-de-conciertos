package com.memelomanos.finderconciertos.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    @Column(unique = true)
    private String correo;

    @JsonIgnore
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) default 'USUARIO'")
    private RolUsuario rol = RolUsuario.USUARIO;

    @ManyToMany
    private List<Concierto> favoritos = new ArrayList<>();

    public Usuario() {
    }

    public Usuario(String nombre, String correo) {
        this.nombre = nombre;
        this.correo = correo;
    }

    public Usuario(String nombre, String correo, String password) {
        this(nombre, correo, password, RolUsuario.USUARIO);
    }

    public Usuario(String nombre, String correo, String password, RolUsuario rol) {
        this.nombre = nombre;
        this.correo = correo;
        this.password = password;
        this.rol = rol == null ? RolUsuario.USUARIO : rol;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public RolUsuario getRol() {
        return rol == null ? RolUsuario.USUARIO : rol;
    }

    public void setRol(RolUsuario rol) {
        this.rol = rol == null ? RolUsuario.USUARIO : rol;
    }

    public List<Concierto> getFavoritos() {
        return favoritos;
    }

    public void agregarFavorito(Concierto concierto) {
        favoritos.add(concierto);
    }

    public void quitarFavorito(Concierto concierto) {
        favoritos.remove(concierto);
    }
}
