package it.chronos.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

/**
 * Contenitore dei DTO di request/response dell'auth-service.
 *
 * <p>I record con annotazioni di validazione ({@code NotBlank}, {@code Email}, {@code Size})
 * sono quelli di ingresso; gli altri sono DTO di uscita e non richiedono validazione.</p>
 */
public final class AuthDtos {

    private AuthDtos() {
    }

    /**
     * Richiesta di login: credenziali dell'utente.
     */
    public record LoginRequest(
            /** Email dell'utente. Obbligatoria e in formato valido. */
            @NotBlank(message = "l'email e obbligatoria")
            @Email(message = "l'email non e valida")
            String email,
            /** Password dell'utente. Obbligatoria. */
            @NotBlank(message = "la password e obbligatoria")
            String password) {
    }

    /**
     * Richiesta di registrazione di un nuovo utente.
     */
    public record RegisterRequest(
            /** Email dell'utente. Obbligatoria e in formato valido. */
            @NotBlank(message = "l'email e obbligatoria")
            @Email(message = "l'email non e valida")
            String email,
            /** Nome dell'utente. Obbligatorio, max 64 caratteri. */
            @NotBlank(message = "il nome e obbligatorio")
            @Size(max = 64, message = "il nome non puo superare i 64 caratteri")
            String firstName,
            /** Cognome dell'utente. Obbligatorio, max 64 caratteri. */
            @NotBlank(message = "il cognome e obbligatorio")
            @Size(max = 64, message = "il cognome non puo superare i 64 caratteri")
            String lastName,
            /** Password dell'utente. Obbligatoria, tra 8 e 128 caratteri. */
            @NotBlank(message = "la password e obbligatoria")
            @Size(min = 8, max = 128, message = "la password deve avere tra 8 e 128 caratteri")
            String password,
            /** Ruolo richiesto. Opzionale: se assente viene assegnato il ruolo di default. */
            @Size(max = 32, message = "il ruolo non puo superare i 32 caratteri")
            String role) {
    }

    /**
     * Risposta di login/registrazione: token JWT emesso e dati dell'utente.
     */
    public record AuthResponse(
            /** Token JWT firmato, da inviare nell'header {@code Authorization}. */
            String token,
            /** ID utente assegnato dal sistema. */
            UUID userId,
            /** Email dell'utente. */
            String email,
            /** Nome e cognome dell'utente. */
            String fullName,
            /** Ruoli assegnati all'utente. */
            List<String> roles) {
    }

    /**
     * Profilo dell'utente autenticato.
     */
    public record UserProfile(
            /** ID utente assegnato dal sistema. */
            UUID userId,
            /** Email dell'utente. */
            String email,
            /** Nome e cognome dell'utente. */
            String fullName,
            /** Ruoli assegnati all'utente. */
            List<String> roles) {
    }
}
