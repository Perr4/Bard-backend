package io.bard.uscite_service.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Un'uscita del gruppo, come dal modulo del cliente:
 * DOVE ({@code luogo}), QUANDO ({@code data}), TIPOLOGIA ({@code tipologia})
 * e la griglia dei partecipanti per numero.
 */
@Entity
@Table(name = "uscite")
@Getter
@Setter
@ToString
public class Uscita {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** QUANDO */
    @Column(nullable = false)
    private Date data;

    /** DOVE */
    @Column(nullable = false)
    private String luogo;

    /** TIPOLOGIA */
    @Column
    private String tipologia;

    /** La griglia: una riga per ballerino, una colonna per numero. */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "uscita_partecipazione",
            joinColumns = @JoinColumn(name = "uscita_id", nullable = false),
            uniqueConstraints = @UniqueConstraint(
                    name = "uq_uscita_ballerino_numero",
                    columnNames = {"uscita_id", "ballerino_id", "numero"}))
    private List<Partecipazione> partecipazioni = new ArrayList<>();
}
