<script setup>
// Vista de registro: crea la cuenta y despues inicia sesion con los mismos
// datos, para que el usuario no tenga que escribirlos dos veces.
import { reactive, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { registrarUsuario } from '@/api/usuarios'
import { iniciarSesion } from '@/stores/usuario'
import { validarDatosUsuario, validarPassword, LARGO_MINIMO_PASSWORD } from '@/utils/validacion'

const router = useRouter()

const datos = reactive({ nombre: '', correo: '', password: '', confirmacion: '' })
const errores = ref({})
const errorGeneral = ref('')
const enviando = ref(false)

function validar() {
  const resultado = validarDatosUsuario(datos)
  const errorPassword = validarPassword(datos.password)
  if (errorPassword) {
    resultado.password = errorPassword
  } else if (datos.confirmacion !== datos.password) {
    resultado.confirmacion = 'Las contraseñas no coinciden.'
  }
  return resultado
}

async function enviar() {
  errorGeneral.value = ''
  errores.value = validar()
  if (Object.keys(errores.value).length) return

  const correo = datos.correo.trim()
  enviando.value = true
  try {
    await registrarUsuario({ nombre: datos.nombre.trim(), correo, password: datos.password })
  } catch (e) {
    if (e.status === 409) errores.value = { correo: e.message }
    else errorGeneral.value = e.message
    enviando.value = false
    return
  }

  try {
    await iniciarSesion(correo, datos.password)
    router.push({ name: 'perfil', query: { nuevo: '1' } })
  } catch {
    // La cuenta si se creo; solo fallo el inicio de sesion automatico.
    router.push({ name: 'entrar', query: { registrado: '1' } })
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

      <div class="campo">
        <label for="registro-password">Contraseña</label>
        <input
          id="registro-password"
          v-model="datos.password"
          type="password"
          autocomplete="new-password"
          :aria-invalid="!!errores.password"
          aria-describedby="registro-password-ayuda"
        />
        <span v-if="errores.password" id="registro-password-ayuda" class="error-campo">{{ errores.password }}</span>
        <span v-else id="registro-password-ayuda" class="texto-suave ayuda">Mínimo {{ LARGO_MINIMO_PASSWORD }} caracteres.</span>
      </div>

      <div class="campo">
        <label for="registro-confirmacion">Repite la contraseña</label>
        <input
          id="registro-confirmacion"
          v-model="datos.confirmacion"
          type="password"
          autocomplete="new-password"
          :aria-invalid="!!errores.confirmacion"
          aria-describedby="registro-confirmacion-error"
        />
        <span v-if="errores.confirmacion" id="registro-confirmacion-error" class="error-campo">{{ errores.confirmacion }}</span>
      </div>

      <p v-if="errorGeneral" class="aviso aviso-error" role="alert">{{ errorGeneral }}</p>

      <div class="acciones">
        <button type="submit" class="boton boton-primario" :disabled="enviando">
          {{ enviando ? 'Creando cuenta…' : 'Crear cuenta' }}
        </button>
      </div>
    </form>

    <p class="texto-suave">¿Ya tienes cuenta? <RouterLink to="/entrar">Entra aquí</RouterLink></p>
  </div>
</template>

<style scoped>
.ayuda {
  font-size: 14px;
}
</style>
