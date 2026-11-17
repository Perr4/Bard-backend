package io.bard.presenza_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Date;
import java.util.UUID;

/**
 * DTO che rappresenta un record di presenza (attendance).
 *
 * <p>Usato sia in ingresso ({@code create} / {@code update}) sia in uscita dalle API
 * di {@code PresenzaController}. L'{@link #id} e generato dal server in fase di creazione
 * e deve essere fornito in fase di aggiornamento; non e quindi vincolato qui per non
 * impedire la creazione.</p>
 */
@Setter
@Getter
@ToString
@Schema(description = "DTO representing an attendance record (presenza)")
public class PresenzaDto {

    /**
     * Identificatore univoco del record di presenza.
     *
     * <p>Generato dal server in creazione; obbligatorio in aggiornamento.</p>
     */
    @Schema(description = "Unique identifier of the attendance record", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    /**
     * Data della presenza. Obbligatoria.
     */
    @NotNull(message = "la data della presenza e obbligatoria")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Schema(description = "Date of attendance", example = "2026-09-07", format = "date", requiredMode = Schema.RequiredMode.REQUIRED)
    private Date date;

    /**
     * UUID del ballerino a cui si riferisce la presenza. Obbligatorio.
     */
    @NotNull(message = "l'id del ballerino e obbligatorio")
    @Schema(description = "UUID of the ballerino (dancer)", example = "605f7d60-824d-4048-8747-231c2a2c73ae", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID ballerino_id;

}
