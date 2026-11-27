<script setup>
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import BotonFavorito from '@/components/BotonFavorito.vue'
import { partesDeFecha } from '@/utils/fechas'
// Foto de Nainoa Shizuru en Unsplash (licencia de Unsplash):
// https://unsplash.com/photos/NcdG9mK3PBY
import imagenPredeterminada from '@/assets/concierto-default.jpg'

const props = defineProps({
  concierto: {
    type: Object,
    required: true,
  },
})

function usarImagenPredeterminada(event) {
  const imagen = event.target
  imagen.onerror = null
  imagen.src = imagenPredeterminada
}

const fecha = computed(() => partesDeFecha(props.concierto.fecha))
</script>

<template>
  <!-- Cada concierto se muestra como un boleto: foto, fecha y artista, y un
       talon separado por una linea perforada con las acciones. -->
  <article class="tarjeta concierto">
    <img
      class="imagen-concierto"
      :src="concierto.imagenUrl || imagenPredeterminada"
      alt=""
      @error="usarImagenPredeterminada"
    />

    <RouterLink
      :to="{ name: 'concierto-detalle', params: { id: concierto.id } }"
      class="cuerpo"
    >
      <time v-if="fecha" class="fecha" :datetime="concierto.fecha">
        <span class="dia">{{ fecha.dia }}</span>
        <span class="mes">{{ fecha.mes }}</span>
        <span class="anio">{{ fecha.anio }}</span>
      </time>

      <div class="info">
        <h2>{{ concierto.artista }}</h2>
        <p class="lugar">{{ concierto.lugar }}, {{ concierto.ciudad }}</p>
      </div>
    </RouterLink>

    <footer class="talon">
      <RouterLink
        :to="{ name: 'concierto-detalle', params: { id: concierto.id } }"
        class="boton boton-primario"
      >
        Ver detalle
      </RouterLink>
      <BotonFavorito :concierto="concierto" />
    </footer>
  </article>
</template>

<style scoped>
.concierto {
  position: relative;
  display: grid;
  grid-template-columns: 200px minmax(0, 1fr) auto;
  padding: 0;
  overflow: hidden;
}

.imagen-concierto {
  display: block;
  width: 100%;
  height: 100%;
  min-height: 150px;
  object-fit: cover;
}

.cuerpo {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 20px 24px;
  min-width: 0;
  color: inherit;
  text-decoration: none;
}

.cuerpo:hover h2 {
  color: var(--rosa);
}

.cuerpo:focus-visible {
  outline: 2px solid var(--rosa);
  outline-offset: -4px;
}

.fecha {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
  min-width: 64px;
  font-family: var(--fuente-cartel);
  line-height: 0.9;
}

.dia {
  font-size: 56px;
  font-weight: 900;
}

.mes {
  font-size: 22px;
  font-weight: 700;
  color: var(--rosa);
}

.anio {
  margin-top: 4px;
  font-family: var(--fuente-texto);
  font-size: 13px;
  color: var(--color-suave);
}

.info {
  min-width: 0;
  overflow-wrap: anywhere;
}

.info h2 {
  font-size: clamp(1.75rem, 4vw, 2.25rem);
}

.lugar {
  margin: 6px 0 0;
  color: var(--color-suave);
}

/* Talon: la linea punteada y los dos medios circulos simulan la
   perforacion de un boleto de papel. */
.talon {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: stretch;
  gap: 10px;
  padding: 20px 24px;
  border-left: 2px dashed #cdb9a0;
}

.talon::before,
.talon::after {
  content: '';
  position: absolute;
  left: -12px;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: var(--telon);
}

.talon::before {
  top: -11px;
}

.talon::after {
  bottom: -11px;
}

@media (max-width: 680px) {
  .concierto {
    grid-template-columns: minmax(0, 1fr);
  }

  .imagen-concierto {
    aspect-ratio: 16 / 9;
    min-height: 0;
  }

  .talon {
    flex-direction: row;
    flex-wrap: wrap;
    align-items: center;
    border-left: 0;
    border-top: 2px dashed #cdb9a0;
  }

  .talon::before,
  .talon::after {
    top: -12px;
    left: auto;
  }

  .talon::before {
    left: -11px;
  }

  .talon::after {
    right: -11px;
    bottom: auto;
  }
}
</style>
