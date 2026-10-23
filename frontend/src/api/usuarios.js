// Funciones para los endpoints de usuarios y favoritos (ver openapi.yml).
// Los endpoints de favoritos reciben el usuarioId porque todavia no hay
// login; cuando se cierre #57 cambiaran a /usuarios/me/favoritos.
import { pedir } from './cliente'

/** POST /usuarios/registro -> usuario creado (201). 409 si el correo ya existe. */
export function registrarUsuario({ nombre, correo }) {
  return pedir('/usuarios/registro', { metodo: 'POST', cuerpo: { nombre, correo } })
}

/** GET /usuarios/perfil?correo= -> usuario. 404 si no existe. */
export function obtenerPerfil(correo) {
  return pedir('/usuarios/perfil', { params: { correo } })
}

/** GET /usuarios/{id} */
export function obtenerUsuario(id) {
  return pedir(`/usuarios/${encodeURIComponent(id)}`)
}

/** PUT /usuarios/{id} -> usuario actualizado. 409 si el correo es de otro usuario. */
export function actualizarUsuario(id, { nombre, correo }) {
  return pedir(`/usuarios/${encodeURIComponent(id)}`, { metodo: 'PUT', cuerpo: { nombre, correo } })
}

/** DELETE /usuarios/{id} (204) */
export function eliminarUsuario(id) {
  return pedir(`/usuarios/${encodeURIComponent(id)}`, { metodo: 'DELETE' })
}

/** GET /usuarios/{id}/favoritos -> lista de conciertos */
export function obtenerFavoritos(usuarioId) {
  return pedir(`/usuarios/${encodeURIComponent(usuarioId)}/favoritos`)
}

/** POST /usuarios/favoritos -> usuario con su lista de favoritos actualizada */
export function agregarFavorito(usuarioId, conciertoId) {
  return pedir('/usuarios/favoritos', { metodo: 'POST', cuerpo: { usuarioId, conciertoId } })
}

/** DELETE /usuarios/{id}/favoritos/{conciertoId} (204) */
export function quitarFavorito(usuarioId, conciertoId) {
  return pedir(
    `/usuarios/${encodeURIComponent(usuarioId)}/favoritos/${encodeURIComponent(conciertoId)}`,
    { metodo: 'DELETE' },
  )
}
