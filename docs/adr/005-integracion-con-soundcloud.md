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

El backend busca el perfil del artista en este orden y se queda con el
primero que funcione:

1. **URL guardada a mano.** Si algún concierto del artista tiene el campo
   opcional `soundcloudUrl`, le pide al endpoint público oEmbed
   (`https://soundcloud.com/oembed`, sin key) el reproductor embebible.
   Va primero porque una persona confirmó que es el artista correcto, y
   es la única forma de corregir una coincidencia automática equivocada.
   Se usaba como Plan B; desde #111 tiene prioridad sobre los demás.
2. **Plan A (con credenciales).** Si están definidas `SOUNDCLOUD_CLIENT_ID`
   y `SOUNDCLOUD_CLIENT_SECRET`, pide un token con Client Credentials
   (`POST https://secure.soundcloud.com/oauth/token` con autenticación
   HTTP Basic), lo guarda en memoria y lo reutiliza hasta un minuto antes
   de que venza. Con ese token busca al artista (`GET /users?q=`). Solo
   acepta resultados cuyo nombre coincide con el del artista (sin
   distinguir mayúsculas, acentos ni signos) y, si hay varios, elige el
   de más seguidores. Regresa su perfil, avatar y sus pistas más
   escuchadas (ordenadas por `playback_count`, máximo 5).
3. **Plan C (sin credenciales, #111).** Arma hasta 4 URLs de perfil a
   partir del nombre, en minúsculas, sin acentos y sin signos (para
   "Bad Bunny": `soundcloud.com/badbunny`, `/bad-bunny`, `/bad_bunny` y
   `/badbunnyofficial`), y las consulta una por una con oEmbed. Acepta el
   perfil solo si el `author_name` que regresa oEmbed coincide con el
   nombre del artista, y se detiene en la primera que coincide. Un `404`
   significa que la URL no existe y pasa a la siguiente; cualquier otra
   falla detiene la búsqueda, porque las demás fallarían igual.

Con oEmbed no hay lista de pistas: el widget ya muestra las del perfil.
La respuesta incluye `origen` (`manual`, `api` o `automatico`) para que la
interfaz sepa si la coincidencia la confirmó una persona. Si es
`automatico`, ofrece "¿No es este artista? Buscar en SoundCloud"; si no
se encontró, ofrece un botón "Buscar en SoundCloud" que abre la búsqueda
de personas en otra pestaña.

Las respuestas se guardan en una caché en memoria por artista: 24 horas
si se encontró el perfil y 6 horas si no, para no repetir las consultas
del Plan C en cada visita. Si SoundCloud falló, el "no encontrado" no se
guarda. La `soundcloudUrl` forma parte de la clave de la caché, así que
capturarla o corregirla se ve en la siguiente consulta. Si el artista no
tiene perfil o SoundCloud no responde, el endpoint regresa `404` con el
formato de error común, nunca un `500`.

Mientras el equipo confirma si alguien puede registrar la app, el sistema
funciona con la URL manual y el Plan C. Cuando haya credenciales basta con
definir las variables de entorno; no hay que cambiar código.

## Consecuencias

**Pros:**

- El secret se queda en el servidor y no aparece en ninguna respuesta.
- La funcionalidad sirve desde hoy sin depender de que alguien pague
  Artist Pro: con el Plan C la mayoría de los artistas importados de
  Ticketmaster tienen reproductor sin que nadie capture su URL.
- Reutilizar el token y la caché de 24 horas mantiene las peticiones a
  SoundCloud muy por debajo de sus límites.
- La interfaz no cambia entre un plan y otro: siempre recibe la misma
  forma de respuesta.

**Contras:**

- Los artistas cuyo perfil no está en una URL predecible (por ejemplo
  `soundcloud.com/badbunnypr`) siguen necesitando que alguien capture su
  `soundcloudUrl` a mano.
- **Riesgo de homónimos.** Con el Plan A o el Plan C se puede elegir a
  otro usuario que tiene exactamente el mismo nombre (un fan, un tributo,
  otro artista). Lo mitigamos exigiendo que el nombre coincida, prefiriendo
  al de más seguidores en el Plan A, marcando la respuesta como
  `automatico` para que la interfaz ofrezca buscar al correcto, y dejando
  que la `soundcloudUrl` capturada a mano siempre gane.
- Sin credenciales, un artista no encontrado cuesta hasta 4 consultas a
  oEmbed (cada una con su timeout). La caché de 6 horas lo limita a una
  vez por artista en ese tiempo.
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

### 3. Solo la URL capturada a mano (oEmbed)

**Pros:** no requiere credenciales ni manejo de tokens, y nunca elige al
artista equivocado.

**Contras:** no podemos buscar al artista ni mostrar sus pistas más
populares; todo depende de que alguien capture la URL, y casi ningún
concierto importado de Ticketmaster la tiene. Se mantiene como la opción
con más prioridad, no como la única.

### 4. Usar la búsqueda interna de soundcloud.com sin credenciales

**Pros:** encontraría perfiles con URLs poco predecibles.

**Contras:** requiere el `client_id` interno del sitio web, que no es
público y cambia sin aviso, y los términos de uso de la API prohíben el
scraping. Se descartó; en su lugar la interfaz enlaza a la búsqueda de
SoundCloud para que el usuario la haga él mismo.
