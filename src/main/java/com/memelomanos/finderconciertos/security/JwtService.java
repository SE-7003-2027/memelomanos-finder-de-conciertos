package com.memelomanos.finderconciertos.security;

import com.memelomanos.finderconciertos.model.RolUsuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    private static final String EXPIRACION_POR_DEFECTO_MS = "86400000";
    private static final String CLAIM_USUARIO_ID = "usuarioId";
    private static final String CLAIM_ROL = "rol";

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtService(
            @Value("${jwt.secret:clave_secreta_por_defecto_para_desarrollo_local_de_al_menos_256_bits_memelomanos_finder_conciertos}") String secret,
            @Value("${jwt.expiration-ms:" + EXPIRACION_POR_DEFECTO_MS + "}") long expirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generarToken(String correo, Long usuarioId) {
        return generarToken(correo, usuarioId, RolUsuario.USUARIO);
    }

    public String generarToken(String correo, Long usuarioId, RolUsuario rol) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expirationMs);
        RolUsuario rolEfectivo = rol == null ? RolUsuario.USUARIO : rol;

        return Jwts.builder()
                .subject(correo)
                .claim(CLAIM_USUARIO_ID, usuarioId)
                .claim(CLAIM_ROL, rolEfectivo.name())
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(secretKey)
                .compact();
    }

    public String extraerCorreo(String token) {
        return extraerClaim(token, Claims::getSubject);
    }

    public Long extraerUsuarioId(String token) {
        Claims claims = extraerTodosLosClaims(token);
        Object id = claims.get(CLAIM_USUARIO_ID);
        if (id instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    public RolUsuario extraerRol(String token) {
        Claims claims = extraerTodosLosClaims(token);
        Object rol = claims.get(CLAIM_ROL);
        if (rol instanceof String nombreRol && !nombreRol.isBlank()) {
            try {
                return RolUsuario.valueOf(nombreRol);
            } catch (IllegalArgumentException ignored) {
                return RolUsuario.USUARIO;
            }
        }
        return RolUsuario.USUARIO;
    }

    public boolean esTokenValido(String token, String correo) {
        try {
            String correoToken = extraerCorreo(token);
            return (correoToken != null && correoToken.equals(correo) && !estaExpirado(token));
        } catch (Exception e) {
            return false;
        }
    }

    private boolean estaExpirado(String token) {
        return extraerClaim(token, Claims::getExpiration).before(new Date());
    }

    public <T> T extraerClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extraerTodosLosClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extraerTodosLosClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
