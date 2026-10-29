<script setup>
// Vista de perfil (issue #52): ver y editar datos, eliminar la cuenta y
// administrar los conciertos favoritos.
import { reactive, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { actualizarUsuario, eliminarUsuario } from '@/api/usuarios'
import {
  usuarioActual,
  actualizarDatos,
  cerrarSesion,
  quitarFavorito,
  verificarPassword,
} from '@/stores/usuario'
import { validarDatosUsuario } from '@/utils/validacion'
import { formatearFecha } from '@/utils/fechas'

const route = useRoute()
const router = useRouter()

const bienvenida = ref(route.query.nuevo === '1')
const aviso = ref('')

// --- Edicion de datos ---
const editando = ref(false)
const datos = reactive({ nombre: '', correo: '', password: '' })
const errores = ref({})
const errorEdicion = ref('')
const guardando = ref(false)

function empezarEdicion() {
  datos.nombre = usuarioActual.usuario.nombre
  datos.correo = usuarioActual.usuario.correo
  datos.password = ''
  errores.value = {}
  errorEdicion.value = ''
  aviso.value = ''
  editando.value = true
}

async function guardar() {
  errorEdicion.value = ''
  errores.value = validarDatosUsuario(datos)
  if (!datos.password) {
    errores.value.password = 'Escribe tu contraseña para guardar los cambios.'
  }
  if (Object.keys(errores.value).length) return

  guardando.value = true
  try {
    // El backend pide la contraseña en el PUT pero no la revisa, asi que
    // la comprobamos aqui con el login antes de guardar.
    if (!(await verificarPassword(datos.password))) {
      errores.value = { password: 'La contraseña no es correcta.' }
      return
    }
    const usuario = await actualizarUsuario(usuarioActual.usuario.id, {
      nombre: datos.nombre.trim(),
      correo: datos.correo.trim(),
      password: datos.password,
    })
    actualizarDatos(usuario)
    editando.value = false
    aviso.value = 'Tus datos se guardaron.'
  } catch (e) {
    if (e.status === 409) errores.value = { correo: e.message }
    else errorEdicion.value = e.message
  } finally {
    guardando.value = false
  }
}

// --- Eliminar cuenta (la confirmacion vive en la pagina) ---
const confirmandoEliminar = ref(false)
const eliminando = ref(false)
const errorEliminar = ref('')

async function eliminarCuenta() {
  eliminando.value = true
  errorEliminar.value = ''
  try {
    await eliminarUsuario(usuarioActual.usuario.id)
    cerrarSesion()
    router.push({ name: 'conciertos' })
  } catch (e) {
    errorEliminar.value = e.message
  } finally {
    eliminando.value = false
  }
}

// --- Favoritos ---
const quitando = ref(null)
const errorFavoritos = ref('')

async function quitar(concierto) {
  quitando.value = concierto.id
  errorFavoritos.value = ''
  try {
    await quitarFavorito(concierto.id)
  } catch (e) {
    errorFavoritos.value = e.message
  } finally {
    quitando.value = null
  }
}

function salir() {
  cerrarSesion()
  router.push({ name: 'conciertos' })
}
</script>

<template>
  <div class="contenedor">
    <template v-if="!usuarioActual.usuario">
      <h1>Mi perfil</h1>
      <p v-if="usuarioActual.avisoSesion" class="aviso aviso-error" role="status">{{ usuarioActual.avisoSesion }}</p>
      <div class="tarjeta">
        <p>Para ver tu perfil y tus favoritos, entra con tu cuenta o crea una nueva.</p>
        <div class="acciones">
          <RouterLink to="/entrar" class="boton boton-primario">Entrar</RouterLink>
          <RouterLink to="/registro" class="boton">Crear cuenta</RouterLink>
        </div>
      </div>
    </template>

    <template v-else>
      <header class="cabecera">
        <div>
          <h1>Hola, {{ usuarioActual.usuario.nombre }}</h1>
          <p class="texto-suave">{{ usuarioActual.usuario.correo }}</p>
        </div>
        <button type="button" class="boton" @click="salir">Salir</button>
      </header>

      <p v-if="bienvenida" class="aviso aviso-exito" role="status">
        Tu cuenta quedó creada. Ve a <RouterLink to="/">Conciertos</RouterLink> y marca tus favoritos.
      </p>
      <p v-if="aviso" class="aviso aviso-exito" role="status">{{ aviso }}</p>

      <!-- Favoritos -->
      <section class="tarjeta seccion">
        <h2>Mis favoritos ({{ usuarioActual.favoritos.length }})</h2>
        <p v-if="!usuarioActual.favoritos.length" class="texto-suave">
          Todavía no tienes favoritos. Busca un concierto y toca “Favorito” para guardarlo aquí.
        </p>
        <ul v-else class="lista-favoritos">
          <li v-for="concierto in usuarioActual.favoritos" :key="concierto.id" class="favorito">
            <div class="favorito-info">
              <strong>{{ concierto.artista }}</strong>
              <span class="texto-suave">
                {{ formatearFecha(concierto.fecha) }} · {{ concierto.lugar }}, {{ concierto.ciudad }}
              </span>
            </div>
            <button
              type="button"
              class="boton"
              :disabled="quitando === concierto.id"
              :aria-label="`Quitar ${concierto.artista} de favoritos`"
              @click="quitar(concierto)"
            >
              {{ quitando === concierto.id ? 'Quitando…' : 'Quitar' }}
            </button>
          </li>
        </ul>
        <p v-if="errorFavoritos" class="aviso aviso-error" role="alert">{{ errorFavoritos }}</p>
      </section>

      <!-- Datos -->
      <section class="tarjeta seccion">
        <h2>Mis datos</h2>

        <form v-if="editando" class="formulario" novalidate @submit.prevent="guardar">
          <div class="campo">
            <label for="perfil-nombre">Nombre</label>
            <input id="perfil-nombre" v-model="datos.nombre" type="text" autocomplete="name" :aria-invalid="!!errores.nombre" />
            <span v-if="errores.nombre" class="error-campo">{{ errores.nombre }}</span>
          </div>
          <div class="campo">
            <label for="perfil-correo">Correo</label>
            <input id="perfil-correo" v-model="datos.correo" type="email" autocomplete="email" :aria-invalid="!!errores.correo" />
            <span v-if="errores.correo" class="error-campo">{{ errores.correo }}</span>
          </div>
          <div class="campo">
            <label for="perfil-password">Tu contraseña actual</label>
            <input
              id="perfil-password"
              v-model="datos.password"
              type="password"
              autocomplete="current-password"
              :aria-invalid="!!errores.password"
              aria-describedby="perfil-password-ayuda"
            />
            <span v-if="errores.password" id="perfil-password-ayuda" class="error-campo">{{ errores.password }}</span>
            <span v-else id="perfil-password-ayuda" class="texto-suave ayuda">La pedimos para confirmar que eres tú.</span>
          </div>
          <p v-if="errorEdicion" class="aviso aviso-error" role="alert">{{ errorEdicion }}</p>
          <div class="acciones">
            <button type="submit" class="boton boton-primario" :disabled="guardando">
              {{ guardando ? 'Guardando…' : 'Guardar cambios' }}
            </button>
            <button type="button" class="boton" :disabled="guardando" @click="editando = false">Cancelar</button>
          </div>
        </form>

        <template v-else>
          <dl class="datos">
            <dt>Nombre</dt>
            <dd>{{ usuarioActual.usuario.nombre }}</dd>
            <dt>Correo</dt>
            <dd>{{ usuarioActual.usuario.correo }}</dd>
          </dl>
          <div class="acciones">
            <button type="button" class="boton" @click="empezarEdicion">Editar datos</button>
          </div>
        </template>
      </section>

      <!-- Zona de peligro -->
      <section class="tarjeta seccion">
        <h2>Eliminar cuenta</h2>
        <template v-if="!confirmandoEliminar">
          <p class="texto-suave">Se borran tu perfil y tu lista de favoritos. No se puede deshacer.</p>
          <div class="acciones">
            <button type="button" class="boton boton-peligro" @click="confirmandoEliminar = true">Eliminar mi cuenta</button>
          </div>
        </template>
        <template v-else>
          <p><strong>¿Seguro que quieres eliminar tu cuenta?</strong> Perderás tus {{ usuarioActual.favoritos.length }} favoritos.</p>
          <p v-if="errorEliminar" class="aviso aviso-error" role="alert">{{ errorEliminar }}</p>
          <div class="acciones">
            <button type="button" class="boton boton-peligro" :disabled="eliminando" @click="eliminarCuenta">
              {{ eliminando ? 'Eliminando…' : 'Sí, eliminar' }}
            </button>
            <button type="button" class="boton" :disabled="eliminando" @click="confirmandoEliminar = false">Cancelar</button>
          </div>
        </template>
      </section>
    </template>
  </div>
</template>

<style scoped>
.cabecera {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: flex-start;
  justify-content: space-between;
}

.cabecera p {
  margin: 4px 0 0;
}

.seccion {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.seccion h2 {
  font-size: 18px;
}

.seccion p {
  margin: 0;
}

.lista-favoritos {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
}

.favorito {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  border-top: 1px solid var(--color-borde);
}

.favorito:first-child {
  border-top: none;
  padding-top: 0;
}

.favorito-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.datos {
  display: grid;
  grid-template-columns: max-content 1fr;
  gap: 6px 16px;
  margin: 0;
}

.datos dt {
  color: var(--color-suave);
}

.datos dd {
  margin: 0;
  overflow-wrap: anywhere;
}

.ayuda {
  font-size: 14px;
}

a.boton {
  text-decoration: none;
  display: inline-block;
}
</style>
