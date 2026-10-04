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

El flujo recomendado para comprobar que las funciones principales trabajan correctamente es:

1. Registrar un usuario.
2. Consultar la lista de conciertos.
3. Obtener el perfil del usuario.
4. Agregar un concierto a favoritos.
5. Consultar nuevamente el perfil para comprobar el favorito.

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

Guarda el `id` obtenido porque se utilizará para agregar favoritos.

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

### 3.5 Comprobar el favorito

Después de agregar el concierto, vuelve a consultar el perfil:

```bash
curl "http://localhost:8080/api/v1/usuarios/perfil?correo=usuario.prueba@example.com"
```

En la propiedad `favoritos` debe aparecer el concierto agregado.

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

### Flujo recomendado

Ejecutar las solicitudes en este orden:

1. **Usuarios → Registrar usuario**
2. **Conciertos → Listar conciertos**
3. **Usuarios → Consultar perfil**
4. **Usuarios → Agregar favorito**
5. **Usuarios → Consultar perfil**

Para `Agregar favorito`, utiliza un `usuarioId` y un `conciertoId` que existan realmente en la base de datos.

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

---

## 6. Códigos de respuesta esperados

| Endpoint              | Método | Éxito         | Posibles errores                   |
| --------------------- | ------ | ------------- | ---------------------------------- |
| `/usuarios/registro`  | POST   | `201 Created` | `400 Bad Request`, `409 Conflict`  |
| `/conciertos`         | GET    | `200 OK`      | —                                  |
| `/usuarios/perfil`    | GET    | `200 OK`      | `400 Bad Request`, `404 Not Found` |
| `/usuarios/favoritos` | POST   | `200 OK`      | `400 Bad Request`, `404 Not Found` |

### Descripción de los errores

**400 Bad Request**

La petición contiene datos faltantes, un formato incorrecto o JSON inválido.

**404 Not Found**

El recurso solicitado no existe, por ejemplo, cuando se consulta un usuario que no está registrado.

**409 Conflict**

Se intenta registrar un usuario utilizando un correo que ya está registrado.

---

## 7. Notas

Los endpoints `/usuarios/me/...` definidos en el contrato OpenAPI corresponden a funcionalidades que requieren autenticación y todavía no forman parte de los endpoints actuales utilizados en esta guía.

La colección de Postman debe actualizarse cuando se integren nuevos CRUDs o endpoints al proyecto.
