package io.bard.learning_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.sql.Date;

/**
 * Dati dell'utente da creare in auth-service quando si crea un ballerino.
 *
 * <p>E' un DTO locale di ballerini-service: <strong>non</strong> e' l'entita'
 * {@code UserAccount} di auth-service. L'anagrafica resta di proprieta' di
 * auth-service, che viene raggiunto via Feign ({@code AuthCliente}).</p>
 */
public record UserCreate(
        @NotBlank(message = "l'email e obbligatoria")
        @Email(message = "l'email non e valida")
        String email,

        @NotBlank(message = "la password e obbligatoria")
        @Size(min = 3, max = 12, message = "la password deve avere tra 3 e 12 caratteri")
        String password,

        @NotBlank(message = "il nome e obbligatorio")
        @Size(max = 64, message = "il nome non puo superare i 64 caratteri")
        String firstName,

        @NotBlank(message = "il cognome e obbligatorio")
        @Size(max = 64, message = "il cognome non puo superare i 64 caratteri")
        String lastName,

        @NotNull(message = "la data di nascita e obbligatoria")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        Date dataNascita) {
}
