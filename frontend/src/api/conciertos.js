// Funciones para los endpoints de conciertos (ver openapi.yml).
import { pedir } from './cliente'

/**
 * GET /conciertos. Los filtros vacios se ignoran, asi que sin filtros
 * regresa todos los conciertos.
 * @param {{ artista?: string, ciudad?: string }} [filtros]
 */
export function buscarConciertos({ artista, ciudad } = {}) {
  return pedir('/conciertos', { params: { artista, ciudad } })
}

/** GET /conciertos/{id} */
export function obtenerConcierto(id) {
  return pedir(`/conciertos/${encodeURIComponent(id)}`)
}
