import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    // En desarrollo, todo lo que empiece con /api se manda al backend
    // de Spring Boot. Asi el navegador cree que es el mismo origen y no
    // hay problemas de CORS.
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
