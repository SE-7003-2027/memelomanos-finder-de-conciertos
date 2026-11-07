<script setup>
// Reproductor de SoundCloud para el detalle de un concierto (#82).
// No pide nada hasta que el usuario lo abre, para que la vista cargue
// rapido; si SoundCloud no tiene datos, el resto del detalle no cambia.
//   <ReproductorSoundCloud :artista="concierto.artista" />
import { computed, ref, watch } from 'vue'
import { obtenerSoundCloudArtista } from '@/api/artistas'

const props = defineProps({
  artista: { type: String, required: true },
})

const WIDGET = 'https://w.soundcloud.com/player/'

const abierto = ref(false)
const cargando = ref(false)
const sinPerfil = ref(false)
const error = ref('')
const datos = ref(null)
const pistaActual = ref(null)

// Cuenta las peticiones para ignorar una respuesta vieja si el usuario
// cambia de concierto mientras se estaba cargando la anterior.
let peticionActual = 0

// Si se elige una pista, el widget la reproduce sola; si no, muestra el
// perfil con sus pistas. Solo aceptamos URLs del widget oficial.
const urlWidget = computed(() => {
  if (pistaActual.value) {
    return `${WIDGET}?url=${encodeURIComponent(pistaActual.value.url)}&auto_play=true&show_comments=false&visual=false`
  }
  const url = datos.value?.widgetUrl
  return url && url.startsWith(WIDGET) ? url : null
})

const altoWidget = computed(() => (pistaActual.value ? 166 : 300))

async function cargar() {
  const numero = ++peticionActual
  abierto.value = true
  cargando.value = true
  sinPerfil.value = false
  error.value = ''
  datos.value = null
  pistaActual.value = null

  try {
    const respuesta = await obtenerSoundCloudArtista(props.artista)
    if (numero !== peticionActual) return
    datos.value = respuesta
  } catch (e) {
    if (numero !== peticionActual) return
    if (e.status === 404) sinPerfil.value = true
    else error.value = e.message
  } finally {
    if (numero === peticionActual) cargando.value = false
  }
}

function elegir(pista) {
  pistaActual.value = pistaActual.value?.url === pista.url ? null : pista
}

function formatearDuracion(milisegundos) {
  const totalSegundos = Math.round((milisegundos || 0) / 1000)
  const minutos = Math.floor(totalSegundos / 60)
  const segundos = String(totalSegundos % 60).padStart(2, '0')
  return `${minutos}:${segundos}`
}

// Vue reutiliza el componente al pasar de un concierto a otro: se cierra
// para no mostrar la musica del artista anterior.
watch(
  () => props.artista,
  () => {
    peticionActual++
    abierto.value = false
    cargando.value = false
    sinPerfil.value = false
    error.value = ''
    datos.value = null
    pistaActual.value = null
  },
)
</script>

<template>
  <section class="tarjeta reproductor" aria-labelledby="titulo-reproductor">
    <h2 id="titulo-reproductor">Escucha a {{ artista }}</h2>

    <button
      v-if="!abierto"
      type="button"
      class="boton boton-primario"
      @click="cargar"
    >
      <span aria-hidden="true">▶</span> Escuchar al artista
    </button>

    <div v-else class="contenido" aria-live="polite" :aria-busy="cargando">
      <p v-if="cargando" class="texto-suave">Buscando a {{ artista }} en SoundCloud…</p>

      <p v-else-if="sinPerfil" class="texto-suave">
        Este artista no tiene perfil en SoundCloud.
      </p>

      <div v-else-if="error" class="estado-error">
        <p class="aviso aviso-error" role="alert">
          No pudimos cargar SoundCloud. {{ error }}
        </p>
        <button type="button" class="boton" @click="cargar">Intentar de nuevo</button>
      </div>

      <template v-else-if="datos">
        <div class="perfil">
          <img
            v-if="datos.avatarUrl"
            :src="datos.avatarUrl"
            alt=""
            width="48"
            height="48"
            class="avatar"
          >
          <div class="perfil-texto">
            <strong>{{ datos.artista }}</strong>
            <a :href="datos.perfilUrl" target="_blank" rel="noopener noreferrer">
              Ver perfil en SoundCloud <span aria-hidden="true">↗</span>
            </a>
          </div>
        </div>

        <iframe
          v-if="urlWidget"
          :key="urlWidget"
          :src="urlWidget"
          :height="altoWidget"
          :title="`Reproductor de SoundCloud: ${pistaActual ? pistaActual.titulo : datos.artista}`"
          class="widget"
          allow="autoplay; encrypted-media"
          loading="lazy"
          frameborder="0"
          scrolling="no"
        />

        <div v-if="datos.pistas?.length" class="pistas">
          <h3>Más escuchadas</h3>
          <ol>
            <li v-for="pista in datos.pistas" :key="pista.url">
              <button
                type="button"
                class="pista"
                :class="{ activa: pistaActual?.url === pista.url }"
                :aria-pressed="pistaActual?.url === pista.url"
                @click="elegir(pista)"
              >
                <span class="titulo">{{ pista.titulo }}</span>
                <span class="duracion">{{ formatearDuracion(pista.duracionMs) }}</span>
              </button>
            </li>
          </ol>
        </div>
      </template>
    </div>
  </section>
</template>

<style scoped>
.reproductor {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 16px;
}

.reproductor h2 {
  font-size: 20px;
  overflow-wrap: anywhere;
}

.contenido {
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 100%;
}

.contenido p {
  margin: 0;
}

.estado-error {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 12px;
}

.perfil {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.avatar {
  flex-shrink: 0;
  border-radius: 50%;
  object-fit: cover;
  background: var(--color-acento-suave);
}

.perfil-texto {
  display: flex;
  flex-direction: column;
  min-width: 0;
  overflow-wrap: anywhere;
}

.perfil-texto a {
  font-size: 14px;
}

.widget {
  display: block;
  width: 100%;
  border: 0;
  border-radius: 8px;
  background: var(--color-fondo);
}

.pistas h3 {
  font-size: 15px;
  margin-bottom: 8px;
  color: var(--color-suave);
}

.pistas ol {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.pista {
  font: inherit;
  width: 100%;
  min-height: 44px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  border: 1px solid var(--color-borde);
  border-radius: 8px;
  background: var(--color-superficie);
  color: var(--color-texto);
  text-align: left;
  cursor: pointer;
}

.pista:hover,
.pista.activa {
  border-color: var(--color-acento);
  color: var(--color-acento);
}

.pista.activa {
  background: var(--color-acento-suave);
}

.pista:focus-visible {
  outline: 2px solid var(--color-acento);
  outline-offset: 2px;
}

.titulo {
  min-width: 0;
  overflow-wrap: anywhere;
}

.duracion {
  flex-shrink: 0;
  color: var(--color-suave);
  font-size: 14px;
  font-variant-numeric: tabular-nums;
}
</style>
