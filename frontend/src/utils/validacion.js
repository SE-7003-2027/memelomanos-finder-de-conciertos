// Validaciones basicas del lado del cliente. El backend sigue siendo la
// fuente de verdad (ADR-002); esto solo evita peticiones obviamente malas.

const PATRON_CORREO = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

export function esCorreoValido(correo) {
  return PATRON_CORREO.test(String(correo ?? '').trim())
}

/** Regresa un objeto con los errores por campo; vacio si todo esta bien. */
export function validarDatosUsuario({ nombre, correo }) {
  const errores = {}
  if (!String(nombre ?? '').trim()) {
    errores.nombre = 'Escribe tu nombre.'
  }
  if (!String(correo ?? '').trim()) {
    errores.correo = 'Escribe tu correo.'
  } else if (!esCorreoValido(correo)) {
    errores.correo = 'El correo no tiene un formato valido, por ejemplo nombre@correo.com.'
  }
  return errores
}
