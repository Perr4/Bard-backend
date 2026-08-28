package io.bard.presenza_service.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Date;
import java.util.UUID;

@Setter
@Getter
@ToString
public class PresenzaDto {
    private Date date;
    private UUID ballerino_id;
}
