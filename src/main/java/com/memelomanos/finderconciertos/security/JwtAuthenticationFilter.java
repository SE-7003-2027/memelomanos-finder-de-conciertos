package com.memelomanos.finderconciertos.security;

import com.memelomanos.finderconciertos.model.RolUsuario;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIJO_ROL = "ROLE_";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        try {
            final String correo = jwtService.extraerCorreo(jwt);

            if (correo != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtService.esTokenValido(jwt, correo)) {
                    RolUsuario rol = jwtService.extraerRol(jwt);
                    List<SimpleGrantedAuthority> autoridades = List.of(
                            new SimpleGrantedAuthority(PREFIJO_ROL + rol.name())
                    );
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            correo,
                            null,
                            autoridades
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception ignored) {
            // Si el token es invalido o esta expirado, no se autentica y continua el filtro
        }

        filterChain.doFilter(request, response);
    }
}
