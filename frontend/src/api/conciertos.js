// Funciones para los endpoints de conciertos (ver openapi.yml).
import { pedir } from './cliente'

/**
 * GET /conciertos. Los filtros vacíos se ignoran, así que sin filtros
 * regresa todos los conciertos.
 * @param {{ artista?: string, ciudad?: string, fechaDesde?: string, fechaHasta?: string }} [filtros]
 */
export function buscarConciertos({
    artista,
    ciudad,
    fechaDesde,
    fechaHasta,
  } = {}) {
  return pedir('/conciertos', {
    params: { artista, ciudad, fechaDesde, fechaHasta },
  })
}

/** GET /conciertos/{id} */
export function obtenerConcierto(id) {
  return pedir(`/conciertos/${encodeURIComponent(id)}`)
}
