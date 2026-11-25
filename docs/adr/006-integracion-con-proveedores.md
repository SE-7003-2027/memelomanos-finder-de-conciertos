# ADR-006: Integración con proveedores externos y sincronización

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

### Contrato para proveedores de conciertos

La propuesta del [#77](https://github.com/SE-7003-2027/memelomanos-finder-de-conciertos/issues/77) define
`ProveedorConciertos` para los adaptadores que suministran eventos. Cada uno
identifica su fuente con `nombre()` y devuelve una lista de `ConciertoExterno`
desde `buscar(CriteriosBusqueda criterios)`. Los enlaces de búsqueda de
Superboletos y Viagogo y el reproductor de SoundCloud quedan fuera de este
contrato mientras no suministren eventos para el catálogo.

### Fallas y credenciales

Cada adaptador respeta la cuota de su proveedor y aplica tiempos de espera.
Ticketmaster deja al menos 250 ms entre peticiones. Si falla una página, se
registra una advertencia y se conservan los eventos ya recibidos. Si falla
antes de recibir resultados, se devuelve una lista vacía. El fallo de un
proveedor no debe provocar un HTTP 500 en la aplicación ni impedir que se
procesen resultados de otras fuentes.

Las credenciales se leen de variables de entorno, como
`TICKETMASTER_API_KEY`. No se guardan en el repositorio ni se escriben en los
logs. Ticketmaster queda deshabilitado si no hay clave o si la configuración
lo apaga.

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
marcar como no disponible un evento con cancelación explícita o que desaparece
de una sincronización completa de su fuente. Se conserva el registro y se
excluye de la búsqueda. Si ya era favorito, sigue visible en el perfil con su
estado.

Una respuesta parcial, con error, limitada en páginas o que cubre solo un
subconjunto del catálogo sirve para actualizar los conciertos recibidos. La
ausencia de un evento en esa respuesta no demuestra que fue retirado. Antes
de aplicar la regla por ausencia, la sincronización debe poder distinguir
una descarga completa de una parcial. La lista que devuelve actualmente
Ticketmaster no comunica por sí sola esa diferencia.

## Consecuencias

**Pros:**

- La búsqueda funciona con los datos guardados aunque un proveedor no responda.
- Se reduce el uso de la cuota y se conservan los favoritos al actualizar eventos.
- Las distintas fuentes pueden ofrecer opciones de compra para el mismo concierto.

**Contras:**

- La búsqueda refleja la última sincronización; los cambios pueden tardar en verse.
- Hay que almacenar el catálogo y ejecutar la sincronización periódica.
- Para detectar bajas por ausencia hace falta confirmar que la descarga fue completa.

## Alternativas consideradas

- **Consultar en vivo en cada búsqueda.** Los resultados serían más recientes,
  pero aumentarían la latencia y el uso de cuota; una caída dejaría al usuario
  sin resultados de esa fuente.
- **Sincronizar cada hora.** Reduciría el retraso con que se ven las novedades,
  a cambio de hacer más peticiones. Se conserva un intervalo configurable de
  seis horas como punto de partida.
- **Borrar eventos que ya no llegan.** Simplificaría la búsqueda, pero podría
  perder favoritos y confundir una descarga incompleta con una baja real.
