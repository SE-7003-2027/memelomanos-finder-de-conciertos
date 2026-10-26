<script setup>
import { RouterLink, useRoute } from 'vue-router'

const route = useRoute()
</script>

<template>
  <div class="contenedor">
    <h1>Detalle del concierto</h1>

    <section class="tarjeta">
      <p>Concierto #{{ route.params.id }}</p>
      <RouterLink to="/">Volver a conciertos</RouterLink>
    </section>
  </div>
</template>