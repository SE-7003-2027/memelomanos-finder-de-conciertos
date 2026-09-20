# ADR-002: Java Swing como interfaz gráfica de escritorio

## Status

Propuesta

## Contexto

El backend del proyecto (ver ADR-001) usa Java + Spring Boot para
exponer una API. Falta definir cómo el usuario va a interactuar con
esa API: una interfaz web, móvil, o de escritorio.

## Decisión

El equipo está considerando construir una aplicación de escritorio
usando Java Swing como interfaz gráfica, la cual consumiría la API
expuesta por el backend de Spring Boot.

La idea es que Swing se encargue únicamente de la interfaz (ventanas,
botones, listas de conciertos, formularios de perfil), mientras que
toda la lógica de negocio, manejo de datos y reglas del sistema
vivirían en el backend de Spring Boot, comunicándose por peticiones
HTTP a la API.

Esta decisión todavía no está cerrada: el equipo quiere validar que
Swing sea la opción correcta antes de comprometerse por completo (por
ejemplo, comparándola con hacer una interfaz web en su lugar).

## Consecuencias

**Pros (si se decide usar Swing):**

- Mantiene separadas la lógica de negocio (Spring Boot) y la
  presentación (Swing), lo que facilita modificar o escalar una sin
  tener que tocar la otra
- Swing es parte del JDK estándar, no requiere dependencias externas
  ni configuración adicional para empezar
- Todo el equipo trabaja en un solo lenguaje (Java) tanto para el
  backend como para la interfaz

**Contras (si se decide usar Swing):**

- Java Swing tiene un aspecto visual más anticuado comparado con
  frameworks de UI más modernos
- Al ser una app de escritorio, no es accesible desde el navegador o
  el celular sin desarrollo adicional
- El equipo debe manejar dos "capas" corriendo por separado (el
  backend de Spring y el cliente de Swing) durante el desarrollo

## Próximos pasos

- Confirmar como equipo si Swing es la opción definitiva o si se
  explora una interfaz web
- Actualizar este ADR a "Aceptada" una vez que la decisión sea final

## Alternativas
- Utilizar el motor de plantillas de Thymeleaf
- Utilizar un frontend desacomplado Ej. JS con Vue, React, Angular.

### Thymeleaf
En lugar de crear una API REST y un cliente separado, el backend renderiza las vistas HTML directamente en el servidor utilizando Thymeleaf y las envía al navegador.
* **Pros:** 
  * Reduce la complejidad operativa: hay un solo proyecto, un solo repositorio y un solo despliegue.
  * Todo el equipo puede mantenerse trabajando 100% en el ecosistema Java/Spring.
  * Elimina la necesidad de lidiar con problemas de comunicación por red (CORS, serialización JSON) entre el frontend y el backend.
* **Contras:** 
  * Acopla fuertemente la interfaz de usuario con el backend.
  * Si en el futuro se requiere una aplicación móvil nativa, habría que construir la API REST de todos modos.
  * La experiencia de usuario es menos interactiva y fluida en comparación con aplicaciones modernas.

### 2. Arquitectura Desacoplada con Frontend Web Moderno (React / Vue / Angular)
Se mantiene la decisión de exponer una API REST desde Spring Boot, pero en lugar de consumir los datos con una aplicación de escritorio en Swing, se construye una Single Page Application (SPA) para la web.

* **Pros:** 
  * Distribución universal: cualquier usuario con un navegador puede usar la app sin instalar nada en su computadora (a diferencia de Swing).
  * Ecosistema masivo: es el estándar actual de la industria, lo que facilita encontrar bibliotecas de UI modernas y resolver problemas.
  * Valida estrictamente la API REST, dejándola lista para futuros clientes (como apps móviles).
* **Contras:** 
  * Requiere que el equipo aprenda e integre un stack tecnológico completamente distinto (JavaScript/TypeScript, NPM, Node.js).
  * Aumenta la carga de infraestructura, ya que se deben configurar pipelines de despliegue separados para el frontend (ej. Vercel, Netlify o AWS S3) y el backend.
