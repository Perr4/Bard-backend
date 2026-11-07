package io.bard.learning_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

/**
 * Client HTTP verso auth-service: legge l'anagrafica di un utente.
 *
 * <p>L'URL di base e' configurabile tramite {@code auth.service.url}
 * (default {@code http://localhost:7999}).</p>
 */
@Component
public class AuthServiceClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public AuthServiceClient(
            RestTemplate restTemplate,
            @Value("${auth.service.url:http://localhost:7999}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    /**
     * Anagrafica dell'utente identificato da {@code userId}.
     *
     * @return l'anagrafica, oppure {@code null} se l'utente non esiste
     *         (auth-service risponde 404)
     */
    public UserAnagrafica findAnagrafica(UUID userId) {
        try {
            return restTemplate.getForObject(
                    baseUrl + "/api/auth/internal/users/{id}",
                    UserAnagrafica.class,
                    userId);
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        }
    }

    /**
     * Anagrafica letta da auth-service (sola lettura).
     */
    public record UserAnagrafica(
            UUID userId,
            String firstName,
            String lastName,
            java.sql.Date dataNascita) {
    }
}
