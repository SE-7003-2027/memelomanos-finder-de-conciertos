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
  border-bottom: 1px solid var(--color-borde);
}

.encabezado-interior {
  max-width: var(--ancho-pagina);
  margin: 0 auto;
  padding: 16px 20px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
}

.marca {
  font-family: var(--fuente-cartel);
  font-weight: 900;
  font-size: 26px;
  line-height: 1;
  color: var(--color-texto);
  text-decoration: none;
}

nav {
  display: flex;
  gap: 22px;
}

nav a {
  padding: 4px 0;
  border-bottom: 2px solid transparent;
  color: var(--color-suave);
  font-weight: 600;
  text-decoration: none;
}

nav a:hover {
  color: var(--color-texto);
}

nav a.router-link-exact-active {
  color: var(--color-texto);
  border-bottom-color: var(--rosa);
}
</style>
