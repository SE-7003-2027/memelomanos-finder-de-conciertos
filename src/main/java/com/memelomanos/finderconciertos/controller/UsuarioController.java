package com.memelomanos.finderconciertos.controller;

import com.memelomanos.finderconciertos.model.Concierto;
import com.memelomanos.finderconciertos.model.FavoritoRequest;
import com.memelomanos.finderconciertos.model.RegistroRequest;
import com.memelomanos.finderconciertos.model.Usuario;
import com.memelomanos.finderconciertos.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Autowired
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/usuarios/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public Usuario registrarUsuario(@Valid @RequestBody RegistroRequest request) {
        return usuarioService.registrarUsuario(
                request.getNombre(), request.getCorreo());
    }

    @GetMapping("/usuarios/perfil")
    public Usuario obtenerPerfil(@RequestParam String correo) {
        return usuarioService.buscarPerfilPorCorreo(correo);
    }

    @PostMapping("/usuarios/favoritos")
    public Usuario agregarFavorito(@Valid @RequestBody FavoritoRequest request) {
        return usuarioService.agregarFavorito(
                request.getUsuarioId(), request.getConciertoId());
    }

    @GetMapping("/usuarios/{id}")
    public Usuario obtenerUsuario(@PathVariable Long id) {
        return usuarioService.buscarUsuarioPorId(id);
    }

    @PutMapping("/usuarios/{id}")
    public Usuario actualizarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody RegistroRequest request) {
        return usuarioService.actualizarUsuario(
                id, request.getNombre(), request.getCorreo());
    }

    @DeleteMapping("/usuarios/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
    }

    @GetMapping("/usuarios/{id}/favoritos")
    public List<Concierto> obtenerFavoritos(@PathVariable Long id) {
        return usuarioService.obtenerFavoritos(id);
    }

    @DeleteMapping("/usuarios/{id}/favoritos/{conciertoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void quitarFavorito(
            @PathVariable Long id,
            @PathVariable Long conciertoId) {
        usuarioService.quitarFavorito(id, conciertoId);
    }
}

