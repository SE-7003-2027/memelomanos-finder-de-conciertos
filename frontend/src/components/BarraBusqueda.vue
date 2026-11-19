<script setup>
import { reactive } from 'vue'

const props = defineProps({
  cargando: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['buscar'])

const filtros = reactive({
  artista: '',
  ciudad: '',
  fechaDesde: '',
  fechaHasta: '',
})

function enviar() {
  if (props.cargando) return

  emit('buscar', {
    artista: filtros.artista.trim(),
    ciudad: filtros.ciudad.trim(),
    fechaDesde: filtros.fechaDesde,
    fechaHasta: filtros.fechaHasta,
  })
}
</script>

<template>
  <form
    class="tarjeta buscador"
    role="search"
    aria-label="Buscar conciertos"
    @submit.prevent="enviar"
  >
    <div class="campo">
      <label for="buscar-artista">Artista</label>
      <input
        id="buscar-artista"
        v-model="filtros.artista"
        type="search"
        placeholder="Por ejemplo, Queen"
        :disabled="cargando"
      />
    </div>

    <div class="campo">
      <label for="buscar-ciudad">Ciudad</label>
      <input
        id="buscar-ciudad"
        v-model="filtros.ciudad"
        type="search"
        placeholder="Por ejemplo, CDMX"
        :disabled="cargando"
      />
    </div>

    <div class="campo">
      <label for="buscar-fecha-desde">Desde</label>
      <input
        id="buscar-fecha-desde"
        v-model="filtros.fechaDesde"
        type="date"
        :disabled="cargando"
      />
    </div>

    <div class="campo">
      <label for="buscar-fecha-hasta">Hasta</label>
      <input
        id="buscar-fecha-hasta"
        v-model="filtros.fechaHasta"
        type="date"
        :disabled="cargando"
      />
    </div>

    <button
      type="submit"
      class="boton boton-primario"
      :disabled="cargando"
    >
      {{ cargando ? 'Buscando…' : 'Buscar' }}
    </button>
  </form>
</template>

<style scoped>
.buscador {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr)) auto;
  gap: 16px;
  align-items: end;
}

.campo {
  min-width: 0;
}

.campo input {
  width: 100%;
  min-width: 0;
}

@media (max-width: 900px) {
  .buscador {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 600px) {
  .buscador {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
