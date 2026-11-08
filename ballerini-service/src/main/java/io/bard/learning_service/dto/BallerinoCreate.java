package io.bard.learning_service.dto;

import io.bard.common.model.Sezione;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class BallerinoCreate {

    @NotNull(message = "l'id utente e obbligatorio")
    private UUID userId;

    private Integer eta;
    private Sezione sezione;
    private String sesso;

}
