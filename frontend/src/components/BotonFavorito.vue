<script setup>
// Boton para marcar o desmarcar un concierto como favorito (issue #52).
// Pensado para ponerse en la tarjeta de concierto de #50:
//   <BotonFavorito :concierto="concierto" />
import { computed, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { usuarioActual, esFavorito, agregarFavorito, quitarFavorito } from '@/stores/usuario'

const props = defineProps({
  concierto: { type: Object, required: true },
})

const ocupado = ref(false)
const error = ref('')

const marcado = computed(() => esFavorito(props.concierto.id))
const etiqueta = computed(() =>
  marcado.value ? `Quitar ${props.concierto.artista} de favoritos` : `Agregar ${props.concierto.artista} a favoritos`,
)

async function alternar() {
  ocupado.value = true
  error.value = ''
  try {
    if (marcado.value) await quitarFavorito(props.concierto.id)
    else await agregarFavorito(props.concierto)
  } catch (e) {
    error.value = e.message
  } finally {
    ocupado.value = false
  }
}
</script>

<template>
  <span class="favorito">
    <button
      v-if="usuarioActual.usuario"
      type="button"
      class="boton-corazon"
      :class="{ marcado }"
      :aria-pressed="marcado"
      :aria-label="etiqueta"
      :title="etiqueta"
      :disabled="ocupado"
      @click="alternar"
    >
      <span aria-hidden="true">{{ marcado ? '♥' : '♡' }}</span>
      {{ marcado ? 'En favoritos' : 'Favorito' }}
    </button>
    <RouterLink v-else to="/entrar" class="texto-suave enlace-entrar">Entra para guardar favoritos</RouterLink>
    <span v-if="error" class="error" role="alert">{{ error }}</span>
  </span>
</template>

<style scoped>
.favorito {
  display: inline-flex;
  flex-direction: column;
  gap: 4px;
  align-items: flex-start;
}

.boton-corazon {
  font: inherit;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  display: inline-flex;
  gap: 6px;
  align-items: center;
  border: 1px solid var(--color-borde);
  background: var(--color-superficie);
  color: var(--color-suave);
  border-radius: 999px;
  padding: 5px 12px;
}

.boton-corazon:hover:not(:disabled),
.boton-corazon.marcado {
  border-color: var(--color-error);
  color: var(--color-error);
}

.boton-corazon:disabled {
  opacity: 0.6;
  cursor: wait;
}

.boton-corazon:focus-visible {
  outline: 2px solid var(--color-acento);
  outline-offset: 2px;
}

.enlace-entrar {
  font-size: 14px;
}

.error {
  color: var(--color-error);
  font-size: 13px;
}
</style>
