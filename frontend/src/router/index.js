import { createRouter, createWebHistory } from 'vue-router'
import ConciertosView from '../views/ConciertosView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'conciertos',
      component: ConciertosView,
    },
    {
      path: '/conciertos/:id',
      name: 'concierto-detalle',
      component: () => import('../views/ConciertoDetalleView.vue'),
    },
    {
      path: '/registro',
      name: 'registro',
      component: () => import('../views/RegistroView.vue'),
    },
    {
      path: '/entrar',
      name: 'entrar',
      component: () => import('../views/EntrarView.vue'),
    },
    {
      path: '/perfil',
      name: 'perfil',
      component: () => import('../views/PerfilView.vue'),
    },
  ],
})

export default router