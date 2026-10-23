import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { restaurarSesion } from './stores/usuario'

const app = createApp(App)

app.use(router)

// Recupera al usuario guardado antes de montar, para que las vistas ya
// sepan que se esta cargando y no muestren "Entra" por un instante.
restaurarSesion()

app.mount('#app')
