# Guía de contribución

Esta guía explica cómo reportar issues y cómo abrir PRs en este repositorio.

## Antes de crear un issue

Verifica que no exista ya un issue similar. Si no existe, usa la plantilla
que corresponda (bug, feature o documentación).

### Si es un bug

- Describe los pasos para reproducirlo
- Incluye la versión o rama sobre la que estás trabajando
- Si puedes, agrega capturas de pantalla

### Si es un feature o mejora

- Describe tu idea detalladamente
- Si puedes, incluye los archivos que se modificarían
- Si aplica, agrega capturas de cómo quedaría el sistema resultante

### Si es documentación

- Cita los archivos con documentación desactualizada
- Explica dónde se necesitan las actualizaciones

## Ramas

- Nunca trabajes directo sobre `main`: todo cambio entra por una rama y
  un Pull Request.
- Crea la rama desde un `main` actualizado (`git checkout main && git pull`).
- Usa nombres cortos, en minúsculas y con guiones, de preferencia con el
  número del issue: `51-cliente-http`, `fix-favoritos-endpoint`.

La wiki tiene el paso a paso con los comandos y los errores más comunes
en [Flujo de trabajo](https://github.com/SE-7003-2027/memelomanos-finder-de-conciertos/wiki/Flujo-de-trabajo).

## Commits

Seguimos [Conventional Commits](https://www.conventionalcommits.org/es/v1.0.0/):
un prefijo con el tipo de cambio, un alcance opcional entre paréntesis y
una descripción corta en imperativo.

| Prefijo | Se usa para |
| ------- | ----------- |
| `feat:` | Una funcionalidad nueva |
| `fix:` | Corregir un error |
| `docs:` | Cambios solo de documentación (README, ADRs, wiki) |
| `test:` | Agregar o cambiar pruebas |
| `refactor:` | Cambios internos sin cambiar el comportamiento |
| `chore:` | Configuración, dependencias o infraestructura |

Ejemplos: `feat(frontend): cliente HTTP y vistas de perfil`,
`fix: return 201 Created on user registration`,
`docs: actualizar README con los pasos del frontend`.

Un commit, una tarea: no mezcles cambios sin relación en un mismo commit.

## Antes de abrir un PR

Como requester (quien abre el PR):

- Ten una rama limpia, con solo los commits necesarios, en un orden que
  ayude al revisor a entender los cambios
- Revisa que las pruebas pasen y que la funcionalidad sea correcta antes
  de abrir el PR
- Agrega pruebas que el revisor pueda replicar
- Rebasa tu rama con main
- Documenta el PR usando la plantilla (`.github/pull_request_template.md`)
- Enfoca el PR a un solo bug/feature; si hay cambios extra, ponlos en su
  propio commit (o en otro PR)
- Actualiza documentos como el README si es necesario
- En la descripción, cierra los issues con `Closes #123`

## Merge

- Se mergea con **Squash and merge**, para que cada PR quede como un solo
  commit en `main`. El título del PR se convierte en el mensaje de ese
  commit, así que debe seguir el formato de la sección Commits.
- Después del merge se borra la rama.

## Revisión de código

Como revisor:

- Verifica que el sistema funcione tal cual lo describe el requester
- Verifica que las pruebas existentes pasen
- Verifica que la rama esté limpia y rebasada con main
- Responde rápido a nuevos cambios para no bloquear al requester
- Si propones un cambio de algoritmo o diseño, indica fuentes, pros y
  contras, y ejemplifica la idea
- Deja comentarios claros y accionables

### Tipos de revisión

- **Request changes**: el PR no se puede mergear hasta resolver esos
  cambios (rompe pruebas, rompe el sistema, o no está rebasado con main)
- **Comment**: el PR se puede mergear sin resolver los cambios propuestos
  (typos, estilo de código, documentación menor, ajustes mínimos de UI)

## Convenciones de código

Ver [`docs/guia-de-estilo.md`](docs/guia-de-estilo.md).

## Decisiones de arquitectura

Los cambios importantes de diseño se documentan como ADR en
[`docs/adr/`](docs/adr/), a partir de [`docs/adr/template.md`](docs/adr/template.md).
El índice y el procedimiento para escribir uno nuevo están en la página
[Decisiones de arquitectura](https://github.com/SE-7003-2027/memelomanos-finder-de-conciertos/wiki/Decisiones-de-arquitectura)
de la wiki.
