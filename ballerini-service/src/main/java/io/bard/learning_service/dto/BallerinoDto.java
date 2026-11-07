package io.bard.learning_service.dto;

import io.bard.learning_service.model.Sezione;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.util.UUID;

@Getter
@Setter
public class BallerinoDto {

    private UUID id;
    private UUID userId;
    private String firstName;
    private String lastName;
    private Date dataNascita;
    private Integer eta;
    private Sezione sezione;

}
