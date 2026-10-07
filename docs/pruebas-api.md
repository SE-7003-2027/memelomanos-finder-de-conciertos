# Guía de pruebas de la API

Esta guía explica cómo levantar el proyecto y probar los endpoints actuales de la API de Finder de Conciertos usando `curl`, Postman o Insomnia.

La API utiliza el prefijo:

```text
http://localhost:8080/api/v1
```

## 1. Levantar el proyecto

Primero, desde la raíz del proyecto, levanta la base de datos:

```bash
docker compose up -d
```

Si Docker muestra un error de permisos al acceder al socket `/var/run/docker.sock`, se puede utilizar:

```bash
sudo docker compose up -d
```

> Si ya tienes PostgreSQL instalado o un contenedor `postgres` usando el puerto `5432`, no utilices `docker compose`: sigue la sección 2 del README para conectarte a tu base de datos.

Después inicia la aplicación:

```bash
./mvnw spring-boot:run
```

En Windows se utiliza `mvnw.cmd spring-boot:run`. Si tienes Maven instalado, también se puede utilizar:

```bash
mvn spring-boot:run
```

La API estará disponible en:

```text
http://localhost:8080/api/v1
```

Swagger UI también está disponible en:

```text
http://localhost:8080/swagger-ui/index.html
```

### Datos de prueba

Al arrancar, la aplicación carga datos de prueba: 3 conciertos (Queen, Bad Bunny y Queen tribute band) y 2 usuarios. Por eso, en una base limpia los IDs `1`, `2` y `3` de conciertos y los IDs `1` y `2` de usuarios ya están ocupados: el primer usuario que registres tendrá el `id` `3`, no `1`.

Estos datos solo se insertan si no existen (los conciertos, si la tabla está vacía; los usuarios, si su correo no está registrado), así que reiniciar la aplicación con la misma base no los duplica. Si quieres empezar desde cero con la base de Docker:

```bash
docker compose down -v
docker compose up -d
```

> Si tu base se creó con una versión anterior y tiene usuarios con el mismo correo repetido, la aplicación puede fallar al arrancar (la columna `correo` ahora es única) o `GET /usuarios/perfil` puede responder `500`. Reinicia la base con los comandos anteriores.

---

## 2. Flujo básico de prueba

El flujo recomendado para comprobar las funciones principales de la API es:

1. Registrar un usuario.
2. Consultar la lista de conciertos.
3. Obtener el perfil del usuario.
4. Agregar un concierto a favoritos.
5. Consultar los favoritos del usuario.
6. Quitar el concierto de favoritos.
7. Consultar el usuario por ID.
8. Actualizar los datos del usuario.
9. Crear, consultar y actualizar un concierto.
10. Eliminar el concierto y el usuario creados para la prueba.

> Para las pruebas de favoritos se necesita conocer un `usuarioId` y un `conciertoId` existentes en la base de datos. Utiliza los `id` que devuelven el registro de usuario y la creación de concierto. Evita usar `1` a ciegas: ese ID pertenece a los datos de prueba, y las operaciones de actualizar y eliminar lo modificarían.

---

## 3. Probar con curl

### 3.1 Registrar un usuario

Endpoint:

```text
POST /api/v1/usuarios/registro
```

Comando:

```bash
curl -X POST http://localhost:8080/api/v1/usuarios/registro \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Usuario de Prueba",
    "correo": "usuario.prueba@example.com"
  }'
```

Respuesta esperada:

```text
201 Created
```

La respuesta contiene los datos del usuario creado y su `id`.

Ejemplo:

```json
{
  "id": 3,
  "nombre": "Usuario de Prueba",
  "correo": "usuario.prueba@example.com",
  "favoritos": []
}
```

Guarda el `id` obtenido porque se utilizará para las pruebas de favoritos y de los endpoints por ID. El valor real depende de tu base de datos (ver "Datos de prueba").

Si el correo ya está registrado:

```text
409 Conflict
```

Si falta algún dato o el correo no tiene un formato válido:

```text
400 Bad Request
```

---

### 3.2 Listar conciertos

Endpoint:

```text
GET /api/v1/conciertos
```

Comando:

