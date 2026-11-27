package com.memelomanos.finderconciertos.service;

import com.memelomanos.finderconciertos.exception.ArtistaSinSoundCloudException;
import com.memelomanos.finderconciertos.integracion.soundcloud.ArtistaSoundCloud;
import com.memelomanos.finderconciertos.integracion.soundcloud.NombresSoundCloud;
import com.memelomanos.finderconciertos.integracion.soundcloud.SoundCloudCliente;
import com.memelomanos.finderconciertos.integracion.soundcloud.SoundCloudOEmbedCliente;
import com.memelomanos.finderconciertos.integracion.soundcloud.SoundCloudPropiedades;
import com.memelomanos.finderconciertos.model.Concierto;
import com.memelomanos.finderconciertos.repository.ConciertoRepository;
import java.time.Clock;
import java.time.Duration;
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
 * Obtiene el perfil de SoundCloud de un artista siguiendo ADR-005, en
 * este orden:
 * <ol>
 *   <li>la soundcloudUrl capturada a mano en sus conciertos, con oEmbed;</li>
 *   <li>la API, si hay credenciales (Plan A);</li>
 *   <li>las URLs mas probables segun el nombre, con oEmbed (Plan C).</li>
 * </ol>
 * Las respuestas, tambien las de "no encontrado", se guardan en memoria
 * para no repetir llamadas.
 */
@Service
public class ArtistaService {

    private static final Logger log = LoggerFactory.getLogger(ArtistaService.class);

    // Tope para que nombres inventados no hagan crecer la cache sin limite.
    static final int MAXIMO_EN_CACHE = 500;

    private final SoundCloudCliente soundCloudCliente;
    private final SoundCloudOEmbedCliente oEmbedCliente;
    private final ConciertoRepository conciertoRepository;
    private final SoundCloudPropiedades propiedades;
    private final Clock reloj;
    private final Map<String, EntradaCache> cache = new ConcurrentHashMap<>();

    @Autowired
    public ArtistaService(SoundCloudCliente soundCloudCliente,
                          SoundCloudOEmbedCliente oEmbedCliente,
                          ConciertoRepository conciertoRepository,
                          SoundCloudPropiedades propiedades) {
        this(soundCloudCliente, oEmbedCliente, conciertoRepository, propiedades, Clock.systemUTC());
    }

    ArtistaService(SoundCloudCliente soundCloudCliente,
                   SoundCloudOEmbedCliente oEmbedCliente,
                   ConciertoRepository conciertoRepository,
                   SoundCloudPropiedades propiedades,
                   Clock reloj) {
        this.soundCloudCliente = soundCloudCliente;
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

        // La URL manual forma parte de la clave: si alguien la captura o la
        // corrige, la siguiente visita ya no usa lo que estaba en cache.
        Optional<String> urlManual = urlGuardada(nombreLimpio);
        String clave = nombreLimpio.toLowerCase(Locale.ROOT) + urlManual.map(url -> " " + url).orElse("");
        Instant ahora = reloj.instant();
        EntradaCache guardada = cache.get(clave);
        if (guardada != null && ahora.isBefore(guardada.venceEn())) {
            return guardada.datos().orElseThrow(() -> noEncontrado(nombreLimpio));
        }

        RegistroDeFallas fallas = new RegistroDeFallas();
        Optional<ArtistaSoundCloud> datos = urlManual
                .flatMap(url -> buscarConUrlManual(nombreLimpio, url, fallas))
                .or(() -> buscarConApi(nombreLimpio, fallas))
                .or(() -> buscarConCandidatas(nombreLimpio, fallas));

        if (datos.isPresent()) {
            guardarEnCache(clave, datos, ahora, propiedades.duracionCache());
            return datos.get();
        }
        // Si SoundCloud fallo no sabemos si el artista existe, asi que no
        // recordamos el "no encontrado" y la siguiente visita lo vuelve a intentar.
        if (!fallas.hubo) {
            guardarEnCache(clave, Optional.empty(), ahora, propiedades.duracionCacheNoEncontrados());
        }
        throw noEncontrado(nombreLimpio);
    }

    private Optional<String> urlGuardada(String nombre) {
        return conciertoRepository
                .findFirstByArtistaIgnoreCaseAndSoundcloudUrlIsNotNull(nombre)
                .map(Concierto::getSoundcloudUrl);
    }

    private Optional<ArtistaSoundCloud> buscarConUrlManual(String nombre, String perfilUrl,
                                                         RegistroDeFallas fallas) {
        try {
            return oEmbedCliente.obtenerReproductor(nombre, perfilUrl);
        } catch (RuntimeException ex) {
            // Puede ser una URL mal capturada o que SoundCloud no respondio:
            // seguimos con la busqueda por nombre en lugar de responder 500.
            log.warn("oEmbed de SoundCloud fallo para '{}': {}", perfilUrl, ex.getMessage());
            fallas.hubo = true;
            return Optional.empty();
        }
    }

    private Optional<ArtistaSoundCloud> buscarConApi(String nombre, RegistroDeFallas fallas) {
        if (!soundCloudCliente.estaConfigurado()) {
            return Optional.empty();
        }
        try {
            return soundCloudCliente.buscarArtista(nombre);
        } catch (RuntimeException ex) {
            // Token rechazado, limite alcanzado o timeout: seguimos con el Plan C.
            log.warn("La API de SoundCloud fallo al buscar '{}': {}", nombre, ex.getMessage());
            fallas.hubo = true;
            return Optional.empty();
        }
    }

    // Se detiene en la primera candidata que coincide. Si SoundCloud falla
    // (no un 404), tampoco sigue: las demas fallarian igual y cada una
    // puede tardar hasta el timeout.
    private Optional<ArtistaSoundCloud> buscarConCandidatas(String nombre, RegistroDeFallas fallas) {
        for (String perfilUrl : NombresSoundCloud.candidatas(nombre)) {
            try {
                Optional<ArtistaSoundCloud> encontrado = oEmbedCliente.buscarPerfil(nombre, perfilUrl);
                if (encontrado.isPresent()) {
                    return encontrado;
                }
            } catch (RuntimeException ex) {
                log.warn("oEmbed de SoundCloud fallo para '{}': {}", perfilUrl, ex.getMessage());
                fallas.hubo = true;
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private static ArtistaSinSoundCloudException noEncontrado(String nombre) {
        return new ArtistaSinSoundCloudException("No encontramos a " + nombre + " en SoundCloud");
    }

    private void guardarEnCache(String clave, Optional<ArtistaSoundCloud> datos,
                                Instant ahora, Duration duracion) {
        if (cache.size() >= MAXIMO_EN_CACHE) {
            cache.values().removeIf(entrada -> !ahora.isBefore(entrada.venceEn()));
            if (cache.size() >= MAXIMO_EN_CACHE) {
                cache.clear();
            }
        }
        cache.put(clave, new EntradaCache(datos, ahora.plus(duracion)));
    }

    private record EntradaCache(Optional<ArtistaSoundCloud> datos, Instant venceEn) {
    }

    // Anota si algun plan fallo por SoundCloud (y no porque el artista no exista).
    private static final class RegistroDeFallas {
        private boolean hubo;
    }
}
