<script setup>
import { RouterLink, RouterView } from 'vue-router'
import { usuarioActual } from '@/stores/usuario'
</script>

<template>
  <header class="encabezado">
    <div class="encabezado-interior">
      <RouterLink to="/" class="marca">Finder de Conciertos</RouterLink>
      <nav>
        <RouterLink to="/">Conciertos</RouterLink>
        <template v-if="usuarioActual.usuario">
          <RouterLink to="/perfil">Mi perfil</RouterLink>
        </template>
        <template v-else>
          <RouterLink to="/entrar">Entrar</RouterLink>
          <RouterLink to="/registro">Crear cuenta</RouterLink>
        </template>
      </nav>
    </div>
  </header>

  <main>
    <RouterView />
  </main>
</template>

<style scoped>
.encabezado {
  background: var(--color-superficie);
  border-bottom: 1px solid var(--color-borde);
}

.encabezado-interior {
  max-width: 720px;
  margin: 0 auto;
  padding: 14px 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
}

.marca {
  font-weight: 800;
  font-size: 18px;
  color: var(--color-texto);
  text-decoration: none;
}

nav {
  display: flex;
  gap: 16px;
}

nav a {
  text-decoration: none;
  color: var(--color-suave);
  font-weight: 500;
}

nav a.router-link-exact-active {
  color: var(--color-acento);
}
</style>
