# ADR-005: Integración con SoundCloud para escuchar al artista

## Status

Propuesta

## Contexto

Queremos que, desde el detalle de un concierto, el usuario pueda escuchar
al artista sin salir de la página. Al principio el plan contemplaba
Spotify (de ahí los endpoints `/usuarios/me/artistas-top` del contrato),
pero el equipo decidió cambiarlo por SoundCloud (#81 y #82).

La API pública de SoundCloud tiene dos restricciones que condicionan el
diseño:

- **Registrar una app requiere Artist Pro.** Para obtener `client_id` y
  `client_secret` la cuenta que registra la app necesita una suscripción
  Artist Pro. No sabemos todavía si alguien del equipo la tiene.
- **Límite de tokens.** Con el flujo Client Credentials, SoundCloud
  entrega como máximo 50 tokens cada 12 horas por app (y 30 por hora por
  IP). Un token dura alrededor de una hora, así que pedir uno por
  petición agotaría el límite en minutos.

Además, el `client_secret` no puede llegar al navegador, y la vista del
concierto tiene que seguir funcionando aunque SoundCloud falle o el
artista no tenga perfil.

## Decisión

El backend es el único que habla con SoundCloud. La interfaz llama a
`GET /api/v1/artistas/{nombre}/soundcloud` y muestra el reproductor
embebido (widget de SoundCloud en un `iframe`) con lo que regrese.

El backend elige entre dos planes según la configuración:

- **Plan A (con credenciales).** Si están definidas `SOUNDCLOUD_CLIENT_ID`
  y `SOUNDCLOUD_CLIENT_SECRET`, pide un token con Client Credentials
  (`POST https://secure.soundcloud.com/oauth/token` con autenticación
  HTTP Basic), lo guarda en memoria y lo reutiliza hasta un minuto antes
  de que venza. Con ese token busca al artista (`GET /users?q=`), toma su
  perfil y avatar, y regresa sus pistas más escuchadas (ordenadas por
  `playback_count`, máximo 5).
- **Plan B (sin credenciales).** Si no hay credenciales, o si el Plan A
  falla, usa la URL de SoundCloud guardada en el campo opcional
  `soundcloudUrl` de los conciertos de ese artista y le pide al endpoint
  público oEmbed (`https://soundcloud.com/oembed`, sin key) el
  reproductor embebible. En este plan no hay lista de pistas: el widget
  ya muestra las del perfil.

En los dos planes la respuesta se guarda en una caché en memoria por
artista durante 24 horas. Si el artista no tiene perfil o SoundCloud no
responde, el endpoint regresa `404` con el formato de error común, nunca
un `500`.

Mientras el equipo confirma si alguien puede registrar la app, el sistema
funciona solo con el Plan B. Cuando haya credenciales basta con definir
las variables de entorno; no hay que cambiar código.

## Consecuencias

**Pros:**

- El secret se queda en el servidor y no aparece en ninguna respuesta.
- La funcionalidad sirve desde hoy con el Plan B, sin depender de que
  alguien pague Artist Pro.
- Reutilizar el token y la caché de 24 horas mantiene las peticiones a
  SoundCloud muy por debajo de sus límites.
- La interfaz no cambia entre un plan y otro: siempre recibe la misma
  forma de respuesta.

**Contras:**

- Con el Plan B alguien tiene que capturar a mano la URL de SoundCloud de
  cada artista (en `soundcloudUrl` al crear o editar el concierto).
- Con el Plan A la búsqueda por nombre puede encontrar a otro usuario con
  un nombre parecido. Se prefiere la coincidencia exacta, pero no siempre
  existe.
- La caché y el token viven en memoria: se pierden al reiniciar y no se
  comparten si algún día hay varias instancias del backend.
- Agregamos una dependencia externa que puede cambiar sus condiciones de
  uso.

## Alternativas consideradas

### 1. Spotify Web API

**Pros:** buena búsqueda de artistas y datos de popularidad.

**Contras:** la reproducción completa necesita que el usuario tenga
cuenta y conecte su sesión; el reproductor embebido solo da fragmentos a
quien no la tiene. Se descartó cuando el equipo cambió a SoundCloud.

### 2. Llamar a SoundCloud directo desde el navegador

**Pros:** menos código en el backend.

**Contras:** el Plan A necesita el `client_secret`, que no puede vivir en
el frontend, y no tendríamos una caché compartida entre usuarios. Se
descartó.

### 3. Solo Plan B (oEmbed)

**Pros:** no requiere credenciales ni manejo de tokens.

**Contras:** no podemos buscar al artista ni mostrar sus pistas más
populares; todo depende de que alguien capture la URL. Se mantiene como
respaldo, no como única opción.
