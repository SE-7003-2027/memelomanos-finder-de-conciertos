<script setup>
// Vista de registro (issue #52): crea el usuario y lo deja como usuario actual.
import { reactive, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { registrarUsuario } from '@/api/usuarios'
import { iniciarSesion } from '@/stores/usuario'
import { validarDatosUsuario } from '@/utils/validacion'

const router = useRouter()

const datos = reactive({ nombre: '', correo: '' })
const errores = ref({})
const errorGeneral = ref('')
const enviando = ref(false)

async function enviar() {
  errorGeneral.value = ''
  errores.value = validarDatosUsuario(datos)
  if (Object.keys(errores.value).length) return

  enviando.value = true
  try {
    const usuario = await registrarUsuario({
      nombre: datos.nombre.trim(),
      correo: datos.correo.trim(),
    })
    await iniciarSesion(usuario)
    router.push({ name: 'perfil', query: { nuevo: '1' } })
  } catch (e) {
    if (e.status === 409) {
      errores.value = { correo: e.message }
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
      <h1>Crear cuenta</h1>
      <p class="texto-suave">Guarda tus conciertos favoritos para no perderte ninguno.</p>
    </header>

    <form class="tarjeta formulario" novalidate @submit.prevent="enviar">
      <div class="campo">
        <label for="registro-nombre">Nombre</label>
        <input
          id="registro-nombre"
          v-model="datos.nombre"
          type="text"
          autocomplete="name"
          :aria-invalid="!!errores.nombre"
          aria-describedby="registro-nombre-error"
        />
        <span v-if="errores.nombre" id="registro-nombre-error" class="error-campo">{{ errores.nombre }}</span>
      </div>

      <div class="campo">
        <label for="registro-correo">Correo</label>
        <input
          id="registro-correo"
          v-model="datos.correo"
          type="email"
          autocomplete="email"
          :aria-invalid="!!errores.correo"
          aria-describedby="registro-correo-error"
        />
        <span v-if="errores.correo" id="registro-correo-error" class="error-campo">{{ errores.correo }}</span>
      </div>

      <p v-if="errorGeneral" class="aviso aviso-error" role="alert">{{ errorGeneral }}</p>

      <div class="acciones">
        <button type="submit" class="boton boton-primario" :disabled="enviando">
          {{ enviando ? 'Creando cuenta…' : 'Crear cuenta' }}
        </button>
      </div>
    </form>

    <p class="texto-suave">¿Ya tienes cuenta? <RouterLink to="/entrar">Entra con tu correo</RouterLink></p>
  </div>
</template>
