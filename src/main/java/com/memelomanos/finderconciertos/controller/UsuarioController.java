package com.memelomanos.finderconciertos.controller;

import com.memelomanos.finderconciertos.model.Usuario;
import com.memelomanos.finderconciertos.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone los endpoints para el registro y consulta de perfil de usuario.
 * El controller solo recibe la peticion y llama al service.
 */
@RestController
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Autowired
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/usuarios/registro")
    public Usuario registrarUsuario(@RequestParam String nombre, @RequestParam String correo) {
        return usuarioService.registrarUsuario(nombre, correo);
    }

    @GetMapping("/usuarios/perfil")
    public Usuario obtenerPerfil(@RequestParam String correo) {
        return usuarioService.searchProfileByCorreo(correo);
    }
}