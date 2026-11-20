package io.bard.uscite_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

/**
 * Una cella del modulo: il ballerino {@code ballerinoId} esegue il numero
 * {@code numero} nell'uscita.
 */
@Embeddable
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Partecipazione {

    @Column(name = "ballerino_id", nullable = false)
    private UUID ballerinoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "numero", nullable = false)
    private Numero numero;
}
