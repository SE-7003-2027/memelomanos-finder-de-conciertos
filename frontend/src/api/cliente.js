// Cliente HTTP base. Todas las llamadas a la API pasan por aqui para no
// repetir fetch, encabezados ni manejo de errores en cada vista (issue #51).

const PREFIJO_API = '/api/v1'

// Token JWT de la sesion actual. Siguiendo el ADR-004 se guarda solo en
// memoria (no en localStorage), asi que se pierde al recargar la pagina.
let tokenActual = null

// Funcion que se llama cuando el backend rechaza un token (vencido o
// invalido). La registra el store del usuario para cerrar la sesion.
let alRechazarToken = null

export function establecerToken(token) {
  tokenActual = token || null
}

export function hayToken() {
  return tokenActual !== null
}

export function alRechazarSesion(funcion) {
  alRechazarToken = funcion
}

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
 * @param {boolean} [opciones.publico=false] no manda el token (registro y login)
 */
export async function pedir(ruta, { metodo = 'GET', cuerpo, params, publico = false } = {}) {
  const url = PREFIJO_API + ruta + construirQuery(params)

  const encabezados = { Accept: 'application/json' }
  if (cuerpo !== undefined) {
    encabezados['Content-Type'] = 'application/json'
  }
  const conToken = !publico && tokenActual !== null
  if (conToken) {
    encabezados.Authorization = `Bearer ${tokenActual}`
  }

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
    // Si mandamos token y aun asi responde 401, el token ya no sirve.
    if (respuesta.status === 401 && conToken && alRechazarToken) {
      alRechazarToken()
    }
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
  if (status === 401) return 'Tu sesion no es valida. Vuelve a entrar.'
  if (status === 404) return 'No se encontro lo que buscabas.'
  if (status >= 500) return 'El servidor tuvo un problema. Intenta de nuevo en un momento.'
  return `La peticion fallo (error ${status}).`
}
