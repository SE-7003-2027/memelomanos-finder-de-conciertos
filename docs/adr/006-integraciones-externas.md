# ADR-006: Arquitectura base para integraciones externas de conciertos

## Status

Propuesta

## Contexto

El sistema necesita incorporar información de conciertos proveniente de
distintos proveedores externos (como Ticketmaster y futuras integraciones
con boleteras o catálogos), además de coexistir con fuentes que hoy operan
con enlaces de búsqueda (Superboletos, Viagogo) y la integración de audio
con SoundCloud (ADR-005).

Sin una base común aparecen tres problemas:

1. **Acoplamiento a cada proveedor:** los servicios de negocio y el
   agregador tendrían que conocer los DTOs, esquemas de paginación, nombres
   de campos y excepciones HTTP de cada API externa.
2. **Duplicación de conciertos al sincronizar:** cada fuente usa su propio
   identificador de evento; si no se normaliza la identidad externa, volver
   a sincronizar crearía registros repetidos y rompería los favoritos de los
   usuarios.
3. **Riesgo de filtrar secretos y propagar fallas:** cada proveedor requiere
   sus propias credenciales y puede sufrir timeouts, límites de tasa (`429`)
   o caídas temporales que no deberían tirar la búsqueda de conciertos.

## Decisión

### 1. Patrón Adaptador con una interfaz común

Todo proveedor que consulte eventos externos implementa la interfaz
`ProveedorConciertos` en el paquete
`com.memelomanos.finderconciertos.integracion`:

- `List<ConciertoExterno> buscar(CriteriosBusqueda criterios)`
- `String nombre()`

Cada integración vive en su propio subpaquete dentro de `integracion/` (por
ejemplo, `integracion/ticketmaster/`) y actúa como un **adaptador**: traduce
los `CriteriosBusqueda` del proyecto a la petición específica de su API y
convierte la respuesta externa al record común `ConciertoExterno` (`fuente`,
`idExterno`, `artista`, `fecha`, `ciudad`, `lugar`, `urlCompra`,
`imagenUrl`, `latitud`, `longitud`, `precioMinimo`).

Así, el agregador y los servicios de sincronización pueden recibir una lista
de `ProveedorConciertos` inyectada por Spring y recorrerlos sin saber qué
proveedor concreto es cada uno. Agregar un proveedor nuevo solo requiere
crear una clase `@Component` que implemente `ProveedorConciertos`, sin
modificar el contrato interno ni a los demás adaptadores.

Para integraciones que no ofrecen una API abierta de catálogo (como
Superboletos y Viagogo en su Plan B de enlaces de búsqueda, o SoundCloud
para audio embebido en ADR-005), sus clientes siguen viviendo en sus
subpaquetes dentro de `integracion/` y pueden migrar a implementar
`ProveedorConciertos` si más adelante se obtiene acceso a sus APIs.

### 2. Persistencia idempotente en `Concierto`

La entidad `Concierto` incorpora `fuente`, `idExterno`, `urlCompra`,
`imagenUrl`, `latitud`, `longitud` y `precioMinimo`, con una restricción
única sobre `(fuente, id_externo)` (`uk_concierto_fuente_id_externo`). Esto
permite actualizar eventos importados sin duplicarlos y conservando los
favoritos existentes de los usuarios. Los campos son opcionales para seguir
admitiendo conciertos creados manualmente desde la API.

### 3. Manejo de API keys y configuración

- **Ninguna credencial se escribe en el código fuente ni en
  `application.yml`.**
- Cada proveedor se configura bajo `proveedores.<nombre>` en
  `application.yml` con al menos `habilitado` y sus credenciales leídas de
  variables de entorno (`TICKETMASTER_API_KEY`, `SUPERBOLETOS_API_KEY`,
  `VIAGOGO_API_KEY`, `SOUNDCLOUD_CLIENT_ID`, `SOUNDCLOUD_CLIENT_SECRET`).
- Todas las variables se documentan con valores de ejemplo en `.env.example`,
  mientras que `.env` permanece ignorado por Git (`.gitignore`).
- Si un proveedor tiene `habilitado: false` o su API key está vacía, no
  construye cliente HTTP ni intenta llamar a la red; el backend arranca y
  funciona normalmente en entornos locales y en CI sin requerir credenciales
  reales.
- Las credenciales nunca se exponen al frontend ni se escriben en los logs.

### 4. Comportamiento ante fallas de un proveedor

Las fallas de un proveedor externo se **aíslan** dentro de su adaptador:

- Cada cliente HTTP define timeouts acotados de conexión y lectura para no
  bloquear peticiones indefinidamente.
- Ante errores HTTP (`4xx`, `5xx`, `429`), problemas de red, timeouts o
  interrupciones, el adaptador registra una advertencia en el log (sin
  incluir la API key) y devuelve una lista vacía —o los resultados parciales
  recibidos antes del fallo— en lugar de propagar la excepción.
- De esta forma, si un proveedor cae, el agregador continúa consultando al
  resto de proveedores habilitados y el usuario sigue viendo resultados
  válidos sin recibir un error `500`.

## Consecuencias

**Pros:**

- Agregar o quitar un proveedor de conciertos no obliga a cambiar el modelo
  interno ni la lógica del agregador.
- Las pruebas unitarias pueden usar `ProveedorFalso` (en `src/test`) sin
  depender de red ni de credenciales reales.
- El riesgo de fugar secretos al repositorio o al navegador se reduce al
  mantenerlos exclusivamente en variables de entorno del servidor.
- Una caída o límite de tasa en un proveedor externo no degrada por completo
  la aplicación.

**Contras:**

- `ConciertoExterno` representa un mínimo común denominador: campos muy
  específicos de una boletera (como mapas de asientos o precios por sección)
  quedan fuera o requieren evolucionar el modelo común.
- Devolver lista vacía ante una falla puede ocultar problemas de
  configuración si no se revisan los logs del servidor.
- Mientras haya proveedores en Plan B (enlaces de búsqueda), coexisten dos
  estilos de integración dentro de `integracion/`.

## Alternativas consideradas

### 1. Llamar a cada proveedor directamente desde los servicios de negocio

**Pros:** menos interfaces y archivos al inicio cuando solo hay un proveedor.

**Contras:** el agregador y los servicios se llenan de condicionales por
proveedor, cada nueva API exige modificar clases existentes y es más difícil
escribir pruebas aisladas. Se descartó porque el proyecto contempla múltiples
fuentes.

### 2. Propagar excepciones de proveedores externos hasta el controlador

**Pros:** el cliente sabría inmediatamente que una fuente externa falló.

**Contras:** un problema temporal o un límite de cuota en un solo proveedor
rompería toda la búsqueda con errores `500` o `502` aunque haya datos locales
y otros proveedores disponibles. Se descartó en favor de degradación gradual
con registro en logs.

### 3. Guardar credenciales en archivos de configuración por perfil dentro del repo

**Pros:** más rápido para configurar el entorno local al clonar el proyecto.

**Contras:** alto riesgo de subir claves reales al historial de Git y rotar
secretos obliga a crear commits. Se descartó por seguridad; se usan
variables de entorno y `.env.example`.
