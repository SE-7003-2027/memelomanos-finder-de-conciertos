<script setup>
// "Entrar" temporal (issue #52): busca el perfil por correo con
// GET /usuarios/perfil y lo deja como usuario actual. No pide contraseña
// porque todavia no hay login; se reemplaza cuando se cierre #57.
import { ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { obtenerPerfil } from '@/api/usuarios'
import { iniciarSesion } from '@/stores/usuario'
import { esCorreoValido } from '@/utils/validacion'

const router = useRouter()

const correo = ref('')
const errorCorreo = ref('')
const errorGeneral = ref('')
const enviando = ref(false)

async function enviar() {
  errorCorreo.value = ''
  errorGeneral.value = ''
  if (!esCorreoValido(correo.value)) {
    errorCorreo.value = 'Escribe un correo valido, por ejemplo nombre@correo.com.'
    return
  }

  enviando.value = true
  try {
    const usuario = await obtenerPerfil(correo.value.trim())
    await iniciarSesion(usuario)
    router.push({ name: 'perfil' })
  } catch (e) {
    if (e.status === 404) {
      errorCorreo.value = 'No hay ninguna cuenta con ese correo.'
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
      <p class="texto-suave">Por ahora solo necesitas tu correo. La contraseña llega con el login.</p>
    </header>

    <form class="tarjeta formulario" novalidate @submit.prevent="enviar">
      <div class="campo">
        <label for="entrar-correo">Correo</label>
        <input
          id="entrar-correo"
          v-model="correo"
          type="email"
          autocomplete="email"
          :aria-invalid="!!errorCorreo"
          aria-describedby="entrar-correo-error"
        />
        <span v-if="errorCorreo" id="entrar-correo-error" class="error-campo">{{ errorCorreo }}</span>
      </div>

      <p v-if="errorGeneral" class="aviso aviso-error" role="alert">{{ errorGeneral }}</p>

      <div class="acciones">
        <button type="submit" class="boton boton-primario" :disabled="enviando">
          {{ enviando ? 'Buscando…' : 'Entrar' }}
        </button>
      </div>
    </form>

    <p class="texto-suave">¿No tienes cuenta? <RouterLink to="/registro">Crea una</RouterLink></p>
  </div>
</template>
