package com.memelomanos.finderconciertos.datosdeprueba;

import com.memelomanos.finderconciertos.model.Concierto;
import com.memelomanos.finderconciertos.repository.ConciertoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDate;

/**
 * Esta clase solo sirve para tener datos de prueba mientras
 * desarrollamos, para no probar el endpoint con la base vacia.
 * Cuando ya tengamos un endpoint para crear conciertos de verdad,
 * se puede borrar esta clase.
 */
// TODO(#95): borrar estos datos cuando la sincronización real (#80) esté estable
@Configuration
@Profile("dev")
public class DatosDePruebaConfig {

    @Bean
    CommandLineRunner cargarDatosDePrueba(ConciertoRepository conciertoRepository) {
        return args -> {
            // Evita duplicar los datos en cada arranque.
            if (conciertoRepository.count() > 0) {
                return;
            }

            conciertoRepository.save(new Concierto(
                    "Queen", LocalDate.of(2026, 11, 20), "CDMX", "Foro Sol"));
            conciertoRepository.save(new Concierto(
                    "Bad Bunny", LocalDate.of(2026, 12, 5), "Guadalajara", "Estadio Akron"));
            conciertoRepository.save(new Concierto(
                    "Queen tribute band", LocalDate.of(2027, 1, 15), "Monterrey", "Arena Monterrey"));
        };
    }
}
