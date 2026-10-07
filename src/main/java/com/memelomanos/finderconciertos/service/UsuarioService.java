package com.memelomanos.finderconciertos.service;

import com.memelomanos.finderconciertos.exception.ConciertoNoEncontradoException;
import com.memelomanos.finderconciertos.exception.CorreoDuplicadoException;
import com.memelomanos.finderconciertos.exception.UsuarioNoEncontradoException;
import com.memelomanos.finderconciertos.model.Concierto;
import com.memelomanos.finderconciertos.model.Usuario;
import com.memelomanos.finderconciertos.repository.ConciertoRepository;
import com.memelomanos.finderconciertos.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
            throw new CorreoDuplicadoException(
                    "El correo que intentas introducir ya se encuentra registrado.");
        }

        Usuario userNuevo = new Usuario();
        userNuevo.setNombre(nombre);
        userNuevo.setCorreo(correo);
        return usuarioRepository.save(userNuevo);
    }

    public Usuario buscarPerfilPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() ->
                        new UsuarioNoEncontradoException("Usuario no encontrado"));
    }

    public Usuario agregarFavorito(Long usuarioId, Long conciertoId) {
        Usuario usuario = buscarUsuarioPorId(usuarioId);

        Concierto concierto = conciertoRepository.findById(conciertoId)
                .orElseThrow(() ->
                        new ConciertoNoEncontradoException("Concierto no encontrado"));

        if (!usuario.getFavoritos().contains(concierto)) {
            usuario.agregarFavorito(concierto);
            usuario = usuarioRepository.save(usuario);
        }

        return usuario;
    }

    public Usuario buscarUsuarioPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new UsuarioNoEncontradoException("Usuario no encontrado"));
    }

    public Usuario actualizarUsuario(Long id, String nombre, String correo) {
        Usuario usuario = buscarUsuarioPorId(id);

        usuarioRepository.findByCorreo(correo).ifPresent(otroUsuario -> {
            if (!otroUsuario.getId().equals(id)) {
                throw new CorreoDuplicadoException(
                        "El correo ya está registrado por otro usuario.");
            }
        });

        usuario.setNombre(nombre);
        usuario.setCorreo(correo);

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void eliminarUsuario(Long id) {
        Usuario usuario = buscarUsuarioPorId(id);
        usuarioRepository.delete(usuario);
    }

    @Transactional(readOnly = true)
    public List<Concierto> obtenerFavoritos(Long id) {
        Usuario usuario = buscarUsuarioPorId(id);
        return usuario.getFavoritos();
    }

    @Transactional
    public void quitarFavorito(Long usuarioId, Long conciertoId) {
        Usuario usuario = buscarUsuarioPorId(usuarioId);

        Concierto concierto = conciertoRepository.findById(conciertoId)
                .orElseThrow(() ->
                        new ConciertoNoEncontradoException("Concierto no encontrado"));

        if (!usuario.getFavoritos().contains(concierto)) {
            throw new ConciertoNoEncontradoException(
                    "El concierto no se encuentra entre los favoritos del usuario.");
        }

        usuario.quitarFavorito(concierto);
        usuarioRepository.save(usuario);
    }
}

