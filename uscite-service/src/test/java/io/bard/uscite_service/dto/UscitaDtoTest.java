package io.bard.uscite_service.dto;

import io.bard.uscite_service.model.Numero;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test del DTO {@link UscitaDto}: stato iniziale, round-trip e validazione.
 */
class UscitaDtoTest {

    @Test
    void shouldBeEmptyByDefault() {
        UscitaDto dto = new UscitaDto();

        assertNull(dto.getId());
        assertNull(dto.getData());
        assertNull(dto.getLuogo());
        assertNull(dto.getTipologia());
        assertNull(dto.getPartecipazioni());
    }

    @Test
    void gettersAndSettersShouldRoundTrip() {
        UUID id = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
        UUID ballerinoId = UUID.fromString("605f7d60-824d-4048-8747-231c2a2c73ae");

        UscitaDto dto = new UscitaDto();
        dto.setId(id);
        dto.setData(Date.valueOf("2026-09-07"));
        dto.setLuogo("Piazza Eleonora, Cagliari");
        dto.setTipologia("Festa paesana");
        PartecipazioneDto p = new PartecipazioneDto();
        p.setBallerinoId(ballerinoId);
        p.setNumero(Numero.PASSU);
        dto.setPartecipazioni(List.of(p));

        assertEquals(id, dto.getId());
        assertEquals(Date.valueOf("2026-09-07"), dto.getData());
        assertEquals("Piazza Eleonora, Cagliari", dto.getLuogo());
        assertEquals("Festa paesana", dto.getTipologia());
        assertEquals(1, dto.getPartecipazioni().size());
        assertEquals(ballerinoId, dto.getPartecipazioni().get(0).getBallerinoId());
        assertEquals(Numero.PASSU, dto.getPartecipazioni().get(0).getNumero());
    }

    @Test
    void shouldRejectMissingRequiredFields() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            UscitaDto dto = new UscitaDto();

            var violations = validator.validate(dto);

            assertEquals(2, violations.size(), () -> "attese 2 violazioni, trovate: " + violations);
        }
    }

    @Test
    void shouldAcceptValidUscita() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            UscitaDto dto = new UscitaDto();
            dto.setData(Date.valueOf("2026-09-07"));
            dto.setLuogo("Piazza Eleonora, Cagliari");

            var violations = validator.validate(dto);

            assertTrue(violations.isEmpty(), () -> "violazioni inattese: " + violations);
        }
    }
}
