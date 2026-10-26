# PRD — Finder de Conciertos

## El problema

Encontrar conciertos y eventos musicales relevantes hoy en día está
disperso entre varias plataformas (redes sociales, sitios de venta de
boletos, páginas de venues específicos), lo que hace difícil para una
persona descubrir de forma centralizada qué conciertos hay disponibles
según sus gustos, ciudad o fechas.

La oportunidad es ofrecer una plataforma centralizada donde los usuarios
puedan descubrir conciertos según sus artistas favoritos, ciudad y fechas.
Esto reduce la necesidad de consultar varias páginas para encontrar eventos
y facilita guardar conciertos de interés en un perfil personal.

## Objetivos y lo que se queda fuera

**Objetivos (qué se vería como éxito):**

- Un usuario puede buscar conciertos por artista, ciudad o fecha
- Un usuario puede crear un perfil y guardar conciertos de su interés.

**Fuera de alcance (por ahora):**

- Venta o compra de boletos dentro de la plataforma
- Integración con redes sociales

## User stories / requerimientos

- Como usuario, quiero buscar conciertos por artista para encontrar fechas próximas de mis artistas favoritos.
- Como usuario, quiero buscar conciertos por ciudad para ver qué hay disponible cerca de mí.
- Como usuario, quiero buscar conciertos por rango de fechas para encontrar eventos durante un periodo específico.
- Como usuario, quiero crear una cuenta e iniciar sesión para acceder a mi perfil.
- Como usuario autenticado, quiero guardar conciertos como favoritos para consultarlos después.
- Como usuario autenticado, quiero consultar y administrar mi lista de conciertos favoritos.
- Como usuario, quiero consultar los detalles de un concierto para conocer la información del evento.

## Métricas de éxito

- El tiempo de respuesta de las búsquedas debe ser menor a 500 ms en las pruebas realizadas en el entorno de desarrollo.
- Los filtros por artista, ciudad y fecha deben devolver resultados que cumplan con los criterios indicados.
- Los conciertos almacenados en la base de datos deben poder consultarse sin errores.
- Un usuario debe poder agregar, consultar y quitar conciertos de su lista de favoritos.
- El inicio de sesión debe funcionar y las rutas protegidas deben rechazar solicitudes sin un token válido.

## Restricciones y supuestos

- Stack definido: Java 21 + Spring Boot, Maven (ver ADR-001).
- Proyecto académico con fecha de entrega ligada al calendario del curso.
- Equipo de 5 personas, con disponibilidad limitada por otras materias.
- No se ha confirmado el uso de una API externa de conciertos. Si se integra una, será necesario revisar sus condiciones de uso, disponibilidad y límites de solicitudes.
