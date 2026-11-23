
<script setup>
import { RouterLink } from 'vue-router'
</script>

<template>
  <main class="contenedor">
    <section class="tarjeta estado" aria-labelledby="titulo-error">
      <h1 id="titulo-error">Parece que estás perdido...</h1>

      <p class="texto-suave">
        La página que buscas no existe o la dirección es
        incorrecta.
      </p>

      <img
        src="/images/no-encontrado.png"
        alt="Perro buscando con lupa"
        class="imagen-error"
      />

      <RouterLink :to="{ name: 'conciertos' }" class="volver">
        Regresar a los conciertos
      </RouterLink>
    </section>
  </main>
</template>

<style scoped>
.contenedor {
  width: 100%;
  min-width: 0;
  max-width: 100%;
  box-sizing: border-box;
  padding: 24px 16px;
  margin: 0 auto;
}

.tarjeta {
  min-width: 0;
  box-sizing: border-box;
  padding: 24px;
  border: 1px solid var(--color-borde);
  border-radius: 12px;
  background: var(--color-fondo);
}

.estado {
  display: flex;
  flex-direction: column;
  gap: 16px;
  overflow-wrap: anywhere;
}

.estado h1 {
  margin: 0;
  font-size: 24px;
}

.estado p {
  margin: 0;
}

.imagen-error {
  display: block;
  width: 100%;
  max-width: 280px;
  height: auto;
  margin: 0 auto;
  object-fit: contain;
}

.volver {
  align-self: flex-start;
  max-width: 100%;
  box-sizing: border-box;
  overflow-wrap: anywhere;
}

@media (max-width: 600px) {
  .contenedor {
    padding: 16px;
  }

  .tarjeta {
    padding: 20px;
  }
}
</style>
