const MESES = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic']

/**
 * Convierte una fecha del backend ('2026-11-20') a texto legible ('20 nov 2026').
 * Se parsea a mano para que la zona horaria del navegador no la recorra un dia.
 */
export function formatearFecha(fechaIso) {
  if (!fechaIso) return ''
  const [anio, mes, dia] = String(fechaIso).split('-').map(Number)
  if (!anio || !mes || !dia) return String(fechaIso)
  return `${dia} ${MESES[mes - 1]} ${anio}`
}
