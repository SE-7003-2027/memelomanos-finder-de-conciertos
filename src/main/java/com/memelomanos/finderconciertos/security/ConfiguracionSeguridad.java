package com.memelomanos.finderconciertos.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@EnableWebSecurity
public class ConfiguracionSeguridad {

    private static final String ROL_ADMIN = "ADMIN";

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final HandlerExceptionResolver handlerExceptionResolver;

    public ConfiguracionSeguridad(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) ->
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        // Delega el 403 al ManejadorGlobalDeExcepciones para responder con el formato ErrorRespuesta.
        return (request, response, accessDeniedException) ->
                handlerExceptionResolver.resolveException(request, response, null, accessDeniedException);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilitar CSRF dado que usamos JWT stateless
                .csrf(csrf -> csrf.disable())
                // Conectar la configuracion de CORS definida en WebConfig
                .cors(Customizer.withDefaults())
                // Configurar 401 sin sesion y 403 con ErrorRespuesta cuando falta el rol
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )
                // Sesiones Stateless
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Reglas de autorizacion por endpoint
                .authorizeHttpRequests(auth -> auth
                        // 1. Registro y Login publicos
                        .requestMatchers(HttpMethod.POST, "/api/v1/usuarios/registro", "/api/v1/usuarios/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/usuarios/registro", "/usuarios/login").permitAll()
                        // 2. Consulta de conciertos publica
                        .requestMatchers(HttpMethod.GET, "/api/v1/conciertos/**", "/conciertos/**").permitAll()
                        // 3. Perfil de SoundCloud del artista publico (#81)
                        .requestMatchers(HttpMethod.GET, "/api/v1/artistas/**", "/artistas/**").permitAll()
                        // 4. Documentacion Swagger / OpenAPI publica
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/openapi.yml"
                        ).permitAll()
                        // 5. Alta, edicion y baja de conciertos y rutas de administracion solo para ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/v1/conciertos", "/conciertos").hasRole(ROL_ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/conciertos/**", "/conciertos/**").hasRole(ROL_ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/conciertos/**", "/conciertos/**").hasRole(ROL_ADMIN)
                        .requestMatchers("/api/v1/admin/**", "/admin/**").hasRole(ROL_ADMIN)
                        // 6. Todo lo demas requiere token JWT valido
                        .anyRequest().authenticated()
                )
                // Agregar el filtro JWT antes del filtro estandar de Spring Security
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
