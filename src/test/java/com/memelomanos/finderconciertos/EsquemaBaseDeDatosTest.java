package com.memelomanos.finderconciertos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.memelomanos.finderconciertos.model.Concierto;
import com.memelomanos.finderconciertos.model.Usuario;
import com.memelomanos.finderconciertos.repository.ConciertoRepository;
import com.memelomanos.finderconciertos.repository.UsuarioRepository;
import java.time.LocalDate;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Arranca la aplicacion completa contra PostgreSQL (ADR-008). Si arranca,
 * Flyway aplico todas las migraciones y Hibernate (ddl-auto: validate)
 * confirmo que las entidades coinciden con la base.
 *
 * <p>Necesita una base real, asi que solo corre si DB_URL esta definida:
 * el CI la define con su servicio de PostgreSQL. En tu computadora,
 * levanta la base y ejecuta
 * {@code DB_URL=jdbc:postgresql://localhost:5432/finder_conciertos ./mvnw test}.
 */
@SpringBootTest
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "DB_URL", matches = ".+")
@Transactional
@DisplayName("Esquema de la base de datos (Flyway)")
class EsquemaBaseDeDatosTest {

    @Autowired
    private Flyway flyway;

    @Autowired
    private ConciertoRepository conciertoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    @DisplayName("Aplica todas las migraciones al arrancar")
    void arranque_AplicaLasMigraciones() {
        assertThat(flyway.info().current()).isNotNull();
        assertThat(flyway.info().pending()).isEmpty();
    }

    @Test
    @DisplayName("Guarda un concierto y lo marca como favorito de un usuario")
    void guardar_ConciertoYFavorito() {
        Concierto concierto = conciertoRepository.saveAndFlush(
                new Concierto("Queen", LocalDate.of(2027, 2, 20), "CDMX", "Auditorio Nacional"));
        Usuario usuario = new Usuario("Prueba", "esquema@ejemplo.com", "hash");
        usuario.agregarFavorito(concierto);
        usuarioRepository.saveAndFlush(usuario);

        Usuario leido = usuarioRepository.findById(usuario.getId()).orElseThrow();
        assertThat(leido.getFavoritos()).containsExactly(concierto);
    }

    @Test
    @DisplayName("Respeta la restriccion uk_concierto_fuente_id_externo")
    void guardar_EventoImportadoRepetido_Falla() {
        conciertoRepository.saveAndFlush(importado());

        assertThatThrownBy(() -> conciertoRepository.saveAndFlush(importado()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private static Concierto importado() {
        Concierto concierto = new Concierto("Bad Bunny", LocalDate.of(2027, 3, 5), "Guadalajara", "Estadio Akron");
        concierto.setFuente("ticketmaster");
        concierto.setIdExterno("evento-esquema-1");
        return concierto;
    }
}
