# Finder de Conciertos

Proyecto de la materia Ingeniería en Software  Team 8 (memelomanos), UNAM

Repositorio: https://github.com/SE-7003-2027/memelomanos-finder-de-conciertos

## Equipo

- Ricardo Buendía
- [Sofía Ochoa](https://github.com/soffochoa)
- Miguel Ramírez
- Ihebel Tenorio
- Saúl Sánchez

## Qué es este proyecto

Finder de Conciertos es una aplicación para buscar, descubrir y dar
seguimiento a conciertos y eventos musicales.

El proyecto apenas está arrancando, así que la mayoría de los features de
abajo siguen en desarrollo. Este README se va a ir actualizando conforme
avancemos, para que siempre refleje el estado real del proyecto.

### Features (en desarrollo)

Funcionalidades comprometidas por el equipo para el sprint actual:

- [x] Búsqueda de conciertos por artista
- [ ] Visualización de la información básica de un concierto (artista, fecha, ciudad o lugar)
- [ ] Lista de conciertos disponibles
- [ ] Creación y consulta de un perfil de usuario
- [ ] Marcar conciertos de interés para el usuario

## Stack

- Backend: Java 21 + Spring Boot (API), Maven
- Interfaz: en evaluación — el equipo está comparando Java Swing (app de escritorio), Thymeleaf (renderizado en el servidor) y un frontend desacoplado (React/Vue/Angular) — ver ADR-002 para el detalle de cada opción
- Base de datos: PostgreSQL

# Cómo ejecutar el proyecto desde cero

Esta sección hay que mantenerla al corriente conforme agreguemos
herramientas, variables de entorno o pasos nuevos.

## Requisitos previos

- Java 21 (JDK)
- Maven 3.9+ 
- Git
- PostgreSQL 16+ 

## Pasos

### 1. Clonar el repositorio:
   ```bash
   git clone https://github.com/SE-7003-2027/memelomanos-finder-de-conciertos.git
   cd memelomanos-finder-de-conciertos
   ```

### 2. Configuración de la Base de Datos Local
Este proyecto utiliza PostgreSQL y se conecta mediante variables de entorno para mantener las credenciales seguras. Dependiendo de tu entorno de desarrollo, sigue los pasos de la sección 1 o la sección 2.

#### 2.1. Agregar variables de entorno (Recomendado)

Utiliza este método si ya tienes un motor de base de datos funcionando en tu computadora y necesitas decirle a Spring Boot cómo conectarse a él.

#### 2.1.1 Si PostgreSQL está corriendo en local (Instalación nativa)
Si instalaste PostgreSQL directamente en tu computadora (por ejemplo, con el instalador oficial o pgAdmin), asegúrate de crear manualmente una base de datos vacía llamada `finder_conciertos`. Luego, inyecta tus credenciales usando uno de estos dos métodos:

**2.1.1.1 Agregar las variables de entorno en IntelliJ**
1. Ve al menú superior y selecciona **Run** > **Edit Configurations...**
2. Selecciona la aplicación (`FinderDeConciertosApplication`) en el panel izquierdo.
3. Busca el campo **Environment variables** (si no está visible, haz clic en *Modify options* y actívalo).
4. Pega tus credenciales separadas por punto y coma. Por ejemplo:
   `DB_URL=jdbc:postgresql://localhost:5432/finder_conciertos;DB_USER=mi_usuario_local;DB_PASSWORD=mi_password_local`
5. Haz clic en **Apply** y ejecuta el proyecto con el botón verde de Play.

**2.1.1.2 Pasarlas como parámetros de spring-boot:run**
Si ejecutas el proyecto desde la terminal usando Maven, pasa las variables justo antes del comando:
```bash
DB_URL=jdbc:postgresql://localhost:5432/finder_conciertos DB_USER=mi_usuario_local DB_PASSWORD=mi_password_local ./mvnw spring-boot:run
```

#### 2.1.2 Si PostgreSQL está en un contenedor (Caso para los alumnos de Avilés)
Si estás tomando la materia de Bases de Datos con el profesor Avilés, probablemente ya tienes un contenedor Docker llamado `postgres` ocupando el puerto `5432`. 

**No utilices la opción 2 (Docker Compose)** porque causará un choque de puertos. Reutiliza tu contenedor existente siguiendo estos pasos:

1. Enciende tu contenedor habitual:
   ```bash
   docker start postgres
   ```
2. Entra a la consola SQL dentro de tu contenedor:
   ```bash
   docker exec -it postgres psql -U postgres
   ```
3. Crea un usuario dedicado para este proyecto y la base de datos:
   ```sql
   CREATE USER app_conciertos WITH PASSWORD 'tu_password_seguro';
   CREATE DATABASE finder_conciertos OWNER app_conciertos;
   \q
   ```
4. Ahora que la base de datos existe, repite el paso **2.1.1.1** o **2.1.1.2** utilizando este nuevo usuario (`app_conciertos`) y la contraseña que elegiste.

---

#### 2.2. Docker (Entorno limpio)

Si no tienes PostgreSQL instalado en tu PC ni tienes contenedores previos configurados, puedes levantar todo el entorno automáticamente usando el archivo `compose.yml` del proyecto.

*( Advertencia: Si realizaste cualquier paso de la sección 1, ignora esta sección).*

1. Abre una terminal en la raíz del proyecto.
2. Ejecuta el siguiente comando para descargar y levantar la base de datos en segundo plano:
   ```bash
   docker compose up -d
   ```
3. Ejecuta tu aplicación en IntelliJ o terminal. **No necesitas configurar variables de entorno**, el proyecto se conectará usando los valores por defecto configurados para este contenedor.

### 3. Levantar el proyecto:
   ```bash
   mvn spring-boot:run
   ```

Si todo está configurado correctamente, el backend se levantará en el puerto
`8080` y establecerá la conexión con PostgreSQL.

Accediendo a la raíz (`http://localhost:8080/`) puede responder con un
error `404 Not Found`, eso es normal — no hay ningún endpoint ahí. Prueba
en su lugar un endpoint real, por ejemplo:

```
http://localhost:8080/conciertos?artista=queen
```

## Documentación adicional

- Guía de estilo: `docs/guia-de-estilo.md`
- Decisiones de arquitectura (ADRs): `docs/adr/`
- Guía de contribución: `CONTRIBUTING.md`
- Se puede consultar la Wiki del repositorio para más información.