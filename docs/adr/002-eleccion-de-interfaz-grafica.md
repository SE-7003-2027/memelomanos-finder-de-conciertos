# ADR-002: Aplicación web desacoplada (SPA con Vue) como interfaz gráfica

## Status

Aceptada

## Contexto

El backend del proyecto (ver ADR-001) usa Java + Spring Boot y ya
expone una API REST bajo `/api/v1`, descrita con un contrato OpenAPI
(`src/main/resources/static/openapi.yml`) y documentada en Swagger UI.
Faltaba definir cómo va a interactuar el usuario con esa API. Se
consideraron tres caminos: una aplicación de escritorio (Java Swing),
vistas renderizadas en el servidor (Thymeleaf) y un frontend web
desacoplado (React, Vue o Angular).

Para decidir se tomaron en cuenta estos factores:

- **El producto es un buscador.** El PRD busca que cualquier persona
  pueda encontrar conciertos por artista, ciudad o fecha, y ver "qué
  hay disponible cerca de mí". Un buscador se consulta desde cualquier
  dispositivo, idealmente sin instalar nada.
- **La API ya está pensada para clientes externos.** Tiene contrato
  OpenAPI, versionado (`/api/v1`), un formato de error común y
  endpoints `/usuarios/me/...` planeados con autenticación Bearer
  (JWT), que es el esquema típico de un cliente separado del servidor.
- **El equipo es de 5 personas con disponibilidad limitada** (ver PRD)
  y la experiencia previa está concentrada en Java/Spring (ver
  ADR-001). Cualquier tecnología nueva tiene que mantenerse pequeña.

## Decisión

La interfaz será una **aplicación web de una sola página (SPA)**
construida con **Vue 3 y Vite**, separada del backend, que consume la
API REST de Spring Boot únicamente por HTTP/JSON.

- Toda la lógica de negocio, las validaciones y el acceso a datos
  siguen viviendo en el backend. La SPA solo presenta información
  (búsqueda, lista de conciertos, perfil, favoritos) y llama a la API.
- El contrato `openapi.yml` es el acuerdo entre las dos partes: si un
  endpoint cambia, primero se cambia el contrato.
- El código del frontend vivirá en una carpeta `frontend/` dentro de
  este mismo repositorio, para mantener un solo flujo de issues y PRs
  (ver `CONTRIBUTING.md`) y tener el contrato junto a sus dos lados.
- En desarrollo se corren dos procesos: el backend
  (`./mvnw spring-boot:run`, puerto 8080) y el servidor de desarrollo
  de Vite.
- El uso de TypeScript se decide al crear el proyecto del frontend.

**Por qué Vue y no React o Angular.** El mayor riesgo de esta decisión
es la curva de aprendizaje (ver Consecuencias). Vue exige menos
decisiones de herramientas que React: su router (Vue Router) y su
manejo de estado (Pinia) son parte del ecosistema oficial, y sus
componentes de un solo archivo agrupan HTML, lógica y estilos en un
mismo lugar, algo cercano para quien viene del backend. React y
Angular son opciones igualmente válidas; como la SPA solo depende del
contrato de la API, cambiar de framework más adelante no afecta al
backend.

## Consecuencias

**Pros:**

- Aprovecha lo que ya está hecho: la API REST, el contrato OpenAPI y
  Swagger UI sirven tal cual, sin duplicar controllers para vistas.
- Encaja con la autenticación Bearer planeada (`/usuarios/me/...`): una
  SPA envía el token en cada petición, sin depender de sesiones del
  servidor.
- No requiere instalar nada: cualquier persona con un navegador (también
  en el celular, con diseño responsivo) puede usar la app, y la demo es
  una URL.
- Deja la API lista para otros clientes (por ejemplo, una app móvil)
  sin cambios en el backend.
- Una vez acordado el contrato, backend y frontend pueden avanzar en
  paralelo, lo que ayuda con la disponibilidad limitada del equipo.

**Contras:**

