package io.bard.presenza_service.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Minimal unit tests for {@link PresenzaDto} (Lombok getters/setters/toString).
 */
class PresenzaDtoTest {

    @Test
    void shouldBeEmptyByDefault() {
        PresenzaDto dto = new PresenzaDto();

        assertNull(dto.getId());
        assertNull(dto.getDate());
        assertNull(dto.getBallerino_id());
    }

    @Test
    void gettersAndSettersShouldRoundTrip() {
        UUID id = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
        UUID ballerinoId = UUID.fromString("605f7d60-824d-4048-8747-231c2a2c73ae");
        Date date = Date.valueOf("2026-09-07");

        PresenzaDto dto = new PresenzaDto();
        dto.setId(id);
        dto.setBallerino_id(ballerinoId);
        dto.setDate(date);

        assertEquals(id, dto.getId());
        assertEquals(ballerinoId, dto.getBallerino_id());
        assertEquals(date, dto.getDate());
    }

    @Test
    void toStringShouldExposeFieldNamesAndValues() {
        PresenzaDto dto = new PresenzaDto();
        dto.setId(UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890"));
        dto.setBallerino_id(UUID.fromString("605f7d60-824d-4048-8747-231c2a2c73ae"));
        dto.setDate(Date.valueOf("2026-09-07"));

        String text = dto.toString();

        // Lombok @ToString generates "PresenzaDto(id=..., date=..., ballerino_id=...)"
        for (String expected : new String[]{"id=", "date=", "ballerino_id="}) {
            assertTrue(text.contains(expected),
                    "toString() should contain '" + expected + "' but was: " + text);
        }
    }

    @Test
    void shouldRejectMissingRequiredFields() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            PresenzaDto dto = new PresenzaDto();

            Set<ConstraintViolation<PresenzaDto>> violations = validator.validate(dto);

            assertEquals(2, violations.size(), () -> "attese 2 violazioni, trovate: " + violations);
        }
    }

    @Test
    void shouldAcceptValidPresenza() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            PresenzaDto dto = new PresenzaDto();
            dto.setDate(Date.valueOf("2026-09-07"));
            dto.setBallerino_id(UUID.randomUUID());

            Set<ConstraintViolation<PresenzaDto>> violations = validator.validate(dto);

            assertTrue(violations.isEmpty(), () -> "violazioni inattese: " + violations);
        }
    }
}
