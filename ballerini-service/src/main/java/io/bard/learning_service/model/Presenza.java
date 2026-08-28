package io.bard.learning_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Presenza {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "presenzaId")
    private UUID id;
    private Date date;
}
