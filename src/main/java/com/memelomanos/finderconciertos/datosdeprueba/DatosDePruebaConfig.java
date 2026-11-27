package com.memelomanos.finderconciertos.datosdeprueba;

import com.memelomanos.finderconciertos.model.Concierto;
import com.memelomanos.finderconciertos.repository.ConciertoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Esta clase solo sirve para tener datos de prueba mientras
 * desarrollamos, para no probar el endpoint con la base vacia.
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

            LocalDate hoy = LocalDate.now(ZoneId.of("America/Mexico_City"));
            conciertoRepository.save(new Concierto(
                    "Queen", hoy.plusDays(7), "CDMX", "Foro Sol"));
            conciertoRepository.save(new Concierto(
                    "Bad Bunny", hoy.plusDays(22), "Guadalajara", "Estadio Akron"));
            conciertoRepository.save(new Concierto(
                    "Queen tribute band", hoy.plusDays(63), "Monterrey", "Arena Monterrey"));
        };
    }
}
