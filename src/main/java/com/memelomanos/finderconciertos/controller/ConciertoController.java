package com.memelomanos.finderconciertos.controller;

import com.memelomanos.finderconciertos.model.Concierto;
import com.memelomanos.finderconciertos.service.ConciertoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * Expone el endpoint GET /conciertos?artista=X
 * El controller solo recibe la peticion y llama al service, no
 * tiene logica de negocio aqui.
 */
@RestController
public class ConciertoController {

    private final ConciertoService conciertoService;

    @Autowired
    public ConciertoController(ConciertoService conciertoService) {
        this.conciertoService = conciertoService;
    }

    @GetMapping("/conciertos")
    public List<Concierto> buscarPorArtista(@RequestParam String artista) {
        return conciertoService.buscarPorArtista(artista);
    }
}
