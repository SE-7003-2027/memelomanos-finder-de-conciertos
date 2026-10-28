<script setup>
import { ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { obtenerConcierto } from '@/api/conciertos'
import BotonFavorito from '@/components/BotonFavorito.vue'
import { formatearFecha } from '@/utils/fechas'

const route = useRoute()

const concierto = ref(null)
const cargando = ref(true)
const noEncontrado = ref(false)
const error = ref('')

watch(
  () => route.params.id,
  async (id, _anterior, onCleanup) => {
    let vigente = true

    // Evita que una respuesta anterior reemplace el concierto actual.
    onCleanup(() => {
      vigente = false
    })

    cargando.value = true
    noEncontrado.value = false
    error.value = ''
    concierto.value = null

    try {
      const datos = await obtenerConcierto(id)

      if (vigente) {
        concierto.value = datos
      }
    } catch (e) {
      if (!vigente) return

      if (e.status === 404) {
        noEncontrado.value = true
      } else {
        error.value = e.message
      }
    } finally {
      if (vigente) {
        cargando.value = false
      }
    }
  },
  { immediate: true },
)
</script>

<template>
  <div class="contenedor">
    <RouterLink :to="{ name: 'conciertos' }" class="volver">
      ← Volver a conciertos
    </RouterLink>

    <section
      aria-label="Detalle del concierto"
      aria-live="polite"
      :aria-busy="cargando"
    >
      <p v-if="cargando" class="texto-suave">
        Cargando concierto…
      </p>

      <div v-else-if="noEncontrado" class="tarjeta estado">
        <h1>Este concierto no existe</h1>
        <p class="texto-suave">
          Es posible que se haya eliminado o que la dirección sea incorrecta.
        </p>
        <RouterLink :to="{ name: 'conciertos' }">
          Buscar otros conciertos
        </RouterLink>
      </div>

      <div v-else-if="error" class="tarjeta estado">
        <h1>No pudimos cargar el concierto</h1>
        <p class="aviso aviso-error" role="alert">
          {{ error }}
        </p>
      </div>

      <article v-else-if="concierto" class="tarjeta detalle">
        <header class="cabecera">
          <span class="ciudad">{{ concierto.ciudad }}</span>
          <h1>{{ concierto.artista }}</h1>
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

        <footer class="acciones">
          <BotonFavorito :concierto="concierto" />
        </footer>
      </article>
    </section>
  </div>
</template>

<style scoped>
.volver {
  align-self: flex-start;
}

.detalle {
  display: flex;
  flex-direction: column;
  gap: 28px;
  overflow-wrap: anywhere;
}

.cabecera {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 16px;
}

.cabecera h1 {
  font-size: clamp(1.75rem, 4vw, 2.5rem);
}

.ciudad {
  padding: 4px 10px;
  border-radius: 999px;
  background: var(--color-acento-suave);
  color: var(--color-acento);
  font-size: 14px;
  font-weight: 600;
}

.datos {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 24px;
  margin: 0;
}

.datos dt {
  color: var(--color-suave);
  font-size: 14px;
}

.datos dd {
  margin: 4px 0 0;
  font-size: 18px;
}

.acciones {
  padding-top: 20px;
  border-top: 1px solid var(--color-borde);
}

.estado {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.estado h1 {
  font-size: 24px;
}

.estado p {
  margin: 0;
}

@media (max-width: 600px) {
  .datos {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>