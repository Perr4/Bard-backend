package io.bard.uscite_service.dto;

import io.bard.uscite_service.model.Numero;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

/** Una cella del modulo: il ballerino {@code ballerinoId} esegue {@code numero}. */
@Setter
@Getter
@ToString
public class PartecipazioneDto {

    private UUID ballerinoId;
    private Numero numero;
}
