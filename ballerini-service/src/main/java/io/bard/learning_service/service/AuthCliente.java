package io.bard.learning_service.service;

import io.bard.learning_service.dto.UserCreate;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

/**
 * Client Feign verso auth-service.
 *
 * <p>L'anagrafica appartiene a auth-service: ballerini-service non conosce
 * l'entita' {@code UserAccount}, ma solo questi DTO di lettura/creazione.</p>
 */
@FeignClient(name = "AUTH-SERVICE")
public interface AuthCliente {

    /**
     * Anagrafica dell'utente, per comporre il DTO di lettura.
     *
     * @throws org.springframework.cloud.openfeign.FeignException.NotFound se l'utente non esiste
     */
    @GetMapping("/api/auth/internal/users/{id}")
    UserAnagrafica findAnagrafica(@PathVariable("id") UUID id);

    /**
     * Registra l'utente in auth-service (endpoint pubblico {@code /api/auth/register}).
     */
    @PostMapping("/api/auth/register")
    RegisterResponse register(@RequestBody UserCreate user);

    /** Anagrafica letta da auth-service (sola lettura). */
    record UserAnagrafica(
            UUID userId,
            String firstName,
            String lastName,
            java.sql.Date dataNascita) {
    }

    /** Esito della registrazione: serve lo {@code userId} per il profilo. */
    record RegisterResponse(
            UUID userId,
            String email,
            String fullName,
            List<String> roles) {
    }
}
