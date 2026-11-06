package io.bard.learning_service.dto;

import io.bard.learning_service.model.Sezione;
import lombok.*;

import java.sql.Date;


@Getter
@Setter
public class BallerinoCreate {

    private String nome;
    private String cognome;
    private Integer eta;
    private Date dataNascita;
    private Sezione sezione;
    private String sesso;

}