- El equipo debe aprender JavaScript, Vue y el tooling de Node.js (npm,
  Vite). Se mitiga eligiendo Vue y acotando la interfaz a las
  funciones comprometidas: búsqueda, lista de conciertos, perfil y
  favoritos.
- Hay dos aplicaciones que levantar en desarrollo y dos conjuntos de
  dependencias que mantener (Maven y npm).
- Hay que resolver la comunicación entre orígenes distintos (CORS): en
  desarrollo con el proxy de Vite, y en un despliegue separado con
  configuración CORS en el backend.
- Algunas validaciones (por ejemplo, el formato del correo) pueden
  quedar duplicadas en cliente y servidor; el backend sigue siendo la
  fuente de verdad.
- El despliegue pasa de un artefacto a dos (el backend y los archivos
  estáticos de la SPA), a menos que la SPA compilada se sirva desde
  Spring Boot.

## Alternativas consideradas

### Java Swing (aplicación de escritorio)

Era la propuesta original de este ADR: Swing se encargaría solo de la
interfaz y consumiría la API por HTTP.

- **Pros:** mantiene separadas la lógica de negocio y la presentación;
  es parte del JDK, así que no necesita dependencias externas; todo el
  equipo trabaja en un solo lenguaje.
- **Contras:** aspecto visual más anticuado; no es accesible desde el
  navegador ni el celular sin desarrollo adicional; cada máquina
  necesita un runtime de Java para usarla; se deben manejar dos capas
  corriendo por separado (igual que con una SPA, pero sin los
  beneficios de distribución de la web).
- **Por qué se descartó:** el caso de uso (buscar conciertos desde
  cualquier lugar) pide una interfaz que no requiera instalación, y el
  único beneficio exclusivo de Swing (un solo lenguaje) no compensa
  perder eso.

### Thymeleaf (renderizado en el servidor)

En lugar de un cliente separado, el backend renderiza las vistas HTML
con Thymeleaf y las envía al navegador.

- **Pros:** reduce la complejidad operativa (un solo proyecto, un solo
  repositorio, un solo despliegue); todo el equipo sigue en el
  ecosistema Java/Spring; elimina los problemas de comunicación entre
  frontend y backend (CORS, serialización JSON).
- **Contras:** acopla fuertemente la interfaz con el backend; si en el
  futuro se requiere una app móvil nativa, habría que construir la API
  REST de todos modos; la experiencia de usuario es menos interactiva;
  la autenticación Bearer planeada está pensada para clientes
  separados, y con vistas del servidor lo natural serían las sesiones.
- **Por qué se descartó:** la API REST con contrato ya existe, así que
  dejaría sin uso lo ya construido (o habría que mantener dos juegos de
  controllers, uno para HTML y otro para JSON).

### Otros frameworks de SPA (React, Angular)

Misma arquitectura que la opción elegida, con otro framework.

- **React:** ecosistema más grande y más material disponible, pero
  exige elegir por separado router, manejo de estado y herramientas.
- **Angular:** framework completo y estructurado, basado en
  TypeScript, con una curva de aprendizaje más pronunciada.
- **Por qué se descartaron:** solo por la curva de aprendizaje. No hay
  una razón técnica en contra, y pueden reconsiderarse sin afectar al
  backend.

## Próximos pasos

- Crear el issue de arranque del frontend: generar `frontend/` con
  Vue 3 + Vite y una primera pantalla que liste los conciertos desde
  `GET /api/v1/conciertos`.
- Configurar el proxy de Vite hacia `http://localhost:8080` para
  desarrollo, y agregar configuración CORS en el backend (`WebConfig`)
  solo cuando se despliegue por separado.
- Actualizar el README con los requisitos (Node.js LTS) y los pasos
  para levantar el frontend en cuanto exista.
- Decidir en un ADR aparte cómo se guardará el token de autenticación
  en el navegador cuando se implemente el login.

Esta decisión se revisa (con un nuevo ADR que la reemplace) si el
equipo no logra sostener el stack de JavaScript o si se descarta la
API REST como contrato central del sistema.
