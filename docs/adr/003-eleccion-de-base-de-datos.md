# ADR-003: PostgreSQL como base de datos

## Status

Aceptada

## Contexto

El sistema necesita persistir datos de conciertos y usuarios, con una
relación clara entre ellos (por ejemplo, un usuario puede marcar
varios conciertos como favoritos). Se consideraron una base de datos
NoSQL (MongoDB) y una relacional (PostgreSQL/MySQL).

## Decisión

Se decidió usar PostgreSQL como base de datos.

Los datos del proyecto (conciertos con campos fijos como artista,
fecha, ciudad, lugar; usuarios con relación a sus conciertos
favoritos) son datos estructurados con relaciones claras entre
entidades, que es el caso de uso natural para una base de datos
relacional. Se prefirió PostgreSQL sobre MySQL por ser open source,
tener buen soporte en el ecosistema de Spring (Spring Data JPA), y
ser ampliamente usado en la industria.

## Consecuencias

**Pros:**

- Las relaciones entre Usuario y Concierto (favoritos) se modelan de
  forma natural con llaves foráneas, sin necesidad de duplicar datos
- Spring Data JPA facilita el trabajo con PostgreSQL, con soporte
  maduro y bien documentado
- Permite hacer consultas complejas (por ejemplo, filtrar conciertos
  por varios criterios a la vez) de forma directa con SQL

**Contras:**

- Requiere definir un esquema fijo desde el inicio; cambiar la
  estructura de los datos más adelante implica migraciones
- El equipo no tiene tanta experiencia previa con PostgreSQL
  comparado con MongoDB