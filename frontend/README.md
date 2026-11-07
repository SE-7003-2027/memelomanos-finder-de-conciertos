# Frontend - Finder de Conciertos

SPA con Vue 3 + Vite (ver ADR-002). Consume la API de Spring Boot por HTTP.

## Cómo levantarlo

Requisitos: Node 20 o más reciente.

1. Levanta el backend (`./mvnw spring-boot:run` en la raíz, puerto 8080).
2. En otra terminal:

   ```bash
   cd frontend
   npm install
   npm run dev
   ```

3. Abre http://localhost:5173. Vite manda todo lo que empiece con `/api`
   al backend (ver `server.proxy` en `vite.config.js`), así que no hace
   falta configurar CORS en desarrollo.

## Estructura

```
src/
├── api/          cliente HTTP compartido y una función por endpoint (#51)
├── stores/       usuario actual y sus favoritos (#52)
├── views/        una vista por ruta
├── components/   piezas reutilizables, como BotonFavorito y ReproductorSoundCloud
└── utils/        fechas y validaciones
```

Para llamar a la API, usa las funciones de `src/api/` en lugar de `fetch`
directo. Si la API responde con error, lanzan un `ErrorApi` con `status`
y `message` (el `mensaje` que manda el backend).

## Reproductor de SoundCloud

`ReproductorSoundCloud.vue` (#82) se usa en el detalle del concierto. No
llama a la API hasta que el usuario presiona "Escuchar al artista", y
muestra el widget oficial de SoundCloud en un `iframe` con la `widgetUrl`
que regresa `GET /artistas/{nombre}/soundcloud` (nunca inserta HTML de
terceros). Si el artista no tiene perfil (`404`) o hay un error, solo esa
tarjeta cambia; el resto del detalle sigue igual. Ver ADR-005.
