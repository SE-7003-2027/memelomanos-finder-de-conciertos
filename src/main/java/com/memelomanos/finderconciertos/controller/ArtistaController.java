package com.memelomanos.finderconciertos.controller;

import com.memelomanos.finderconciertos.integracion.soundcloud.ArtistaSoundCloud;
import com.memelomanos.finderconciertos.service.ArtistaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ArtistaController {

    private final ArtistaService artistaService;

    public ArtistaController(ArtistaService artistaService) {
        this.artistaService = artistaService;
    }

    // Perfil y pistas del artista en SoundCloud (#81). Es publico, igual
    // que la consulta de conciertos, porque se usa en el detalle.
    @GetMapping("/artistas/{nombre}/soundcloud")
    public ArtistaSoundCloud obtenerSoundCloud(@PathVariable String nombre) {
        return artistaService.obtenerSoundCloud(nombre);
    }
}
