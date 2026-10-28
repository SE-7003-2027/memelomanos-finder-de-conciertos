package com.memelomanos.finderconciertos.security;

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

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtService(
            @Value("${jwt.secret:clave_secreta_por_defecto_para_desarrollo_local_de_al_menos_256_bits_memelomanos_finder_conciertos}") String secret,
            @Value("${jwt.expiration-ms:86400000}") long expirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generarToken(String correo, Long usuarioId) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expirationMs);

        return Jwts.builder()
                .subject(correo)
                .claim("usuarioId", usuarioId)
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
        Object id = claims.get("usuarioId");
        if (id instanceof Number number) {
            return number.longValue();
        }
        return null;
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
