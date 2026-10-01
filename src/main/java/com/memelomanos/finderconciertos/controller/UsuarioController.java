package com.memelomanos.finderconciertos.controller;

import com.memelomanos.finderconciertos.model.FavoritoRequest;
import com.memelomanos.finderconciertos.model.RegistroRequest;
import com.memelomanos.finderconciertos.model.Usuario;
import com.memelomanos.finderconciertos.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone los endpoints para el registro, consulta de perfil, y
 * favoritos de usuario. El controller solo recibe la peticion y
 * llama al service. Todas las rutas quedan bajo /api/v1 (ver WebConfig).
 */
@RestController
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Autowired
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // 201 Created porque se crea un recurso nuevo (asi lo dice el contrato).
    @PostMapping("/usuarios/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public Usuario registrarUsuario(@Valid @RequestBody RegistroRequest request) {
        return usuarioService.registrarUsuario(request.getNombre(), request.getCorreo());
    }

    @GetMapping("/usuarios/perfil")
    public Usuario obtenerPerfil(@RequestParam String correo) {
        return usuarioService.buscarPerfilPorCorreo(correo);
    }

    /**
     * Agrega un concierto a los favoritos del usuario.
     * este endpoint identifica al usuario con usuarioId en el
     * body porque todavia no hay autenticacion. Cuando se implemente
     * login, esto deberia cambiar a identificar al usuario por su
     * token en vez de recibir el id explicitamente.
     */
    @PostMapping("/usuarios/favoritos")
    public Usuario agregarFavorito(@Valid @RequestBody FavoritoRequest request) {
        return usuarioService.agregarFavorito(request.getUsuarioId(), request.getConciertoId());
    }
}
