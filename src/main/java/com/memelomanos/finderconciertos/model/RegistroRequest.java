package com.memelomanos.finderconciertos.model;

// Representa el body JSON que se recibe al registrar un usuario nuevo.
// No es una entidad de base de datos, solo el "molde" de la peticion.
public class RegistroRequest {

    private String nombre;
    private String correo;

    public RegistroRequest() {
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
}
