# Guía de estilo

Reglas y buenas prácticas para escribir código limpio y consistente en el
equipo, que sea fácil de mantener entre todos.

## Java

- Nombrado: `camelCase` para variables y métodos, `PascalCase` para clases
- Identación: 4 espacios
- Llaves: se abren en la misma línea

```java
public class ConciertoService {
    public List<Concierto> buscarPorCiudad(String ciudad) {
        // ...
    }
}
```

## Nombrado en general

- Usa nombres descriptivos en variables, clases y funciones
- Evita abreviaturas poco claras

## Funciones

- Escribe funciones cortas
- Cada función debe hacer una sola cosa

## Comentarios

- Comenta el "por qué", no el "qué" (el código ya dice qué hace)
- Si algo no es obvio (una decisión rara, un workaround), coméntalo

## Por qué importa esto

- **Consistencia**: el código parece escrito por una sola persona
- **Lectura**: reduce el tiempo para entender el código y modificarlo
- **Colaboración**: minimiza merge conflicts y confusión entre el equipo

## Referencia

- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
