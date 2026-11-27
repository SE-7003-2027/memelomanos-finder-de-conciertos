# ADR-008: Migraciones de base de datos con Flyway

## Status

Propuesta

## Contexto

Hasta ahora el esquema lo generaba Hibernate con
`spring.jpa.hibernate.ddl-auto: update`. Ese modo agrega tablas y columnas
nuevas, pero:

- no borra ni renombra columnas y no cambia tipos ni longitudes;
- no deja historial de qué cambió ni cuándo;
- cada base local queda como la dejó la última rama que la usó. Por ejemplo,
  quien probó una rama con una columna que luego se renombró conserva la
  columna vieja.

`Concierto` ya cambió en #90 y #98, y va a seguir cambiando con cada
proveedor (#85, #103) y con los roles de usuario (#107). Además, todas las
pruebas eran `@WebMvcTest` o unitarias: el CI nunca revisaba que el esquema
funcionara con PostgreSQL.

## Decisión

Usamos **Flyway** para crear y cambiar el esquema:

- Las migraciones son archivos SQL en `src/main/resources/db/migration`,
  con el nombre `V<número>__<descripcion>.sql`. Flyway las aplica en orden
  al arrancar la aplicación y anota cada una en la tabla
  `flyway_schema_history`.
- `V1__esquema_inicial.sql` crea el esquema que ya teníamos: `concierto`,
  `usuario`, `usuario_favoritos` y la restricción
  `uk_concierto_fuente_id_externo`.
- Hibernate pasa a `ddl-auto: validate`: ya no cambia la base, solo revisa al
  arrancar que las entidades coincidan con ella. Si falta una migración, la
  aplicación no arranca, en lugar de fallar más tarde con un error de SQL.
- Una migración que ya está en `main` no se edita: cualquier cambio va en una
  migración nueva. Flyway guarda el checksum de cada archivo y se niega a
  arrancar si uno ya aplicado cambió.
- El CI levanta PostgreSQL 16 (la misma imagen que `compose.yml`) y corre
  `EsquemaBaseDeDatosTest`, que arranca la aplicación completa: así cada PR
  aplica todas las migraciones y valida las entidades.

Elegimos Flyway porque las migraciones son SQL normal, que todo el equipo ya
escribe en la materia de Bases de Datos, y porque Spring Boot lo configura
solo: basta con agregar la dependencia.

## Consecuencias

**Pros:**

- El esquema de cada base sale de los mismos archivos, en el mismo orden, y
  se puede reconstruir desde cero.
- Cada cambio al esquema se revisa en el PR como SQL y queda en el historial.
- El CI detecta en el PR una entidad que no coincide con las migraciones.
- Se pueden hacer cambios que `update` no hacía: renombrar o borrar columnas,
  cambiar tipos y migrar datos existentes.

**Contras:**

- Cambiar una entidad ya no basta: hay que escribir su migración en el mismo
  PR (regla en el CONTRIBUTING).
- Si dos PRs abiertos crean migraciones con el mismo número, el que se mergea
  después tiene que renumerar la suya antes de rebasar.
- Las bases locales creadas antes de este cambio no tienen
  `flyway_schema_history`. Hay que recrearlas o marcarlas una vez con un
  baseline (el README explica cómo).
- `EsquemaBaseDeDatosTest` necesita PostgreSQL, así que en local solo corre
  si se define `DB_URL`.

## Alternativas consideradas

### Liquibase

**Pros:** las migraciones se pueden escribir en YAML o XML independientes de
la base, y tiene rollbacks y precondiciones.

**Contras:** es más complejo de configurar y de leer en un PR, y la
portabilidad entre motores no nos sirve porque ya elegimos PostgreSQL
(ADR-003). Los rollbacks automáticos no compensan esa complejidad en un
proyecto de este tamaño.

### Seguir con `ddl-auto: update`

**Pros:** no hay que escribir nada: Hibernate crea las columnas nuevas solo.

**Contras:** no renombra, no borra ni cambia tipos, no deja historial y deja
cada base local distinta. Los problemas aparecen tarde y solo en algunas
computadoras.
