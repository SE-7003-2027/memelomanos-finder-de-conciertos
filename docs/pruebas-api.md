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

Después inicia la aplicación:

```bash
./mvnw spring-boot:run
```

Si el proyecto no tiene `mvnw`, se puede utilizar:

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

> Para las pruebas de favoritos se necesita conocer un `usuarioId` y un `conciertoId` existentes en la base de datos.

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
  "id": 1,
  "nombre": "Usuario de Prueba",
  "correo": "usuario.prueba@example.com",
  "favoritos": []
}
```

Guarda el `id` obtenido porque se utilizará para las pruebas de favoritos y de los endpoints por ID.

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

También se puede filtrar por artista:

```bash
curl "http://localhost:8080/api/v1/conciertos?artista=queen"
```

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

### 3.9 Eliminar un usuario

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

## 4. Probar con Postman

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

### Flujo recomendado

Ejecutar las solicitudes en este orden:

1. **Usuarios → Registrar usuario**
2. **Conciertos → Listar conciertos**
3. **Usuarios → Consultar perfil**
4. **Usuarios → Agregar favorito**
5. **Usuarios → Obtener favoritos del usuario**
6. **Usuarios → Quitar favorito**
7. **Usuarios → Obtener usuario por ID**
8. **Usuarios → Actualizar usuario**

Para las solicitudes que utilizan `usuarioId` y `conciertoId`, utiliza valores que existan realmente en la base de datos.

El endpoint **Eliminar usuario** se puede probar al final con un usuario destinado específicamente para esta prueba.

---

## 5. Probar con Insomnia

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

Para las solicitudes que utilizan IDs en la URL, reemplazar los valores de ejemplo por IDs existentes en la base de datos.

---

## 6. Códigos de respuesta esperados

| Endpoint                                 | Método | Éxito            | Posibles errores                                   |
| ---------------------------------------- | ------ | ---------------- | -------------------------------------------------- |
| `/usuarios/registro`                     | POST   | `201 Created`    | `400 Bad Request`, `409 Conflict`                  |
| `/conciertos`                            | GET    | `200 OK`         | —                                                  |
| `/usuarios/perfil`                       | GET    | `200 OK`         | `400 Bad Request`, `404 Not Found`                 |
| `/usuarios/favoritos`                    | POST   | `200 OK`         | `400 Bad Request`, `404 Not Found`                 |
| `/usuarios/{id}`                         | GET    | `200 OK`         | `404 Not Found`                                    |
| `/usuarios/{id}`                         | PUT    | `200 OK`         | `400 Bad Request`, `404 Not Found`, `409 Conflict` |
| `/usuarios/{id}`                         | DELETE | `204 No Content` | `404 Not Found`                                    |
| `/usuarios/{id}/favoritos`               | GET    | `200 OK`         | `404 Not Found`                                    |
| `/usuarios/{id}/favoritos/{conciertoId}` | DELETE | `204 No Content` | `404 Not Found`                                    |

### Descripción de los errores

**400 Bad Request**

La petición contiene datos faltantes, un formato incorrecto o JSON inválido.

**404 Not Found**

El recurso solicitado no existe, por ejemplo, cuando se consulta un usuario que no está registrado o se intenta eliminar un favorito que no pertenece al usuario.

**409 Conflict**

Se intenta registrar o actualizar un usuario utilizando un correo que ya está registrado por otro usuario.

---

## 7. Notas

Los endpoints `/usuarios/me/...` definidos en el contrato OpenAPI corresponden a funcionalidades que requieren autenticación mediante Bearer token y todavía no forman parte de los endpoints actuales utilizados en esta guía.

La colección de Postman debe actualizarse cuando se integren nuevos CRUDs o endpoints al proyecto.
