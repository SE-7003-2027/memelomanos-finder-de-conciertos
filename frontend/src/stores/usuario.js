// Store del "usuario actual" (issue #52).
//
// Mientras no haya login (#57), el usuario actual se elige registrandose o
// buscando el perfil por correo. Solo se guarda su id en localStorage para
// que no se pierda al recargar; los datos se vuelven a pedir a la API.
//
// Es un reactive() sencillo para no agregar Pinia como dependencia. Si el
// equipo decide usar Pinia, la API publica de este modulo se puede mantener.
import { reactive, readonly } from 'vue'
import * as api from '@/api/usuarios'

const CLAVE_STORAGE = 'finder.usuarioId'

const estado = reactive({
  usuario: null, // { id, nombre, correo }
  favoritos: [], // lista de conciertos
  cargando: false,
})

export const usuarioActual = readonly(estado)

/** Deja a este usuario como el actual (despues de registrarse o entrar). */
export async function iniciarSesion(usuario) {
  estado.usuario = { id: usuario.id, nombre: usuario.nombre, correo: usuario.correo }
  guardarId(usuario.id)
  await recargarFavoritos()
}

export function cerrarSesion() {
  estado.usuario = null
  estado.favoritos = []
  guardarId(null)
}

/** Al arrancar la app, recupera al usuario guardado (si sigue existiendo). */
export async function restaurarSesion() {
  const id = leerId()
  if (!id) return
  estado.cargando = true
  try {
    const usuario = await api.obtenerUsuario(id)
    await iniciarSesion(usuario)
  } catch (error) {
    // Si el usuario ya no existe (404) se olvida; otros errores se ignoran
    // para no bloquear la app si el backend no esta arriba.
    if (error.status === 404) cerrarSesion()
  } finally {
    estado.cargando = false
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

function guardarId(id) {
  try {
    if (id) localStorage.setItem(CLAVE_STORAGE, String(id))
    else localStorage.removeItem(CLAVE_STORAGE)
  } catch {
    // localStorage puede no estar disponible (modo privado); no pasa nada.
  }
}

function leerId() {
  try {
    return localStorage.getItem(CLAVE_STORAGE)
  } catch {
    return null
  }
}
