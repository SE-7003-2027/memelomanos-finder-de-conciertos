<script setup>
// Inicio de sesion con correo y contraseña (POST /usuarios/login).
import { reactive, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { usuarioActual, iniciarSesion } from '@/stores/usuario'
import { esCorreoValido } from '@/utils/validacion'

const route = useRoute()
const router = useRouter()

const datos = reactive({ correo: '', password: '' })
const errores = ref({})
const errorGeneral = ref('')
const enviando = ref(false)

const recienRegistrado = route.query.registrado === '1'

async function enviar() {
  errorGeneral.value = ''
  errores.value = {}
  if (!esCorreoValido(datos.correo)) {
    errores.value.correo = 'Escribe un correo válido, por ejemplo nombre@correo.com.'
  }
  if (!datos.password) {
    errores.value.password = 'Escribe tu contraseña.'
  }
  if (Object.keys(errores.value).length) return

  enviando.value = true
  try {
    await iniciarSesion(datos.correo.trim(), datos.password)
    // Solo se regresa a rutas internas de la app.
    const volver = route.query.volver
    const destino = typeof volver === 'string' && volver.startsWith('/') && !volver.startsWith('//')
      ? volver
      : '/perfil'
    router.push(destino)
  } catch (e) {
    if (e.status === 401) {
      errorGeneral.value = 'El correo o la contraseña no son correctos.'
    } else {
      errorGeneral.value = e.message
    }
  } finally {
    enviando.value = false
  }
}
</script>

<template>
  <div class="contenedor">
    <header>
      <h1>Entrar</h1>
      <p class="texto-suave">Entra con tu correo y tu contraseña para ver tu perfil y tus favoritos.</p>
    </header>

    <p v-if="usuarioActual.avisoSesion" class="aviso aviso-error" role="status">{{ usuarioActual.avisoSesion }}</p>
    <p v-else-if="recienRegistrado" class="aviso aviso-exito" role="status">
      Tu cuenta quedó creada. Entra con tu correo y tu contraseña.
    </p>

    <form class="tarjeta formulario" novalidate @submit.prevent="enviar">
      <div class="campo">
        <label for="entrar-correo">Correo</label>
        <input
          id="entrar-correo"
          v-model="datos.correo"
          type="email"
          autocomplete="email"
          :aria-invalid="!!errores.correo"
          aria-describedby="entrar-correo-error"
        />
        <span v-if="errores.correo" id="entrar-correo-error" class="error-campo">{{ errores.correo }}</span>
      </div>

      <div class="campo">
        <label for="entrar-password">Contraseña</label>
        <input
          id="entrar-password"
          v-model="datos.password"
          type="password"
          autocomplete="current-password"
          :aria-invalid="!!errores.password"
          aria-describedby="entrar-password-error"
        />
        <span v-if="errores.password" id="entrar-password-error" class="error-campo">{{ errores.password }}</span>
      </div>

      <p v-if="errorGeneral" class="aviso aviso-error" role="alert">{{ errorGeneral }}</p>

      <div class="acciones">
        <button type="submit" class="boton boton-primario" :disabled="enviando">
          {{ enviando ? 'Entrando…' : 'Entrar' }}
        </button>
      </div>
    </form>

    <p class="texto-suave">¿No tienes cuenta? <RouterLink to="/registro">Crea una</RouterLink></p>
  </div>
</template>
