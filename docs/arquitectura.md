# Arquitectura del sistema

## Visión general

El sistema se divide en dos partes que se comunican por HTTP (ver
ADR-001, ADR-002 y ADR-003):

```
+----------------------+     HTTP/JSON      +-------------------------+
|   Interfaz web        | <----------------> |   Backend (Spring Boot) |
|   (SPA con Vue)       |      /api/v1       |   expone una API REST   |
+----------------------+                    +-------------------------+
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
  (Concierto, Usuario) y los cuerpos de las peticiones
  (RegistroRequest, FavoritoRequest, ConciertoRequest).
- **Exception**: las excepciones del dominio y el manejador global que
  las traduce a respuestas HTTP con un formato de error común.

## Estructura de paquetes

```
com.memelomanos.finderconciertos
├── FinderDeConciertosApplication.java   (clase principal de Spring Boot)
├── config/        (prefijo /api/v1 y datos de prueba)
├── controller/    (endpoints REST)
├── service/       (lógica de negocio)
├── repository/    (acceso a datos)
├── model/         (entidades del dominio y cuerpos de petición)
└── exception/     (excepciones del dominio y manejador global)
```

## Contrato de la API

El contrato de la API vive en `src/main/resources/static/openapi.yml`
y es el acuerdo entre el backend y la interfaz: si un endpoint cambia,
primero se cambia el contrato. Swagger UI lo muestra en
`http://localhost:8080/swagger-ui/index.html`.

## Interfaz (SPA con Vue)

La interfaz es una aplicación web de una sola página construida con
Vue 3 y Vite (ver ADR-002). Vivirá en una carpeta `frontend/` dentro de
este mismo repositorio (todavía por crear) y consume la API únicamente
por HTTP — no tiene acceso directo a la base de datos ni a las clases
del backend.