```bash
curl http://localhost:8080/api/v1/conciertos
```

Respuesta esperada:

```text
200 OK
```

La respuesta es una lista de conciertos.

También se puede filtrar por artista, por ciudad o por ambos. La búsqueda encuentra coincidencias parciales e ignora mayúsculas y minúsculas; si se envían los dos filtros, se aplican los dos, y un filtro vacío se ignora.

```bash
curl "http://localhost:8080/api/v1/conciertos?artista=queen"
curl "http://localhost:8080/api/v1/conciertos?ciudad=cdmx"
curl "http://localhost:8080/api/v1/conciertos?artista=queen&ciudad=cdmx"
```

Con los datos de prueba, el primer comando devuelve los conciertos de `Queen` y de `Queen tribute band`.

---

### 3.3 Consultar el perfil

Endpoint:

```text
GET /api/v1/usuarios/perfil
```

Comando:

```bash
curl "http://localhost:8080/api/v1/usuarios/perfil?correo=usuario.prueba@example.com"
```

Respuesta esperada:

```text
200 OK
```

La respuesta contiene el perfil del usuario y su lista de favoritos.

Si no se envía el parámetro `correo`:

```text
400 Bad Request
```

Si no existe un usuario con ese correo:

```text
404 Not Found
```

---

### 3.4 Agregar un concierto a favoritos

Endpoint:

```text
POST /api/v1/usuarios/favoritos
```

Comando:

```bash
curl -X POST http://localhost:8080/api/v1/usuarios/favoritos \
  -H "Content-Type: application/json" \
  -d '{
    "usuarioId": 1,
    "conciertoId": 1
  }'
```

Reemplaza `1` por los IDs reales obtenidos durante las pruebas.

Respuesta esperada:

```text
200 OK
```

La respuesta contiene el usuario actualizado con el concierto agregado a favoritos.

Si el concierto ya estaba en los favoritos del usuario, la respuesta también es `200 OK` y el favorito no se duplica.

Si el usuario o el concierto no existen:

```text
404 Not Found
```

---

### 3.5 Consultar los favoritos de un usuario

Endpoint:

```text
GET /api/v1/usuarios/{id}/favoritos
```

Comando:

```bash
curl http://localhost:8080/api/v1/usuarios/1/favoritos
```

Reemplaza `1` por el `usuarioId` correspondiente.

Respuesta esperada:

```text
200 OK
```

La respuesta contiene la lista de conciertos favoritos del usuario.

---

### 3.6 Quitar un concierto de favoritos

Endpoint:

```text
DELETE /api/v1/usuarios/{id}/favoritos/{conciertoId}
```

Comando:

```bash
curl -X DELETE http://localhost:8080/api/v1/usuarios/1/favoritos/1
```

Reemplaza los valores por el `usuarioId` y `conciertoId` correspondientes.

Respuesta esperada:

```text
204 No Content
```

Si el concierto no se encuentra entre los favoritos del usuario, se espera:

```text
404 Not Found
```

También se espera `404 Not Found` si el usuario o el concierto no existen.

---

### 3.7 Consultar un usuario por ID

Endpoint:

```text
GET /api/v1/usuarios/{id}
```

Comando:

```bash
curl http://localhost:8080/api/v1/usuarios/1
```

Reemplaza `1` por el ID del usuario.

Respuesta esperada:

```text
200 OK
```

La respuesta contiene los datos del usuario.

Si el usuario no existe:

```text
404 Not Found
```

---

### 3.8 Actualizar un usuario

Endpoint:

```text
PUT /api/v1/usuarios/{id}
```

Comando:

```bash
curl -X PUT http://localhost:8080/api/v1/usuarios/1 \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Usuario Actualizado",
    "correo": "usuario.actualizado@example.com"
  }'
```

Reemplaza `1` por el ID del usuario que se desea actualizar.

Respuesta esperada:

```text
200 OK
```

Si los datos enviados no son válidos:

```text
400 Bad Request
```

Si el correo ya pertenece a otro usuario:

```text
409 Conflict
```

Si el usuario no existe:

```text
404 Not Found
```

---

### 3.9 Crear un concierto

Endpoint:

```text
POST /api/v1/conciertos
```

