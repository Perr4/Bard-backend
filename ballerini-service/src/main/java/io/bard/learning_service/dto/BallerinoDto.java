package io.bard.learning_service.dto;

import io.bard.learning_service.model.Sezione;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.sql.Date;
import java.util.UUID;

@Getter
@Setter
public class BallerinoDto {

    private UUID id;
    private String nome;
    private String cognome;
    private Integer eta;
    private Date dataNascita;
    private Sezione sezione;

}
