package com.memelomanos.finderconciertos.service;

import com.memelomanos.finderconciertos.exception.ArtistaSinSoundCloudException;
import com.memelomanos.finderconciertos.integracion.soundcloud.ArtistaSoundCloud;
import com.memelomanos.finderconciertos.integracion.soundcloud.SoundCloudOEmbedCliente;
import com.memelomanos.finderconciertos.integracion.soundcloud.SoundCloudPropiedades;
import com.memelomanos.finderconciertos.model.Concierto;
import com.memelomanos.finderconciertos.repository.ConciertoRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Obtiene el perfil de SoundCloud de un artista siguiendo ADR-005. Por
 * ahora usa el Plan B: oEmbed con la URL guardada en sus conciertos. Las
 * respuestas se guardan en memoria para no repetir llamadas.
 */
@Service
public class ArtistaService {

    private static final Logger log = LoggerFactory.getLogger(ArtistaService.class);

    // Tope para que nombres inventados no hagan crecer la cache sin limite.
    static final int MAXIMO_EN_CACHE = 500;

    private final SoundCloudOEmbedCliente oEmbedCliente;
    private final ConciertoRepository conciertoRepository;
    private final SoundCloudPropiedades propiedades;
    private final Clock reloj;
    private final Map<String, EntradaCache> cache = new ConcurrentHashMap<>();

    @Autowired
    public ArtistaService(SoundCloudOEmbedCliente oEmbedCliente,
                          ConciertoRepository conciertoRepository,
                          SoundCloudPropiedades propiedades) {
        this(oEmbedCliente, conciertoRepository, propiedades, Clock.systemUTC());
    }

    ArtistaService(SoundCloudOEmbedCliente oEmbedCliente,
                   ConciertoRepository conciertoRepository,
                   SoundCloudPropiedades propiedades,
                   Clock reloj) {
        this.oEmbedCliente = oEmbedCliente;
        this.conciertoRepository = conciertoRepository;
        this.propiedades = propiedades;
        this.reloj = reloj;
    }

    /**
     * @throws ArtistaSinSoundCloudException si el artista no tiene perfil
     *         o SoundCloud no respondio (se responde 404, no 500)
     */
    public ArtistaSoundCloud obtenerSoundCloud(String nombre) {
        String nombreLimpio = nombre == null ? "" : nombre.trim();
        if (nombreLimpio.isEmpty()) {
            throw new IllegalArgumentException("El nombre del artista no puede estar vacío");
        }

        String clave = nombreLimpio.toLowerCase(Locale.ROOT);
        Instant ahora = reloj.instant();
        EntradaCache guardada = cache.get(clave);
        if (guardada != null && ahora.isBefore(guardada.venceEn())) {
            return guardada.datos();
        }

        ArtistaSoundCloud datos = buscarConOEmbed(nombreLimpio)
                .orElseThrow(() -> new ArtistaSinSoundCloudException(
                        "No encontramos a " + nombreLimpio + " en SoundCloud"));

        guardarEnCache(clave, datos, ahora);
        return datos;
    }

    private Optional<ArtistaSoundCloud> buscarConOEmbed(String nombre) {
        Optional<String> perfilUrl = conciertoRepository
                .findFirstByArtistaIgnoreCaseAndSoundcloudUrlIsNotNull(nombre)
                .map(Concierto::getSoundcloudUrl);
        if (perfilUrl.isEmpty()) {
            return Optional.empty();
        }
        try {
            return oEmbedCliente.obtenerReproductor(nombre, perfilUrl.get());
        } catch (RuntimeException ex) {
            log.warn("oEmbed de SoundCloud fallo para '{}': {}", perfilUrl.get(), ex.getMessage());
            return Optional.empty();
        }
    }

    private void guardarEnCache(String clave, ArtistaSoundCloud datos, Instant ahora) {
        if (cache.size() >= MAXIMO_EN_CACHE) {
            cache.values().removeIf(entrada -> !ahora.isBefore(entrada.venceEn()));
            if (cache.size() >= MAXIMO_EN_CACHE) {
                cache.clear();
            }
        }
        cache.put(clave, new EntradaCache(datos, ahora.plus(propiedades.duracionCache())));
    }

    private record EntradaCache(ArtistaSoundCloud datos, Instant venceEn) {
    }
}
