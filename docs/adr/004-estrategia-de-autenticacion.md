# ADR-004: Estrategia de autenticación y autorización

## Status

Propuesta

## Contexto

Actualmente, el contrato OpenAPI ya tiene endpoints que utilizan tokens Bearer para consultar y modificar el perfil y los favoritos de los usuarios. Sin embargo, todavía falta implementar el inicio de sesión, el manejo de contraseñas y la creación y validación de los tokens.

Antes de empezar a programar estas partes, necesitamos decidir cómo se van a autenticar los usuarios, dónde se guardarán los tokens y qué permisos tendrá cada tipo de usuario.

También tenemos que tomar en cuenta la seguridad de las contraseñas y los tokens, proteger los endpoints y asegurarnos de que todo funcione con la API que ya tenemos.

## Decisión

Se propone que los usuarios puedan iniciar sesión con su correo electrónico y contraseña, y que utilicemos tokens JWT para comprobar su identidad al hacer solicitudes a la API.

Las contraseñas se guardarán usando un algoritmo seguro como BCrypt, para que no se almacenen directamente como texto normal. El backend se encargará de comprobar las credenciales, generar los tokens y revisar que sean válidos y que no hayan expirado.

Para el cliente SPA, se propone guardar el token de acceso en memoria, en lugar de utilizar `localStorage`. De esta forma, el token no se queda guardado de manera permanente en el navegador. Sin embargo, esto no elimina por completo los riesgos de seguridad, ya que un ataque XSS podría permitir que alguien accediera al token. Además, al recargar la página, el token se perderá.

También se propone tener dos roles:

- `USER`: podrá consultar y modificar su propio perfil, así como agregar y eliminar sus conciertos favoritos.
- `ADMIN`: tendrá los mismos permisos que un usuario normal y, además, podrá crear, editar y eliminar conciertos.

El backend tendrá que revisar los permisos antes de permitir el acceso a cada endpoint protegido. No basta con ocultar botones u opciones en la interfaz, porque alguien todavía podría intentar hacer las solicitudes directamente.

Por ahora, todo esto es una propuesta, así que el equipo tendrá que revisarla y ponerse de acuerdo antes de comenzar a implementarla.

## Consecuencias

**Pros:**

- Los JWT permiten seguir utilizando la autenticación mediante Bearer tokens que ya aparece en el contrato de la API.
- BCrypt ayuda a proteger las contraseñas, ya que no se guardan directamente.
- Al tener nuestra propia autenticación, podemos controlar cómo funciona el inicio de sesión y cómo se verifican los usuarios.
- Guardar el token en memoria evita que siga almacenado después de cerrar o recargar la página.
- Los roles `USER` y `ADMIN` permiten separar las acciones normales de las que solo debería realizar un administrador.

**Contras:**

- Nosotros tendremos que programar y mantener el sistema de autenticación y autorización.
- Tendremos que decidir cuánto tiempo duran los tokens y cómo se renovará la sesión.
- Si el usuario recarga la página, perderá el token y tendrá que iniciar sesión otra vez, a menos que implementemos una forma de renovar la sesión.
- Si existe una vulnerabilidad XSS, el token podría quedar expuesto.
- Tendremos que proteger bien las contraseñas, la clave que se utiliza para firmar los JWT y los endpoints que requieren autenticación.

## Alternativas consideradas

### 1. Crear nuestra propia autenticación con JWT, correo y contraseña

**Pros:**

- Podemos controlar cómo funciona el inicio de sesión.
- Es compatible con el contrato actual de la API.
- No necesitamos depender de un servicio externo para autenticar a los usuarios.

**Contras:**

- Tenemos que encargarnos de manejar de forma segura las contraseñas, los tokens y las sesiones.
- Si más adelante lo necesitamos, también tendremos que agregar funciones como la recuperación de contraseñas.

Se propone utilizar esta opción porque funciona con la API que ya tenemos y nos permite manejar la autenticación desde nuestro propio backend.

### 2. Utilizar OAuth2 para iniciar sesión con Google

**Pros:**

- Google se encarga de comprobar las credenciales de sus usuarios.
- No tenemos que manejar directamente las contraseñas de las cuentas de Google.
- Puede hacer que iniciar sesión sea más sencillo para quienes ya tienen una cuenta de Google.

**Contras:**

- Tenemos que registrar y configurar nuestra aplicación con Google.
- Dependemos de un servicio externo.
- Aunque usemos Google, el backend todavía tendrá que comprobar quién es el usuario y decidir cómo se autenticarán sus solicitudes a la API.

Por ahora, se propone no utilizar esta opción, ya que implicaría hacer configuraciones e integraciones adicionales que podemos evitar en esta primera versión.

### 3. Guardar el token en `localStorage`

**Pros:**

- El token sigue disponible aunque se recargue la página.
- Es sencillo obtenerlo desde JavaScript para enviarlo en las solicitudes a la API.

**Contras:**

- Si existe una vulnerabilidad XSS, el token podría ser leído por código malicioso.
- El token seguirá guardado hasta que se elimine o expire.

Se propone no utilizar esta opción porque el token quedaría almacenado de forma persistente y podría ser accesible desde JavaScript.

### 4. Guardar el token en cookies `HttpOnly`

**Pros:**

- JavaScript no puede leer directamente las cookies que tienen la opción `HttpOnly`.
- Las opciones `Secure` y `SameSite` ayudan a agregar otras medidas de seguridad.

**Contras:**

- Tenemos que tomar medidas para evitar ataques CSRF.
- Es necesario adaptar la configuración del frontend y del backend para trabajar con cookies.

Por ahora, se propone no utilizar esta opción porque la API ya está planteada para utilizar Bearer tokens. De todas formas, podríamos considerarla más adelante si decidimos utilizar cookies para manejar las sesiones.

### 5. No utilizar roles de usuario

**Pros:**

- La implementación inicial sería más sencilla.
- Habría menos configuraciones para manejar los permisos.

**Contras:**

- No podríamos distinguir claramente entre usuarios normales y administradores.
- Sería más complicado limitar las acciones de crear, editar y eliminar conciertos a las personas que tengan permiso para hacerlo.

Se propone utilizar los roles `USER` y `ADMIN` para que cada tipo de usuario tenga permisos diferentes y podamos controlar mejor quién puede realizar cada acción.