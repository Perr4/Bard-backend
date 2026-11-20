package io.bard.uscite_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Date;
import java.util.List;
import java.util.UUID;

/**
 * DTO di un'uscita: DOVE, QUANDO, TIPOLOGIA e la griglia dei partecipanti.
 */
@Setter
@Getter
@ToString
@Schema(description = "DTO representing a group outing (uscita)")
public class UscitaDto {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    /** QUANDO */
    @NotNull(message = "la data dell'uscita e obbligatoria")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Schema(description = "Date of the outing", example = "2026-09-07", requiredMode = Schema.RequiredMode.REQUIRED)
    private Date data;

    /** DOVE */
    @NotNull(message = "il luogo dell'uscita e obbligatorio")
    @Schema(description = "Place of the outing", example = "Piazza Eleonora, Cagliari", requiredMode = Schema.RequiredMode.REQUIRED)
    private String luogo;

    /** TIPOLOGIA */
    @Schema(description = "Kind of outing", example = "Festa paesana")
    private String tipologia;

    /** La griglia: una riga per ballerino, una colonna per numero. */
    @Schema(description = "Participants and the numbers they perform")
    private List<PartecipazioneDto> partecipazioni;
}
