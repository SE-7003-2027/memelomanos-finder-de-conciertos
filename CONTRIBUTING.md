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

### Rotación de revisores

Cada PR lo revisa el **revisor principal** del autor. Su approval es
obligatorio. Si no responde en 48 h, el autor le pasa la revisión al
**revisor de respaldo**. Con esta rotación todos revisan PRs de dos
personas distintas.

| Autor del PR | Revisor principal | Revisor de respaldo |
| ------------ | ----------------- | ------------------- |
| Sofía (`soffochoa`) | Saúl (`Saulssof`) | Miguel (`Sensationalforge`) |
| Ihebel (`IhebelTenorio`) | Miguel (`Sensationalforge`) | Ricardo (`ricardobuendiaa`) |
| Miguel (`Sensationalforge`) | Ricardo (`ricardobuendiaa`) | Saúl (`Saulssof`) |
| Ricardo (`ricardobuendiaa`) | Ihebel (`IhebelTenorio`) | Sofía (`soffochoa`) |
| Saúl (`Saulssof`) | Sofía (`soffochoa`) | Ihebel (`IhebelTenorio`) |

Cada issue indica al inicio quién lo revisa. Al abrir el PR, agrega a esa
persona en **Reviewers** y menciónala en la sección "Revisor asignado" de
la plantilla.

### Reglas para mergear

- Ningún PR entra a `main` sin al menos **1 approval**. La rama `main` está
  protegida: no permite merges sin revisión, ni siquiera a admins, y no
  acepta merge commits.
- **Nadie aprueba su propio PR.** Después del approval, el autor hace el
  merge con *Squash and merge*.
- Los PRs que cambian piezas compartidas (modelo de concierto, interfaz de
  proveedores, agregador) necesitan **2 approvals**: el del principal y el
  del respaldo.
- El CI (`Tests`) debe estar en verde y todas las conversaciones del PR
  resueltas antes de mergear.
- Si subes commits nuevos después del approval, el approval se descarta y
  hay que pedir otra revisión.

### Tamaño de los PRs

- Apunta a **menos de 400 líneas cambiadas** (sin contar `package-lock.json`
  ni archivos generados). Si te pasas, divide el trabajo en varios PRs.
- Un issue grande puede cerrarse con varios PRs: usa `Part of #123` en cada
  uno y `Closes #123` solo en el último.
- Si todavía no está listo, ábrelo como **Draft**.

### Mantener la rama al día sin merge commits

No hagas `git merge main` en tu rama. Para traer los cambios de `main`, usa
rebase:

```bash
git fetch origin
git rebase origin/main
git push --force-with-lease
```

Recomendado (una sola vez): `git config --global pull.rebase true`.

### Secretos

Las API keys (Ticketmaster, SoundCloud, etc.) **nunca** se suben al repo.
Se leen de variables de entorno y se documentan en `.env.example` con
valores falsos.

### Qué revisar

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