// Funciones para los endpoints de usuarios y favoritos (ver openapi.yml).
// Todos menos registro y login requieren token; el cliente lo agrega solo.
// Los endpoints de favoritos todavia reciben el usuarioId; cuando exista
// /usuarios/me/favoritos en el backend hay que cambiarlos aqui.
import { pedir } from './cliente'

/** POST /usuarios/registro -> usuario creado (201). 409 si el correo ya existe. */
export function registrarUsuario({ nombre, correo, password }) {
  return pedir('/usuarios/registro', {
    metodo: 'POST',
    cuerpo: { nombre, correo, password },
    publico: true,
  })
}

/** POST /usuarios/login -> { token }. 401 si el correo o la contraseña no coinciden. */
export function iniciarSesion({ correo, password }) {
  return pedir('/usuarios/login', {
    metodo: 'POST',
    cuerpo: { correo, password },
    publico: true,
  })
}

/** GET /usuarios/perfil?correo= -> usuario. 404 si no existe. */
export function obtenerPerfil(correo) {
  return pedir('/usuarios/perfil', { params: { correo } })
}

/** GET /usuarios/{id} */
export function obtenerUsuario(id) {
  return pedir(`/usuarios/${encodeURIComponent(id)}`)
}

/**
 * PUT /usuarios/{id} -> usuario actualizado. 409 si el correo es de otro usuario.
 * El contrato exige mandar la contraseña aunque el backend no la cambia.
 */
export function actualizarUsuario(id, { nombre, correo, password }) {
  return pedir(`/usuarios/${encodeURIComponent(id)}`, {
    metodo: 'PUT',
    cuerpo: { nombre, correo, password },
  })
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
