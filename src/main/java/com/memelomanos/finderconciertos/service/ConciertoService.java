package com.memelomanos.finderconciertos.service;

import com.memelomanos.finderconciertos.exception.ConciertoNoEncontradoException;
import com.memelomanos.finderconciertos.model.Concierto;
import com.memelomanos.finderconciertos.model.ConciertoRequest;
import com.memelomanos.finderconciertos.model.Usuario;
import com.memelomanos.finderconciertos.repository.ConciertoRepository;
import com.memelomanos.finderconciertos.repository.UsuarioRepository;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConciertoService {

    private final ConciertoRepository conciertoRepository;
    private final UsuarioRepository usuarioRepository;

    public ConciertoService(ConciertoRepository conciertoRepository,
                            UsuarioRepository usuarioRepository) {
        this.conciertoRepository = conciertoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<Concierto> buscarPorArtista(String artista) {
        return buscar(artista, null);
    }

    @Transactional(readOnly = true)
    public List<Concierto> buscar(String artista, String ciudad) {
        String filtroArtista = artista == null ? "" : artista.strip();
        String filtroCiudad = ciudad == null ? "" : ciudad.strip();

        if (!filtroArtista.isEmpty() && !filtroCiudad.isEmpty()) {
            return conciertoRepository
                    .findByArtistaContainingIgnoreCaseAndCiudadContainingIgnoreCase(
                            filtroArtista, filtroCiudad);
        }

        if (!filtroArtista.isEmpty()) {
            return conciertoRepository.findByArtistaContainingIgnoreCase(
                    filtroArtista);
        }

        if (!filtroCiudad.isEmpty()) {
            return conciertoRepository.findByCiudadContainingIgnoreCase(
                    filtroCiudad);
        }

        return conciertoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Concierto buscarPorId(Long id) {
        return conciertoRepository.findById(id)
                .orElseThrow(() -> new ConciertoNoEncontradoException(
                        "Concierto no encontrado"));
    }

    @Transactional
    public Concierto crear(ConciertoRequest request) {
        Concierto concierto = new Concierto();
        copiarDatos(request, concierto);
        return conciertoRepository.save(concierto);
    }

    @Transactional
    public Concierto actualizar(Long id, ConciertoRequest request) {
        Concierto concierto = buscarPorId(id);
        copiarDatos(request, concierto);
        return conciertoRepository.save(concierto);
    }

    @Transactional
    public void eliminar(Long id) {
        Concierto concierto = buscarPorId(id);
        List<Usuario> usuarios = usuarioRepository.findUsuariosConFavorito(id);

        for (Usuario usuario : usuarios) {
            usuario.getFavoritos().removeIf(
                    favorito -> Objects.equals(favorito.getId(), id));
        }

        // Quita las referencias antes de borrar para respetar las claves foraneas.
        usuarioRepository.saveAll(usuarios);
        usuarioRepository.flush();
        conciertoRepository.delete(concierto);
    }

    private void copiarDatos(ConciertoRequest request, Concierto concierto) {
        concierto.setArtista(request.getArtista());
        concierto.setFecha(request.getFecha());
        concierto.setCiudad(request.getCiudad());
        concierto.setLugar(request.getLugar());
    }
}
