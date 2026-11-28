package com.memelomanos.finderconciertos.model;

/**
 * Roles disponibles para los usuarios de la aplicación (ver ADR-004).
 * Todo registro público obtiene {@link #USUARIO} por defecto; {@link #ADMIN}
 * habilita la administración de conciertos y los endpoints {@code /admin/**}.
 */
public enum RolUsuario {
    USUARIO,
    ADMIN
}
