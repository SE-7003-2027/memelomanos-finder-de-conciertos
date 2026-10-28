<script setup>
import { RouterLink } from 'vue-router'
import BotonFavorito from '@/components/BotonFavorito.vue'
import { formatearFecha } from '@/utils/fechas'

defineProps({
  concierto: {
    type: Object,
    required: true,
  },
})
</script>

<template>
  <article class="tarjeta concierto">
    <RouterLink
      :to="{ name: 'concierto-detalle', params: { id: concierto.id } }"
      class="enlace-detalle"
    >
      <header class="cabecera">
        <span class="ciudad">{{ concierto.ciudad }}</span>
        <h2>{{ concierto.artista }}</h2>
      </header>

      <dl class="datos">
        <div>
          <dt>Fecha</dt>
          <dd>
            <time :datetime="concierto.fecha">
              {{ formatearFecha(concierto.fecha) }}
            </time>
          </dd>
        </div>

        <div>
          <dt>Lugar</dt>
          <dd>{{ concierto.lugar }}</dd>
        </div>
      </dl>

      <span class="ver-detalle">Ver detalle →</span>
    </RouterLink>

    <footer class="acciones">
      <BotonFavorito :concierto="concierto" />
    </footer>
  </article>
</template>

<style scoped>
.concierto {
  display: flex;
  flex-direction: column;
  gap: 20px;
  height: 100%;
  min-width: 0;
  overflow-wrap: anywhere;
}

.enlace-detalle {
  display: flex;
  flex-direction: column;
  gap: 20px;
  color: inherit;
  text-decoration: none;
}

.enlace-detalle:hover h2 {
  color: var(--color-acento);
}

.enlace-detalle:focus-visible {
  outline: 2px solid var(--color-acento);
  outline-offset: 6px;
  border-radius: 4px;
}

.cabecera {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 12px;
}

.ciudad {
  padding: 4px 10px;
  border-radius: 999px;
  background: var(--color-acento-suave);
  color: var(--color-acento);
  font-size: 13px;
  font-weight: 600;
}

.cabecera h2 {
  font-size: 22px;
}

.datos {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin: 0;
}

.datos dt {
  color: var(--color-suave);
  font-size: 13px;
}

.datos dd {
  margin: 2px 0 0;
}

.ver-detalle {
  color: var(--color-acento);
  font-size: 14px;
  font-weight: 600;
}

.acciones {
  margin-top: auto;
  padding-top: 16px;
  border-top: 1px solid var(--color-borde);
}
</style>