package it.chronos.gateway.security;

import static org.assertj.core.api.Assertions.assertThat;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.TestPropertySource;
import reactor.test.StepVerifier;

/**
 * Test del filtro JWT del gateway: un token valido autentica, uno invalido o
 * scaduto viene rifiutato.
 *
 * <p>La chiave e' la stessa impostata in {@code @TestPropertySource}, usata sia
 * dal manager (via {@code @Value}) sia qui per generare i token di prova.</p>
 */
@SpringBootTest
@TestPropertySource(properties = "bard.security.jwt-secret=test-secret-key-per-i-test-di-jwt-0123456789")
class JwtAuthenticationManagerTest {

    private static final String SECRET = "test-secret-key-per-i-test-di-jwt-0123456789";

    @Autowired
    ReactiveAuthenticationManager authenticationManager;

    private String token(Instant expiration) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();
        return Jwts.builder()
                .subject("test@bard.it")
                .claim("roles", List.of("ROLE_STUDENT"))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(key)
                .compact();
    }

    private Authentication auth(String token) {
        return new UsernamePasswordAuthenticationToken(token, token);
    }

    @Test
    void tokenValidoAutentica() {
        StepVerifier.create(authenticationManager.authenticate(auth(token(Instant.now().plusSeconds(600)))))
                .assertNext(a -> {
                    assertThat(a.isAuthenticated()).isTrue();
                    assertThat(a.getName()).isEqualTo("test@bard.it");
                    assertThat(a.getAuthorities()).extracting(Object::toString)
                            .containsExactly("ROLE_STUDENT");
                })
                .verifyComplete();
    }

    @Test
    void tokenScadutoVieneRifiutato() {
        StepVerifier.create(authenticationManager.authenticate(auth(token(Instant.now().minusSeconds(600)))))
                .expectError()
                .verify();
    }

    @Test
    void tokenMalformatoVieneRifiutato() {
        StepVerifier.create(authenticationManager.authenticate(auth("non-un-jwt")))
                .expectError()
                .verify();
    }
}
