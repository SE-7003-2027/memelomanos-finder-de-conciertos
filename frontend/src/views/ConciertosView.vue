<script setup>
import { onMounted, ref } from 'vue'
import { buscarConciertos } from '@/api/conciertos'
import BarraBusqueda from '@/components/BarraBusqueda.vue'
import BotonFavorito from '@/components/BotonFavorito.vue'
import { formatearFecha } from '@/utils/fechas'

const conciertos = ref([])
const cargando = ref(true)
const error = ref('')

async function cargarConciertos(filtros = {}) {
  cargando.value = true
  error.value = ''
  conciertos.value = []

  try {
    conciertos.value = await buscarConciertos(filtros)
  } catch (e) {
    error.value = e.message
  } finally {
    cargando.value = false
  }
}

onMounted(() => {
  cargarConciertos()
})
</script>

<template>
  <div class="contenedor">
    <header class="cabecera">
      <h1>Conciertos</h1>
      <p class="texto-suave">
        Encuentra conciertos por artista, ciudad o ambos.
      </p>
    </header>

    <BarraBusqueda
      :cargando="cargando"
      @buscar="cargarConciertos"
    />

    <section
      class="resultados"
      aria-label="Resultados de conciertos"
      aria-live="polite"
      :aria-busy="cargando"
    >
      <p v-if="cargando" class="texto-suave">
        Buscando conciertos…
      </p>

      <p v-else-if="error" class="aviso aviso-error" role="alert">
        {{ error }}
      </p>

      <div v-else-if="!conciertos.length" class="tarjeta estado-vacio">
        <h2>Sin resultados</h2>
        <p class="texto-suave">
          No encontramos conciertos. Prueba con otro artista o ciudad,
          o deja los campos vacíos para ver todos.
        </p>
      </div>

      <template v-else>
        <p class="texto-suave cantidad">
          {{ conciertos.length }}
          {{ conciertos.length === 1 ? 'concierto encontrado' : 'conciertos encontrados' }}
        </p>

        <ul class="lista">
          <li
            v-for="concierto in conciertos"
            :key="concierto.id"
            class="tarjeta concierto"
          >
            <div class="informacion">
              <h2>{{ concierto.artista }}</h2>
              <p class="texto-suave">
                {{ formatearFecha(concierto.fecha) }} ·
                {{ concierto.lugar }}, {{ concierto.ciudad }}
              </p>
            </div>

            <BotonFavorito :concierto="concierto" />
          </li>
        </ul>
      </template>
    </section>
  </div>
</template>

<style scoped>
.cabecera p {
  margin: 8px 0 0;
}

.resultados {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.cantidad {
  margin: 0;
}

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
  gap: 16px;
  align-items: center;
  justify-content: space-between;
}

.informacion {
  min-width: 0;
  overflow-wrap: anywhere;
}

.concierto h2 {
  font-size: 18px;
}

.concierto p {
  margin: 4px 0 0;
}

.estado-vacio h2 {
  font-size: 20px;
}

.estado-vacio p {
  margin: 8px 0 0;
}
</style>