Comando:

```bash
curl -X POST http://localhost:8080/api/v1/conciertos \
  -H "Content-Type: application/json" \
  -d '{
    "artista": "Artista de Prueba",
    "fecha": "2027-03-15",
    "ciudad": "CDMX",
    "lugar": "Auditorio Nacional"
  }'
```

Los cuatro campos son obligatorios y no pueden estar vacíos. La fecha usa el formato `YYYY-MM-DD`. No se debe enviar `id` ni ningún otro campo fuera de estos cuatro: el servidor lo rechaza.

Respuesta esperada:

```text
201 Created
```

La respuesta contiene el concierto creado y su `id`.

Ejemplo:

```json
{
  "id": 4,
  "artista": "Artista de Prueba",
  "fecha": "2027-03-15",
  "ciudad": "CDMX",
  "lugar": "Auditorio Nacional"
}
```

Guarda el `id` obtenido para las pruebas de favoritos y de los endpoints por ID.

Si falta algún campo, está vacío, la fecha tiene un formato incorrecto o se envía un campo no permitido:

```text
400 Bad Request
```

---

### 3.10 Consultar un concierto por ID

Endpoint:

```text
GET /api/v1/conciertos/{id}
```

Comando:

```bash
curl http://localhost:8080/api/v1/conciertos/4
```

Reemplaza `4` por el ID del concierto.

Respuesta esperada:

```text
200 OK
```

La respuesta contiene los datos del concierto.

Si el concierto no existe:

```text
404 Not Found
```

---

### 3.11 Actualizar un concierto

Endpoint:

```text
PUT /api/v1/conciertos/{id}
```

Comando:

```bash
curl -X PUT http://localhost:8080/api/v1/conciertos/4 \
  -H "Content-Type: application/json" \
  -d '{
    "artista": "Artista de Prueba (editado)",
    "fecha": "2027-03-16",
    "ciudad": "Guadalajara",
    "lugar": "Auditorio Telmex"
  }'
```

Reemplaza `4` por el ID del concierto que se desea actualizar. Este endpoint reemplaza todos los datos editables, por lo que siempre se deben enviar los cuatro campos.

Respuesta esperada:

```text
200 OK
```

Si los datos enviados no son válidos:

```text
400 Bad Request
```

Si el concierto no existe:

```text
404 Not Found
```

---

### 3.12 Eliminar un concierto

Endpoint:

```text
DELETE /api/v1/conciertos/{id}
```

Comando:

```bash
curl -X DELETE http://localhost:8080/api/v1/conciertos/4
```

Reemplaza `4` por el ID del concierto que se desea eliminar.

Respuesta esperada:

```text
204 No Content
```

Antes de eliminar el concierto, la API lo quita de los favoritos de todos los usuarios que lo tenían guardado. Ambas operaciones se hacen en una misma transacción.

Si el concierto no existe:

```text
404 Not Found
```

> Se recomienda eliminar únicamente el concierto creado para la prueba, no los conciertos de los datos de prueba.

---

### 3.13 Eliminar un usuario

Endpoint:

```text
DELETE /api/v1/usuarios/{id}
```

Comando:

```bash
curl -X DELETE http://localhost:8080/api/v1/usuarios/1
```

Reemplaza `1` por el ID del usuario que se desea eliminar.

Respuesta esperada:

```text
204 No Content
```

Si el usuario no existe:

```text
404 Not Found
```

> Esta operación elimina al usuario, por lo que se recomienda realizarla únicamente al final de las pruebas o utilizando un usuario creado específicamente para probar este endpoint.

---

## 4. Probar casos de error

Todos los errores de la API devuelven el mismo formato de respuesta:

```json
{
  "status": 404,
  "error": "Not Found",
  "mensaje": "Usuario no encontrado",
  "timestamp": "2026-10-01T12:00:00"
}
```

El texto exacto de `mensaje` puede variar (por ejemplo, en los errores de validación depende del idioma de la JVM), pero `status` y `error` son siempre los del código HTTP. En los comandos de esta sección se usa la opción `-i` de `curl` para mostrar el código de respuesta.

### 4.1 Correo con formato inválido (400)

