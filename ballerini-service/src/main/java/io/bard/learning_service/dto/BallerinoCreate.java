package io.bard.learning_service.dto;

import io.bard.common.model.Sezione;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Richiesta di creazione di un ballerino.
 *
 * <p>Contiene l'utente da creare in auth-service ({@link UserCreate}) e i campi
 * del profilo ballerino. Il servizio registra prima l'utente e poi salva il
 * profilo con lo {@code userId} restituito.</p>
 */
@Getter
@Setter
public class BallerinoCreate {

    @NotNull(message = "l'utente e obbligatorio")
    @Valid
    private UserCreate user;

    private Sezione sezione;
    private String sesso;

}
