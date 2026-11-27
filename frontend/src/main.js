// Tipografias del tema (#49): Big Shoulders para fechas y nombres, como en
// los carteles de conciertos, y Archivo para el texto.
import '@fontsource/big-shoulders-display/700'
import '@fontsource/big-shoulders-display/900'
import '@fontsource-variable/archivo'
import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(router)

app.mount('#app')
