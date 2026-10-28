package com.memelomanos.finderconciertos.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Representa el body JSON que se recibe al registrar un usuario nuevo.
// No es una entidad de base de datos, solo el "molde" de la peticion.
// Las anotaciones reflejan los campos "required" y "format: email"
// del contrato; se revisan cuando el controller usa @Valid.
public class RegistroRequest {

    @NotBlank
    private String nombre;

    @NotBlank
    @Email
    private String correo;

    @NotBlank
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
