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