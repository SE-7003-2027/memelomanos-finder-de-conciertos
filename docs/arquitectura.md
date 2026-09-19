# Arquitectura del sistema

## Visión general

El sistema se divide en dos partes que se comunican por HTTP (ver
ADR-001 y ADR-002):

```
+------------------+       HTTP/JSON       +-------------------------+
|   Interfaz        | <--------------------> |   Backend (Spring Boot) |
|   (Java Swing)     |                       |   expone una API REST   |
+------------------+                        +-------------------------+
                                                        |
                                                        v
                                             +-------------------------+
                                             |   Base de datos          |
                                             |   (PostgreSQL)           |
                                             +-------------------------+
```

## Capas del backend

El backend sigue una arquitectura en capas (layered architecture),
para separar responsabilidades:

- **Controller**: recibe las peticiones HTTP y las traduce a llamadas
  al Service. No contiene lógica de negocio.
- **Service**: contiene la lógica de negocio (por ejemplo, cómo se
  busca un concierto, cómo se valida un perfil de usuario).
- **Repository**: se encarga de guardar y consultar datos, sin
  importarle de dónde vienen las peticiones ni por qué se piden.
- **Model**: las clases que representan los datos del dominio
  (Concierto, Usuario, etc. — se definen a detalle en el issue de
  modelos de datos).

## Estructura de paquetes propuesta

```
com.memelomanos.finderconciertos
├── FinderDeConciertosApplication.java   (clase principal de Spring Boot)
├── controller/    (endpoints REST)
├── service/       (lógica de negocio)
├── repository/    (acceso a datos)
└── model/         (entidades del dominio)
```

Esta estructura es la base; los issues de modelos de datos (#12) y
contrato de API (#13) van a llenar el contenido específico de
`model/` y `controller/` respectivamente.

## Interfaz (Swing)

La interfaz vive en un proyecto/módulo separado del backend (o en un
paquete separado dentro del mismo repo, por definir), y consume la
API únicamente por HTTP — no tiene acceso directo a la base de datos
ni a las clases del backend.
