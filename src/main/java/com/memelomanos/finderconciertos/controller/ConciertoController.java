package com.memelomanos.finderconciertos.controller;

import com.memelomanos.finderconciertos.integracion.superboletos.SuperboletosProveedor;
import com.memelomanos.finderconciertos.model.Concierto;
import com.memelomanos.finderconciertos.model.ConciertoRequest;
import com.memelomanos.finderconciertos.service.ConciertoService;
import jakarta.validation.Valid;
import java.util.List;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
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
    private final SuperboletosProveedor superboletosProveedor;

    public ConciertoController(
        ConciertoService conciertoService,
        SuperboletosProveedor superboletosProveedor) {
        this.conciertoService = conciertoService;
        this.superboletosProveedor = superboletosProveedor;
    }

    @GetMapping("/conciertos")
    public List<Concierto> buscar(
        @RequestParam(required = false) String artista,
        @RequestParam(required = false) String ciudad,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta) {

    if (fechaDesde != null && fechaHasta != null
            && fechaDesde.isAfter(fechaHasta)) {
        throw new IllegalArgumentException(
                "La fechaDesde no puede ser posterior a fechaHasta");
    }

    return conciertoService.buscar(artista, ciudad, fechaDesde, fechaHasta);
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

    @GetMapping("/conciertos/{id}/superboletos")
    public Map<String, String> buscarEnSuperboletos(@PathVariable Long id) {
    Concierto concierto = conciertoService.buscarPorId(id);

    String url = superboletosProveedor.generarUrlBusqueda(
            concierto.getArtista()
    );

    return Map.of("urlBusqueda", url);
}
}