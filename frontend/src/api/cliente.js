// Cliente HTTP base. Todas las llamadas a la API pasan por aqui para no
// repetir fetch, encabezados ni manejo de errores en cada vista (issue #51).

const PREFIJO_API = '/api/v1'

// Error que lanza el cliente cuando la API responde con un status que no
// es 2xx. Trae el status y el mensaje que manda el backend en su formato
// de error comun: { status, error, mensaje, timestamp }.
export class ErrorApi extends Error {
  constructor(status, mensaje, detalle = null) {
    super(mensaje)
    this.name = 'ErrorApi'
    this.status = status
    this.detalle = detalle
  }
}

/**
 * Hace una peticion a la API y regresa el JSON de la respuesta (o null si
 * la respuesta no trae cuerpo, como un 204).
 *
 * @param {string} ruta ruta relativa al prefijo /api/v1, por ejemplo '/conciertos'
 * @param {object} [opciones]
 * @param {string} [opciones.metodo='GET']
 * @param {object} [opciones.cuerpo] objeto que se manda como JSON
 * @param {object} [opciones.params] parametros de query; los vacios se omiten
 */
export async function pedir(ruta, { metodo = 'GET', cuerpo, params } = {}) {
  const url = PREFIJO_API + ruta + construirQuery(params)

  const encabezados = { Accept: 'application/json' }
  if (cuerpo !== undefined) {
    encabezados['Content-Type'] = 'application/json'
  }
  // TODO (#57): cuando exista el login, agregar aqui el token:
  // encabezados.Authorization = `Bearer ${token}`

  let respuesta
  try {
    respuesta = await fetch(url, {
      method: metodo,
      headers: encabezados,
      body: cuerpo !== undefined ? JSON.stringify(cuerpo) : undefined,
    })
  } catch {
    throw new ErrorApi(0, 'No se pudo conectar con el servidor. Revisa que el backend este corriendo.')
  }

  const datos = await leerJson(respuesta)

  if (!respuesta.ok) {
    const mensaje = datos?.mensaje || mensajePorStatus(respuesta.status)
    throw new ErrorApi(respuesta.status, mensaje, datos)
  }

  return datos
}

function construirQuery(params) {
  if (!params) return ''
  const query = new URLSearchParams()
  for (const [clave, valor] of Object.entries(params)) {
    if (valor !== undefined && valor !== null && String(valor).trim() !== '') {
      query.append(clave, String(valor).trim())
    }
  }
  const texto = query.toString()
  return texto ? `?${texto}` : ''
}

async function leerJson(respuesta) {
  const texto = await respuesta.text()
  if (!texto) return null
  try {
    return JSON.parse(texto)
  } catch {
    return null
  }
}

function mensajePorStatus(status) {
  if (status === 404) return 'No se encontro lo que buscabas.'
  if (status >= 500) return 'El servidor tuvo un problema. Intenta de nuevo en un momento.'
  return `La peticion fallo (error ${status}).`
}
