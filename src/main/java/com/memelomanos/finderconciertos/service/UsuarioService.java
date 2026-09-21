package com.memelomanos.finderconciertos.service;

import com.memelomanos.finderconciertos.exception.CorreoDuplicadoException;
import com.memelomanos.finderconciertos.exception.UsuarioNoEncontradoException;
import com.memelomanos.finderconciertos.model.Concierto;
import com.memelomanos.finderconciertos.model.Usuario;
import com.memelomanos.finderconciertos.repository.ConciertoRepository;
import com.memelomanos.finderconciertos.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ConciertoRepository conciertoRepository;

    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository,
                           ConciertoRepository conciertoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.conciertoRepository = conciertoRepository;
    }

    public Usuario registrarUsuario(String nombre, String correo) {
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new CorreoDuplicadoException("El correo que intentas introducir ya se encuentra registrado.");
        }

        Usuario userNuevo = new Usuario();
        userNuevo.setNombre(nombre);
        userNuevo.setCorreo(correo);
        return usuarioRepository.save(userNuevo);
    }

    public Usuario buscarPerfilPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));
    }

    public Usuario agregarFavorito(Long usuarioId, Long conciertoId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));

        Concierto concierto = conciertoRepository.findById(conciertoId)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Concierto no encontrado"));

        usuario.agregarFavorito(concierto);

        return usuarioRepository.save(usuario);
    }
}
