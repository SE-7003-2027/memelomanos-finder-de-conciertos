# ADR-001: Elección de Java + Spring como stack de backend

## Status

Aceptada

## Contexto

Necesitábamos elegir un lenguaje y framework de backend para construir
Finder de Conciertos. Las opciones que se consideraron incluían otros
stacks comunes para proyectos web (por ejemplo Node.js/Express o
Python/Django), pero se buscaba algo con lo que el equipo pudiera avanzar
rápido desde el inicio del proyecto.

## Decisión

Se decidió usar Java 21 con Spring (Spring Boot) para el backend,
gestionado con Maven.

La razón principal es que alguien del equipo ya tiene experiencia previa
trabajando con Java y Spring, lo cual reduce la curva de aprendizaje del
equipo y permite empezar a construir funcionalidad real más rápido, en
lugar de invertir tiempo extra aprendiendo un stack nuevo desde cero.

## Consecuencias

**Pros:**

- Arranque más rápido del proyecto, al haber alguien que puede guiar al
  resto del equipo en convenciones y buenas prácticas de Spring
- Spring Boot trae herramientas maduras para construir APIs REST,
  manejar configuración y conectarse a bases de datos
- Java es un lenguaje fuertemente tipado, lo que ayuda a detectar
  errores antes de tiempo de ejecución

**Contras:**

- El resto del equipo que no tiene experiencia previa en Java/Spring
  tendrá una curva de aprendizaje al inicio
- Java/Spring puede requerir más código repetitivo (boilerplate)
  comparado con otros stacks más ligeros