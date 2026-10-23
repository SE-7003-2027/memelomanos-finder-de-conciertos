# Guía de estilo

Reglas y buenas prácticas para escribir código limpio y consistente en el
equipo, que sea fácil de mantener entre todos. Esta es la única copia de
la guía: la wiki enlaza aquí.

## Reglas generales

- Usa nombres descriptivos en variables, clases y funciones, en español
  como el resto del proyecto (`buscarPorCiudad`, `usuarioActual`).
- Evita abreviaturas poco claras.
- Escribe funciones cortas; cada función debe hacer una sola cosa.
- Comenta el "por qué", no el "qué": el código ya dice qué hace. Si algo
  no es obvio (una decisión rara, un workaround), coméntalo.

## Java (backend)

- Nombrado: `camelCase` para variables y métodos, `PascalCase` para
  clases.
- Indentación: 4 espacios.
- Llaves: se abren en la misma línea.
- Inyección de dependencias por constructor, con campos `final`.
- Respeta las capas: el controller no tiene lógica de negocio y el
  service no sabe nada de HTTP (ver `docs/arquitectura.md`).

```java
@Service
public class ConciertoService {

    private final ConciertoRepository conciertoRepository;

    public ConciertoService(ConciertoRepository conciertoRepository) {
        this.conciertoRepository = conciertoRepository;
    }

    public List<Concierto> buscarPorCiudad(String ciudad) {
        // ...
    }
}
```

## Vue y JavaScript (frontend)

- Componentes en archivos `.vue` con `<script setup>`, nombrados en
  `PascalCase`. Las vistas terminan en `View` (`PerfilView.vue`) y los
  componentes reutilizables describen lo que son (`BotonFavorito.vue`).
- Variables y funciones en `camelCase`.
- Indentación: 2 espacios, comillas simples y sin punto y coma, como el
  código que genera Vite.
- Importa desde `src` con el alias `@` (`import { pedir } from '@/api/cliente'`).
- Para hablar con el backend usa las funciones de `src/api/`, nunca
  `fetch` directo en una vista.
- Los estilos de un componente van en `<style scoped>`. Los colores salen
  de las variables de `src/assets/main.css`, no de valores sueltos.
- Todo campo de formulario tiene su `<label for="...">` y un `id` único.

```vue
<script setup>
import { ref } from 'vue'
import { buscarConciertos } from '@/api/conciertos'

const conciertos = ref([])
</script>
```

## Por qué importa esto

- **Consistencia:** el código parece escrito por una sola persona.
- **Lectura:** reduce el tiempo para entender el código y modificarlo.
- **Colaboración:** minimiza merge conflicts y confusión entre el equipo.

## Referencias

- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- [Guía de estilo oficial de Vue](https://vuejs.org/style-guide/)
