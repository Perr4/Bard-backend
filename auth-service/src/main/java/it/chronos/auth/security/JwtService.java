package it.chronos.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMinutes;

    public JwtService(
            @Value("${chronos.security.jwt-secret}") String secret,
            @Value("${chronos.security.jwt-expiration-minutes}") long expirationMinutes) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMinutes = expirationMinutes;
    }

    public String generateToken(String subject, List<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(subject)
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationMinutes * 60)))
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) {
        return claims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        return userDetails.getUsername().equals(extractUsername(token)) && !claims(token).getExpiration().before(new Date());
    }

    /**
     * Verifica la validita' di un token senza bisogno di uno UserDetails:
     * controlla firma, formato e scadenza.
     *
     * @return true solo se il token e' ben formato, firmato con la chiave corretta
     *         e non scaduto; false in tutti gli altri casi (token assente, vuoto,
     *         malformato, con chiave errata o scaduto)
     */
    public boolean isValid(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            return claims(token).getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private Claims claims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
