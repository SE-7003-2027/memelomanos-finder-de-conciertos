package com.memelomanos.finderconciertos.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class ConfiguracionSeguridad {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public ConfiguracionSeguridad(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) ->
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilitar CSRF dado que usamos JWT stateless
                .csrf(csrf -> csrf.disable())
                // Conectar la configuracion de CORS definida en WebConfig
                .cors(Customizer.withDefaults())
                // Configurar 401 Unauthorized cuando no este autenticado
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint()))
                // Sesiones Stateless
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Reglas de autorizacion por endpoint
                .authorizeHttpRequests(auth -> auth
                        // 1. Registro y Login publicos
                        .requestMatchers(HttpMethod.POST, "/api/v1/usuarios/registro", "/api/v1/usuarios/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/usuarios/registro", "/usuarios/login").permitAll()
                        // 2. Consulta de conciertos publica
                        .requestMatchers(HttpMethod.GET, "/api/v1/conciertos/**", "/conciertos/**").permitAll()
                        // 3. Documentacion Swagger / OpenAPI publica
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/openapi.yml"
                        ).permitAll()
                        // 4. Todo lo demas requiere token JWT valido
                        .anyRequest().authenticated()
                )
                // Agregar el filtro JWT antes del filtro estandar de Spring Security
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
