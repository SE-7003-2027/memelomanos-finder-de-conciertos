// Funciones para los endpoints de artistas (ver openapi.yml).
import { pedir } from './cliente'

/**
 * GET /artistas/{nombre}/soundcloud -> { artista, perfilUrl, avatarUrl,
 * widgetUrl, pistas: [{ titulo, url, duracionMs }], fuente } (#81).
 * Responde 404 si el artista no tiene perfil en SoundCloud o si SoundCloud
 * no respondio. Es publico, asi que no manda el token.
 */
export function obtenerSoundCloudArtista(nombre) {
  return pedir(`/artistas/${encodeURIComponent(nombre)}/soundcloud`, { publico: true })
}