```bash
curl -i -X POST http://localhost:8080/api/v1/usuarios/registro \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Usuario de Prueba", "correo": "esto-no-es-un-correo"}'
```

Respuesta esperada: `400 Bad Request`.

### 4.2 Correo duplicado (409)

Ejecuta dos veces el comando de la sección 3.1 con el mismo correo. La primera respuesta es `201 Created` y la segunda `409 Conflict`.

### 4.3 Recurso inexistente (404)

```bash
curl -i http://localhost:8080/api/v1/usuarios/999999
curl -i http://localhost:8080/api/v1/conciertos/999999
```

Respuesta esperada en ambos: `404 Not Found`.

### 4.4 Datos faltantes o campos no permitidos (400)

Falta la fecha, la ciudad y el lugar:

```bash
curl -i -X POST http://localhost:8080/api/v1/conciertos \
  -H "Content-Type: application/json" \
  -d '{"artista": "Queen"}'
```

Se envía un campo que no pertenece al contrato (`id`):

```bash
curl -i -X POST http://localhost:8080/api/v1/conciertos \
  -H "Content-Type: application/json" \
  -d '{"id": 99, "artista": "Queen", "fecha": "2027-02-20", "ciudad": "CDMX", "lugar": "Auditorio Nacional"}'
```

Respuesta esperada en ambos: `400 Bad Request`.

### 4.5 JSON mal formado (400)

```bash
curl -i -X POST http://localhost:8080/api/v1/usuarios/registro \
  -H "Content-Type: application/json" \
  -d '{"nombre": '
```

Respuesta esperada: `400 Bad Request`.

### 4.6 Falta un parámetro obligatorio (400)

```bash
curl -i http://localhost:8080/api/v1/usuarios/perfil
```

Respuesta esperada: `400 Bad Request`, porque falta el parámetro `correo`.

---

## 5. Probar con Postman

La colección de Postman se encuentra en:

```text
docs/postman/finder-conciertos.postman_collection.json
```

Para importarla:

1. Abrir Postman.
2. Seleccionar **Import**.
3. Seleccionar el archivo `finder-conciertos.postman_collection.json`.
4. Importar la colección.
5. Abrir la colección **Finder de Conciertos API**.

La colección utiliza la variable:

```text
{{baseUrl}}
```

con el siguiente valor:

```text
http://localhost:8080/api/v1
```

También utiliza las variables:

```text
{{usuarioId}}
{{conciertoId}}
{{correo}}
```

Estas tres variables se llenan solas cuando la colección se ejecuta en orden: **Registrar usuario** genera un correo único y guarda el `correo` y el `usuarioId` que devuelve la API, y **Crear concierto** guarda el `conciertoId` del concierto creado. Además, cada solicitud verifica el código de respuesta esperado (pestaña **Test Results**).

### Flujo recomendado

Ejecutar las solicitudes en este orden, una por una o con **Run collection**:

1. **Conciertos → Listar conciertos**
2. **Conciertos → Buscar conciertos por artista**
3. **Conciertos → Buscar conciertos por ciudad**
4. **Conciertos → Crear concierto**
5. **Conciertos → Consultar concierto por ID**
6. **Conciertos → Actualizar concierto**
7. **Usuarios → Registrar usuario**
8. **Usuarios → Consultar perfil**
9. **Usuarios → Agregar favorito**
10. **Usuarios → Obtener favoritos del usuario**
11. **Usuarios → Quitar favorito**
12. **Usuarios → Obtener usuario por ID**
13. **Usuarios → Actualizar usuario**
14. **Usuarios → Eliminar usuario**
15. **Limpieza → Eliminar concierto**

Si ejecutas una solicitud suelta sin haber corrido antes **Registrar usuario** o **Crear concierto**, `usuarioId` y `conciertoId` conservan su valor inicial (`1`), que corresponde a los datos de prueba. Ten cuidado con **Actualizar usuario** y **Eliminar usuario**, porque modificarían o borrarían al primer usuario de prueba.

---

## 6. Probar con Insomnia

La API también puede probarse utilizando Insomnia.

Una opción es importar directamente el contrato OpenAPI ubicado en:

```text
src/main/resources/static/openapi.yml
```

