package io.bard.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
    // nullable: non tutti i servizi (es. ballerini-service) popolano questo campo
    @Column(name = "ballerino_id")
    private UUID ballerinoId;
}
