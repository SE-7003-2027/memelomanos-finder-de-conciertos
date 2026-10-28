# Finder de Conciertos

Proyecto de la materia Ingeniería en Software · Team 8 (memelomanos), UNAM

Repositorio: https://github.com/SE-7003-2027/memelomanos-finder-de-conciertos

## Equipo

- Ricardo Buendía
- [Sofía Ochoa](https://github.com/soffochoa)
- Miguel Ramírez
- Ihebel Tenorio
- Saúl Sánchez

## Qué es este proyecto

Finder de Conciertos es una aplicación web para buscar, descubrir y dar
seguimiento a conciertos y eventos musicales. Tiene un backend en
Spring Boot que expone una API REST y una interfaz web en Vue que la
consume.

Este README describe el estado real de `main`. Si agregas una
funcionalidad, una herramienta o un paso de instalación, actualízalo en
el mismo PR.

### Funcionalidades

| Funcionalidad | API | Interfaz |
| ------------- | :-: | :------: |
| Lista de conciertos | ✅ | Versión temporal (la reemplaza #50) |
| Detalle de un concierto | ✅ | En curso (#50) |
| Búsqueda de conciertos por artista y ciudad | ✅ | En curso (#50) |
| Búsqueda de conciertos por fecha | En curso (#53) | — |
| Alta, edición y baja de conciertos | ✅ | — |
| Registro, consulta, edición y baja de perfil | ✅ | ✅ |
| Marcar y quitar conciertos favoritos | ✅ | ✅ |
| Login con contraseña y token | En curso (#56, #57) | — |

## Stack

- **Backend:** Java 21 + Spring Boot 3.3 (API REST), Maven. Ver [ADR-001](docs/adr/001-eleccion-de-stack-backend.md).
- **Interfaz:** aplicación web (SPA) con Vue 3 + Vite, en la carpeta [`frontend/`](frontend/). Ver [ADR-002](docs/adr/002-eleccion-de-interfaz-grafica.md).
- **Base de datos:** PostgreSQL 16. Ver [ADR-003](docs/adr/003-eleccion-de-base-de-datos.md).

# Cómo ejecutar el proyecto desde cero

## Requisitos previos

- Java 21 (JDK). Comprueba con `java -version`.
- Node.js 20 o más reciente, para la interfaz. Comprueba con `node -v`.
- Git.
- Una de estas dos opciones para la base de datos:
  - Docker (Docker Desktop, Colima u OrbStack), o
  - PostgreSQL 16+ instalado en tu computadora.

No necesitas instalar Maven: el proyecto trae el wrapper `./mvnw`
(en Windows, `mvnw.cmd`).

## 1. Clonar el repositorio

```bash
git clone https://github.com/SE-7003-2027/memelomanos-finder-de-conciertos.git
cd memelomanos-finder-de-conciertos
```

## 2. Preparar la base de datos

El backend lee la conexión de tres variables de entorno: `DB_URL`,
`DB_USER` y `DB_PASSWORD`. Si no las defines, usa los valores del
contenedor de Docker del proyecto (`compose.yml`). Elige **una** de las
tres opciones.

### Opción A. Docker con el `compose.yml` del proyecto (la más sencilla)

Úsala si no tienes otro PostgreSQL usando el puerto `5432`.

1. Asegúrate de que Docker esté corriendo (abre Docker Desktop, o
   ejecuta `colima start` si usas Colima).
2. En la raíz del proyecto:

   ```bash
   docker compose up -d
   ```

3. Listo. **No configures variables de entorno**: el backend ya usa el
   usuario y la contraseña de este contenedor.

### Opción B. Ya tienes un contenedor `postgres` de otra materia

Si llevas Bases de Datos con el profesor Avilés, probablemente ya tienes
un contenedor llamado `postgres` en el puerto `5432`. **No uses la
opción A**, porque los dos contenedores chocarían en el mismo puerto.

1. Enciende tu contenedor:

   ```bash
   docker start postgres
   ```

2. Crea un usuario y la base de datos para este proyecto (solo la
   primera vez):

   ```bash
   docker exec -it postgres psql -U postgres
   ```

   ```sql
   CREATE USER app_conciertos WITH PASSWORD 'elige_una_password';
   CREATE DATABASE finder_conciertos OWNER app_conciertos;
   \q
   ```

3. Al levantar el backend (paso 3), pasa ese usuario y esa contraseña
   como variables de entorno.

### Opción C. PostgreSQL instalado en tu computadora

Crea una base de datos vacía llamada `finder_conciertos` (con pgAdmin o
`createdb finder_conciertos`) y, al levantar el backend, pasa tu usuario
y tu contraseña como variables de entorno.

## 3. Levantar el backend

Con la opción A:

```bash
./mvnw spring-boot:run
```

Por defecto, el backend utiliza el perfil `dev`, que carga los conciertos y usuarios de prueba al iniciar la aplicación. Para utilizar otro perfil, define la variable de entorno `SPRING_PROFILES_ACTIVE` con el nombre del perfil deseado.

Con las opciones B o C, pon tus datos antes del comando:

```bash
DB_URL=jdbc:postgresql://localhost:5432/finder_conciertos DB_USER=tu_usuario DB_PASSWORD=tu_password ./mvnw spring-boot:run
```

<details>
<summary>Cómo pasar las variables desde IntelliJ</summary>

1. Ve a **Run → Edit Configurations...**
2. Selecciona `FinderDeConciertosApplication`.
3. En **Environment variables** (si no aparece, actívalo en *Modify
   options*), pega tus datos separados por punto y coma:
   `DB_URL=jdbc:postgresql://localhost:5432/finder_conciertos;DB_USER=tu_usuario;DB_PASSWORD=tu_password`
4. **Apply** y ejecuta con el botón de Play.

</details>

El backend está listo cuando la terminal muestra
`Started FinderDeConciertosApplication`. Queda en el puerto `8080` y
todos los endpoints viven bajo `/api/v1`, por ejemplo
http://localhost:8080/api/v1/conciertos?artista=queen

Abrir `http://localhost:8080/` responde `404`; es normal, porque no hay
nada en la raíz. El contrato completo de la API, con un cliente para
probar cada endpoint, está en Swagger UI:
http://localhost:8080/swagger-ui/index.html

## 4. Levantar la interfaz

Con el backend corriendo, en **otra** terminal:

```bash
cd frontend
npm install
npm run dev
```

Abre http://localhost:5173. Vite reenvía al backend todo lo que empieza
con `/api`, así que no hace falta configurar nada más. Los detalles
están en [`frontend/README.md`](frontend/README.md).

Si algo falla, revisa [Problemas comunes](https://github.com/SE-7003-2027/memelomanos-finder-de-conciertos/wiki/Problemas-comunes)
en la wiki.

# Pruebas

### Pruebas automatizadas del backend

```bash
./mvnw test
```

Al terminar, la consola muestra un resumen de puntaje por categoría de
pruebas. Mientras no exista el login (#57),
`SeguridadApiTest.accesoAnonimo_Bloqueado` falla a propósito, así que el
comando termina con `BUILD FAILURE` aunque todo lo demás pase.

### Pruebas manuales de la API

- Guía para probar cada endpoint con `curl`, Postman o Insomnia:
  [`docs/pruebas-api.md`](docs/pruebas-api.md)
- Colección de Postman:
  [`docs/postman/finder-conciertos.postman_collection.json`](docs/postman/finder-conciertos.postman_collection.json)

# Documentación

| Documento | Qué contiene |
| --------- | ------------ |
| [`docs/PRD.md`](docs/PRD.md) | Problema, objetivos y alcance del producto |
| [`docs/arquitectura.md`](docs/arquitectura.md) | Componentes, capas, flujo de una petición y manejo de errores |
| [`docs/adr/`](docs/adr/) | Decisiones de arquitectura (ADRs) |
| [`docs/guia-de-estilo.md`](docs/guia-de-estilo.md) | Convenciones de código en Java y Vue |
| [`CONTRIBUTING.md`](CONTRIBUTING.md) | Cómo trabajamos: issues, ramas, commits, PRs y revisiones |
| [Wiki](https://github.com/SE-7003-2027/memelomanos-finder-de-conciertos/wiki) | Guías de inicio, referencia de la API, modelo de datos, estado del proyecto y problemas comunes |
