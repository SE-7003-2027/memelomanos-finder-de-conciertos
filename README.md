# Finder de Conciertos

Proyecto de la materia Ingeniería en Software — Team 8 (memelomanos), UNAM

Repositorio: https://github.com/SE-7003-2027/memelomanos-finder-de-conciertos

## Equipo

- Buendía Elizalde Ricardo
- [Ochoa Campos Ana Sofía](https://github.com/soffochoa)
- Ramírez Palacios Miguel
- Tenorio Reyes Ihebel Luro
- Sánchez Soto Saúl

## Qué es este proyecto

Finder de Conciertos es una aplicación para buscar, descubrir y dar
seguimiento a conciertos y eventos musicales.

El proyecto apenas está arrancando, así que la mayoría de los features de
abajo siguen en desarrollo. Este README se va a ir actualizando conforme
avancemos, para que siempre refleje el estado real del proyecto.

### Features (en desarrollo)

- [ ] Búsqueda de conciertos por artista, ciudad o fecha
- [ ] Perfil de usuario
- [ ] (agregar aquí más features conforme se definan)

## Stack

- Backend: Java 21 + Spring (Boot), Maven
- Base de datos: por definir
- Frontend: por definir

## Cómo ejecutar el proyecto desde cero

Esta sección hay que mantenerla al corriente conforme agreguemos
herramientas, variables de entorno o pasos nuevos.

### Requisitos previos

- Java 21 (JDK)
- Maven 3.9+ (o usar el wrapper `./mvnw` incluido en el proyecto)
- Git
- (agregar Docker, base de datos, etc. conforme se definan)

### Pasos

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/SE-7003-2027/memelomanos-finder-de-conciertos.git
   cd memelomanos-finder-de-conciertos
   ```
2. Configurar las variables de entorno (archivo `.env` o
   `application.properties` local; ver `.env.example` cuando exista).
3. Instalar dependencias:
   ```bash
   ./mvnw install
   ```
4. Levantar el proyecto:
   ```bash
   ./mvnw spring-boot:run
   ```

## Documentación adicional

- Guía de estilo: `docs/guia-de-estilo.md`
- Decisiones de arquitectura (ADRs): `docs/adr/`
- Guía de contribución: `CONTRIBUTING.md`