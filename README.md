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

- [ ] Búsqueda de conciertos por artista
- [ ] Visualización de la información básica de un concierto (artista, fecha, ciudad o lugar)
- [ ] Lista de conciertos disponibles
- [ ] Creación y consulta de un perfil de usuario
- [ ] Marcar conciertos de interés para el usuario

## Stack

- Backend: Java 21 + Spring Boot (API), Maven
- Interfaz: en evaluación — se está considerando Java Swing (ver ADR-002) para una app de escritorio que consuma la API
- Base de datos: PostgreSQL

## Cómo ejecutar el proyecto desde cero

Esta sección hay que mantenerla al corriente conforme agreguemos
herramientas, variables de entorno o pasos nuevos.

### Requisitos previos

- Java 21 (JDK)
- Maven 3.9+ 
- Git
- PostgreSQL 16+ 

### Pasos

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/SE-7003-2027/memelomanos-finder-de-conciertos.git
   cd memelomanos-finder-de-conciertos
   ```
2. Copiar el archivo de configuración de ejemplo:

   ```bash
   cp src/main/resources/application.properties.example \
      src/main/resources/application.properties
   ```
3. Configurar en `application.properties` los datos de conexión a PostgreSQL
   (usuario, contraseña, host y puerto si es necesario).
4. Compilar el proyecto:
   ```bash
   mvn clean install
   ```
5. Levantar el proyecto:
   ```bash
   mvn spring-boot:run
   ```

Si todo está configurado correctamente, el backend se levantará en el puerto
`8080` y establecerá la conexión con PostgreSQL.

Actualmente no hay endpoints disponibles, por lo que acceder 
a la raíz de la aplicación puede responder con un error `404 Not Found`.

## Documentación adicional

- Guía de estilo: `docs/guia-de-estilo.md`
- Decisiones de arquitectura (ADRs): `docs/adr/`
- Guía de contribución: `CONTRIBUTING.md`
- Se puede consultar la Wiki del repositorio para más información.