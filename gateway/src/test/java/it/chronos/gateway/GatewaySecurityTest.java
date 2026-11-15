package it.chronos.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Test della configurazione di sicurezza del gateway.
 *
 * <p>Verifica che gli endpoint pubblici siano raggiungibili e che gli altri
 * richiedano autenticazione. Non serve alcun servizio a valle: la risposta
 * attesa e' 401 (sicurezza) oppure un codice diverso da 401 (endpoint pubblico,
 * che senza downstream risponde 503).</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "bard.security.jwt-secret=test-secret-key-per-i-test-di-jwt-0123456789")
class GatewaySecurityTest {

    @LocalServerPort
    int port;

    private WebTestClient client() {
        return WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    void endpointProtettoSenzaTokenDa401() {
        client().get().uri("/api/ballerino")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void endpointProtettoConTokenFalsoDa401() {
        client().get().uri("/api/ballerino")
                .header("Authorization", "Bearer token-falso")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void endpointPubblicoTokenNonDa401() {
        // /api/auth/token e' pubblico: senza downstream risponde 503, non 401
        HttpStatusCode status = client().post().uri("/api/auth/token")
                .exchange()
                .returnResult(Void.class)
                .getStatus();

        assertThat(status).isNotEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void loginEPubblico() {
        HttpStatusCode status = client().post().uri("/api/auth/login")
                .exchange()
                .returnResult(Void.class)
                .getStatus();

        assertThat(status).isNotEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
