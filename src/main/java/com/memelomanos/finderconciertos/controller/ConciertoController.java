package com.memelomanos.finderconciertos.controller;

import com.memelomanos.finderconciertos.model.Concierto;
import com.memelomanos.finderconciertos.model.ConciertoRequest;
import com.memelomanos.finderconciertos.service.ConciertoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConciertoController {

    private final ConciertoService conciertoService;

    public ConciertoController(ConciertoService conciertoService) {
        this.conciertoService = conciertoService;
    }

    @GetMapping("/conciertos")
    public List<Concierto> buscarPorArtista(
            @RequestParam(required = false) String artista) {
        return conciertoService.buscarPorArtista(artista);
    }

    @GetMapping("/conciertos/{id}")
    public Concierto buscarPorId(@PathVariable Long id) {
        return conciertoService.buscarPorId(id);
    }

    @PostMapping("/conciertos")
    @ResponseStatus(HttpStatus.CREATED)
    public Concierto crear(@Valid @RequestBody ConciertoRequest request) {
        return conciertoService.crear(request);
    }

    @PutMapping("/conciertos/{id}")
    public Concierto actualizar(@PathVariable Long id,
                               @Valid @RequestBody ConciertoRequest request) {
        return conciertoService.actualizar(id, request);
    }

    @DeleteMapping("/conciertos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        conciertoService.eliminar(id);
    }
}