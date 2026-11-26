# ADR-007: Sincronización y disponibilidad de conciertos

## Status

Propuesta

## Contexto

Ticketmaster ya alimenta el catálogo de conciertos. Consultar su API en cada
búsqueda haría que los tiempos de respuesta del sitio dependieran del proveedor
y gastaría su cuota con cada visita. También necesitamos distinguir el ID de
un evento en su fuente de la identidad del concierto dentro de la aplicación,
que usan el detalle y los favoritos.

SoundCloud tiene su propio [ADR-005](./005-integracion-con-soundcloud.md).
Superboletos y Viagogo ofrecen por ahora enlaces de búsqueda; esos enlaces no
aportan un catálogo para sincronizar.

## Decisión

### Sincronización e identidad

El backend sincroniza Ticketmaster cada seis horas por defecto, con un horario
configurable. Este intervalo permite refrescar el catálogo varias veces al día
sin consumir cuota por cada búsqueda. Las búsquedas y el detalle consultan
PostgreSQL. Las llamadas HTTP se hacen antes de abrir la transacción de guardado.

Cada evento importado se identifica por `(fuente, idExterno)` y esa pareja
tiene una restricción única en la base. Si el evento ya existe, se actualiza
el mismo registro y conserva su ID interno, de modo que los favoritos siguen
apuntando al concierto correcto.

### Reglas comunes y fallas de sincronización

El [ADR-006 de arquitectura base](https://github.com/SE-7003-2027/memelomanos-finder-de-conciertos/pull/114)
define el contrato `ProveedorConciertos`, las credenciales en variables de
entorno y cómo se aíslan los errores de cada proveedor. Los enlaces de búsqueda
de Superboletos y Viagogo y el reproductor de SoundCloud quedan fuera de la
sincronización del catálogo.

En Ticketmaster, el cliente deja al menos 250 ms entre peticiones. Si falla
una página, registra una advertencia y conserva los eventos ya recibidos. Si
falla antes de recibir resultados, devuelve una lista vacía. En ambos casos,
la búsqueda sigue usando los datos guardados en PostgreSQL.

### Deduplicación entre fuentes

Para el agregador del [#85](https://github.com/SE-7003-2027/memelomanos-finder-de-conciertos/issues/85),
se propone agrupar por artista normalizado, fecha del evento y ciudad
normalizada. Al comparar artista y ciudad se recortan espacios y se ignoran
mayúsculas y acentos. Si falta alguno de los tres datos, se conserva la
identidad de origen y no se agrupa por coincidencia parcial. Un concierto
agrupado puede conservar una opción de compra por proveedor.

Esta agrupación es distinta de la restricción única por fuente e ID externo.
La clave de artista, fecha y ciudad puede agrupar dos funciones del mismo
artista en un solo día; si sucede, habrá que evaluar el recinto o la hora para
distinguirlas.

### Cancelaciones y eventos ausentes

Para el [#103](https://github.com/SE-7003-2027/memelomanos-finder-de-conciertos/issues/103), se propone
marcar como no disponible un evento con cancelación explícita o un concierto
vigente que desaparece de una sincronización completa y no vacía de su fuente.
Se conserva el registro y se excluye de la búsqueda. Si ya era favorito,
sigue visible en el perfil con su estado.

El [PR #122](https://github.com/SE-7003-2027/memelomanos-finder-de-conciertos/pull/122)
considera completa la descarga de Ticketmaster si llega a la última página,
la numeración y los totales de paginación son coherentes, y se reciben tantos
eventos como indica `totalElements`, todos con un ID válido y sin repeticiones.

Si falla una página o se alcanza el límite de 1,000 eventos antes de completar
la descarga, se actualizan los eventos recibidos y se conserva el estado de
los ausentes. Un catálogo vacío tampoco cambia la disponibilidad de los
conciertos guardados, aunque la API indique que terminó de responder.

## Consecuencias

**Pros:**

- La búsqueda funciona con los datos guardados aunque un proveedor no responda.
- Se reduce el uso de la cuota y se conservan los favoritos al actualizar eventos.
- Las distintas fuentes pueden ofrecer opciones de compra para el mismo concierto.

**Contras:**

- La búsqueda refleja la última sincronización; los cambios pueden tardar en verse.
- Hay que almacenar el catálogo y ejecutar la sincronización periódica.
- Para detectar bajas por ausencia se necesita una descarga completa y no vacía.

## Alternativas consideradas

- **Consultar en vivo en cada búsqueda.** Los resultados serían más recientes,
  pero aumentarían la latencia y el uso de cuota; una caída dejaría al usuario
  sin resultados de esa fuente.
- **Sincronizar cada hora.** Reduciría el retraso con que se ven las novedades,
  a cambio de hacer más peticiones. Se conserva un intervalo configurable de
  seis horas como punto de partida.
- **Borrar eventos que ya no llegan.** Simplificaría la búsqueda, pero podría
  perder favoritos y confundir una descarga incompleta con una baja real.