En Insomnia:

1. Abrir **Import**.
2. Seleccionar el archivo `openapi.yml`.
3. Seleccionar la opción para importar una especificación OpenAPI.
4. Insomnia generará las solicitudes disponibles a partir del contrato.
5. Configurar la URL base como:

```text
http://localhost:8080/api/v1
```

Para las solicitudes que utilizan datos en el cuerpo, utilizar JSON.

Ejemplo para registrar un usuario:

```json
{
  "nombre": "Usuario de Prueba",
  "correo": "usuario.prueba@example.com"
}
```

Ejemplo para agregar un favorito:

```json
{
  "usuarioId": 1,
  "conciertoId": 1
}
```

Ejemplo para actualizar un usuario:

```json
{
  "nombre": "Usuario Actualizado",
  "correo": "usuario.actualizado@example.com"
}
```

Ejemplo para crear o actualizar un concierto:

```json
{
  "artista": "Artista de Prueba",
  "fecha": "2027-03-15",
  "ciudad": "CDMX",
  "lugar": "Auditorio Nacional"
}
```

Para las solicitudes que utilizan IDs en la URL, reemplazar los valores de ejemplo por IDs existentes en la base de datos.

---

## 7. Códigos de respuesta esperados

| Endpoint                                 | Método | Éxito            | Posibles errores                                   |
| ---------------------------------------- | ------ | ---------------- | -------------------------------------------------- |
| `/usuarios/registro`                     | POST   | `201 Created`    | `400 Bad Request`, `409 Conflict`                  |
| `/usuarios/perfil`                       | GET    | `200 OK`         | `400 Bad Request`, `404 Not Found`                 |
| `/usuarios/favoritos`                    | POST   | `200 OK`         | `400 Bad Request`, `404 Not Found`                 |
| `/usuarios/{id}`                         | GET    | `200 OK`         | `404 Not Found`                                    |
| `/usuarios/{id}`                         | PUT    | `200 OK`         | `400 Bad Request`, `404 Not Found`, `409 Conflict` |
| `/usuarios/{id}`                         | DELETE | `204 No Content` | `404 Not Found`                                    |
| `/usuarios/{id}/favoritos`               | GET    | `200 OK`         | `404 Not Found`                                    |
| `/usuarios/{id}/favoritos/{conciertoId}` | DELETE | `204 No Content` | `404 Not Found`                                    |
| `/conciertos`                            | GET    | `200 OK`         | —                                                  |
| `/conciertos`                            | POST   | `201 Created`    | `400 Bad Request`                                  |
| `/conciertos/{id}`                       | GET    | `200 OK`         | `404 Not Found`                                    |
| `/conciertos/{id}`                       | PUT    | `200 OK`         | `400 Bad Request`, `404 Not Found`                 |
| `/conciertos/{id}`                       | DELETE | `204 No Content` | `404 Not Found`                                    |

### Descripción de los errores

**400 Bad Request**

La petición contiene datos faltantes, un formato incorrecto, un campo que no pertenece al contrato, JSON inválido o le falta un parámetro obligatorio (por ejemplo, `correo` en `/usuarios/perfil`).

**404 Not Found**

El recurso solicitado no existe, por ejemplo, cuando se consulta un usuario o un concierto que no están registrados, o se intenta quitar un favorito que no pertenece al usuario.

**409 Conflict**

Se intenta registrar o actualizar un usuario utilizando un correo que ya está registrado por otro usuario.

Todas las respuestas de error tienen el mismo formato (`status`, `error`, `mensaje` y `timestamp`), descrito en la sección 4.

---

## 8. Notas

Los endpoints `/usuarios/me/...` definidos en el contrato OpenAPI (favoritos del usuario autenticado, artistas más escuchados y conciertos recomendados) corresponden a funcionalidades que requieren autenticación mediante Bearer token y todavía no están implementados, por lo que no forman parte de esta guía. Mientras tanto, los favoritos se manejan con los endpoints `/usuarios/favoritos` y `/usuarios/{id}/favoritos`, que reciben el `usuarioId` de forma explícita.

La colección de Postman debe actualizarse cuando se integren nuevos CRUDs o endpoints al proyecto.
