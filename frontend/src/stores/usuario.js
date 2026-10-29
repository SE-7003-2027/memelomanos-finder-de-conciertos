// Store de la sesion: usuario actual, su token y sus favoritos.
//
// El token se guarda solo en memoria, como decidio el ADR-004, asi que al
// recargar la pagina hay que volver a entrar. Es un reactive() sencillo
// para no agregar Pinia como dependencia.
import { reactive, readonly } from 'vue'
import * as api from '@/api/usuarios'
import { establecerToken, alRechazarSesion } from '@/api/cliente'

const estado = reactive({
  usuario: null, // { id, nombre, correo }
  favoritos: [], // lista de conciertos
  // Mensaje para la pantalla de entrar cuando la sesion se cerro sola
  // (por ejemplo, porque el token vencio).
  avisoSesion: '',
})

export const usuarioActual = readonly(estado)

// Si el backend rechaza el token en cualquier peticion, se cierra la sesion.
alRechazarSesion(() => {
  if (!estado.usuario) return
  cerrarSesion()
  estado.avisoSesion = 'Tu sesión venció. Vuelve a entrar para continuar.'
})

/**
 * Inicia sesion con correo y contraseña: pide el token, lo guarda y carga
 * los datos del usuario y sus favoritos. Lanza ErrorApi con status 401 si
 * las credenciales no coinciden.
 */
export async function iniciarSesion(correo, password) {
  const { token } = await api.iniciarSesion({ correo, password })
  const usuarioId = leerUsuarioIdDelToken(token)
  if (!usuarioId) {
    throw new Error('El servidor respondió con un token que no se pudo leer.')
  }

  establecerToken(token)
  try {
    const usuario = await api.obtenerUsuario(usuarioId)
    estado.usuario = { id: usuario.id, nombre: usuario.nombre, correo: usuario.correo }
    estado.avisoSesion = ''
    await recargarFavoritos()
  } catch (error) {
    cerrarSesion()
    throw error
  }
}

export function cerrarSesion() {
  establecerToken(null)
  estado.usuario = null
  estado.favoritos = []
}

/**
 * Comprueba la contraseña del usuario actual pidiendo un token nuevo, sin
 * cambiar la sesion. Regresa true si es correcta.
 */
export async function verificarPassword(password) {
  if (!estado.usuario) return false
  try {
    await api.iniciarSesion({ correo: estado.usuario.correo, password })
    return true
  } catch (error) {
    if (error.status === 401) return false
    throw error
  }
}

export async function recargarFavoritos() {
  if (!estado.usuario) return
  estado.favoritos = await api.obtenerFavoritos(estado.usuario.id)
}

export function actualizarDatos(usuario) {
  estado.usuario = { id: usuario.id, nombre: usuario.nombre, correo: usuario.correo }
}

export function esFavorito(conciertoId) {
  return estado.favoritos.some((c) => c.id === conciertoId)
}

export async function agregarFavorito(concierto) {
  if (!estado.usuario || esFavorito(concierto.id)) return
  const usuario = await api.agregarFavorito(estado.usuario.id, concierto.id)
  // El backend regresa el usuario con su lista de favoritos ya actualizada.
  estado.favoritos = usuario.favoritos ?? [...estado.favoritos, concierto]
}

export async function quitarFavorito(conciertoId) {
  if (!estado.usuario) return
  await api.quitarFavorito(estado.usuario.id, conciertoId)
  estado.favoritos = estado.favoritos.filter((c) => c.id !== conciertoId)
}

// El backend guarda el id del usuario en el claim "usuarioId" del JWT.
// Solo se lee el contenido; la firma la valida el backend en cada peticion.
function leerUsuarioIdDelToken(token) {
  try {
    const parte = token.split('.')[1]
    const base64 = parte.replace(/-/g, '+').replace(/_/g, '/')
    const relleno = base64 + '='.repeat((4 - (base64.length % 4)) % 4)
    const datos = JSON.parse(atob(relleno))
    return datos.usuarioId ?? null
  } catch {
    return null
  }
}
