package com.memelomanos.finderconciertos.controller;

import com.memelomanos.finderconciertos.service.SincronizacionService;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Profile("dev")
@RestController
public class SincronizacionController {

    private final SincronizacionService sincronizacion;

    public SincronizacionController(SincronizacionService sincronizacion) {
        this.sincronizacion = sincronizacion;
    }

    @PostMapping("/admin/sincronizar")
    public Map<String, Integer> sincronizar() {
        return Map.of("procesados", sincronizacion.sincronizar());
    }
}
