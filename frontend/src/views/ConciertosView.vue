<script setup>
// Lista provisional para comprobar favoritos y navegacion.
// La busqueda y el detalle completo se implementan en #50.
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { buscarConciertos } from '@/api/conciertos'
import BotonFavorito from '@/components/BotonFavorito.vue'
import { formatearFecha } from '@/utils/fechas'

const conciertos = ref([])
const cargando = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    conciertos.value = await buscarConciertos()
  } catch (e) {
    error.value = e.message
  } finally {
    cargando.value = false
  }
})
</script>

<template>
  <div class="contenedor">
    <h1>Conciertos</h1>
    <p v-if="cargando" class="texto-suave">Cargando conciertos…</p>
    <p v-else-if="error" class="aviso aviso-error" role="alert">{{ error }}</p>
    <p v-else-if="!conciertos.length" class="texto-suave">Todavía no hay conciertos.</p>

    <ul v-else class="lista">
      <li v-for="concierto in conciertos" :key="concierto.id" class="tarjeta concierto">
        <div>
          <h2>{{ concierto.artista }}</h2>
          <p class="texto-suave">
            {{ formatearFecha(concierto.fecha) }} · {{ concierto.lugar }}, {{ concierto.ciudad }}
          </p>
          <RouterLink
            :to="{ name: 'concierto-detalle', params: { id: concierto.id } }"
          >
            Ver detalle
          </RouterLink>
        </div>

        <BotonFavorito :concierto="concierto" />
      </li>
    </ul>
  </div>
</template>

<style scoped>
.lista {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.concierto {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
}

.concierto h2 {
  font-size: 18px;
}

.concierto p {
  margin: 4px 0 0;
}
</style>