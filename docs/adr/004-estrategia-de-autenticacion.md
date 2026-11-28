# ADR-004: Estrategia de autenticación y autorización

## Status

Aceptada (2026-11-23)

## Contexto

Actualmente, el contrato OpenAPI define endpoints protegidos con tokens Bearer para consultar y modificar el perfil y los favoritos de los usuarios, así como endpoints para administrar conciertos (`POST`, `PUT`, `DELETE /api/v1/conciertos`) y ejecutar la sincronización manual (`/api/v1/admin/sincronizar`).

Con la incorporación de conciertos importados de proveedores externos (como Ticketmaster), permitir que cualquier usuario autenticado edite o elimine conciertos o dispare sincronizaciones representa un riesgo para la integridad del catálogo.

Necesitamos definir cómo se autentican los usuarios, qué roles existen, cómo viajan los roles en el JWT y cómo responde la API cuando falta sesión o permisos.

## Decisión

Los usuarios inician sesión con su correo electrónico y contraseña, y la API utiliza tokens JWT firmados (HMAC-SHA con `JJWT`) para comprobar su identidad y permisos en cada solicitud protegida.

Las contraseñas se guardan usando BCrypt (`PasswordEncoder`), nunca en texto plano. El backend comprueba las credenciales, genera el token JWT y valida su firma y vigencia en `JwtAuthenticationFilter`.

Para el cliente SPA, el token de acceso se mantiene en memoria en lugar de `localStorage` para evitar persistencia indefinida en el navegador (aunque no elimina por sí solo el riesgo de XSS y se pierde al recargar la página).

### Roles y permisos

La entidad `Usuario` incluye el campo `rol` (`RolUsuario` en formato `EnumType.STRING`, con valor por defecto `USUARIO`):

- `USUARIO` (rol por defecto en todo registro público): puede consultar y modificar su perfil y gestionar sus conciertos favoritos (`/api/v1/usuarios/**`). El endpoint de registro nunca acepta un rol desde el cliente.
- `ADMIN`: tiene todos los permisos de `USUARIO` y además puede crear, editar y eliminar conciertos (`POST /api/v1/conciertos`, `PUT /api/v1/conciertos/**`, `DELETE /api/v1/conciertos/**`) y acceder a los endpoints de administración (`/api/v1/admin/**`, incluido `POST /api/v1/admin/sincronizar`).

El rol se incluye en el claim `rol` del JWT al iniciar sesión (`JwtService`). En cada petición, `JwtAuthenticationFilter` lee ese claim (tratando tokens sin claim o con un valor desconocido como `USUARIO`) y construye la autoridad `ROLE_<ROL>` para Spring Security (`hasRole("ADMIN")`).

### Respuestas de error de seguridad

- **Rutas públicas:** registro, login, consultas `GET /api/v1/conciertos/**`, `GET /api/v1/artistas/**` y Swagger/OpenAPI.
- **Sin token o token inválido (`401 Unauthorized`):** la petición se rechaza en `AuthenticationEntryPoint`.
- **Con token válido pero sin rol `ADMIN` (`403 Forbidden`):** `ConfiguracionSeguridad` delega la `AccessDeniedException` al `HandlerExceptionResolver` para que `ManejadorGlobalDeExcepciones` responda `403 Forbidden` usando el formato común `ErrorRespuesta` (`status`, `error`, `mensaje`, `timestamp`).

En el perfil `dev`, los datos de prueba (`UsuarioDatosDePruebaConfig`) asignan el rol `ADMIN` a uno de los usuarios de prueba (`flanconsalsa67@gmail.com`) y `USUARIO` al otro (`pollosasados99@gmail.com`) para facilitar la verificación manual de ambos flujos.

## Consecuencias

**Pros:**

- Los JWT permiten seguir utilizando la autenticación mediante Bearer tokens que ya aparece en el contrato de la API y validar el rol sin consultar la base de datos en cada petición.
- BCrypt protege las contraseñas en la base de datos.
- Separar `USUARIO` y `ADMIN` protege el catálogo de conciertos y las tareas de sincronización contra modificaciones accidentales o no autorizadas.
- Delegar el `403` a `ManejadorGlobalDeExcepciones` mantiene consistente el formato `ErrorRespuesta` en toda la API.
- Los tokens emitidos antes de agregar roles se tratan de forma segura como `USUARIO`, nunca como `ADMIN`.

**Contras:**

- Al vivir el rol dentro del JWT, un cambio de rol en la base de datos surte efecto hasta que el usuario vuelve a iniciar sesión o expira su token (24 horas por defecto).
- Nosotros mantenemos el sistema de autenticación, expiración y autorización en el backend.
- Si el usuario recarga la página en la SPA, pierde el token en memoria y debe iniciar sesión nuevamente mientras no exista renovación de sesión.
- Es necesario proteger la clave `JWT_SECRET` mediante variables de entorno y no exponerla en el repositorio.

## Alternativas consideradas

### 1. Crear nuestra propia autenticación con JWT, correo y contraseña

**Pros:**

- Control total sobre el inicio de sesión, los claims del token y las reglas de autorización.
- Compatible con el contrato OpenAPI actual.
- Sin dependencia de terceros para autenticar usuarios.

**Contras:**

- Requiere mantener el manejo seguro de contraseñas, tokens y futuras funciones como recuperación de contraseña.

Se eligió esta opción porque encaja con el contrato actual y mantiene la arquitectura simple y autocontenida.

### 2. Utilizar OAuth2 para iniciar sesión con Google

**Pros:**

- Delega la verificación de credenciales a Google.

**Contras:**

- Añade registro externo, dependencia de un tercero y de todas formas requiere gestionar roles internos en el backend.

Se descartó para esta versión para evitar complejidad innecesaria.

### 3. Guardar el token en `localStorage`

**Pros:**

- Persiste tras recargar la página.

**Contras:**

- Un ataque XSS puede leer el token de forma persistente desde JavaScript.

Se descartó para limitar la exposición del token en el navegador.

### 4. Guardar el token en cookies `HttpOnly`

**Pros:**

- No accesible directamente desde JavaScript y configurable con `Secure` y `SameSite`.

**Contras:**

- Requiere protección adicional contra CSRF y adaptar el contrato actual basado en Bearer tokens.

Se descartó por ahora para mantener compatibilidad con el esquema `bearerAuth` del contrato OpenAPI.

### 5. No utilizar roles de usuario

**Pros:**

- Implementación inicial más sencilla.

**Contras:**

- Cualquier usuario autenticado podría modificar o borrar conciertos importados de proveedores externos y disparar sincronizaciones administrativas.

Se descartó; se adoptan los roles `USUARIO` y `ADMIN` con validación en el backend.
