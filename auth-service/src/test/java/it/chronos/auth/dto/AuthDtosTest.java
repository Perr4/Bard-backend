package it.chronos.auth.dto;

import it.chronos.auth.dto.AuthDtos.AuthResponse;
import it.chronos.auth.dto.AuthDtos.LoginRequest;
import it.chronos.auth.dto.AuthDtos.RegisterRequest;
import it.chronos.auth.dto.AuthDtos.UserProfile;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Minimal unit tests for the DTO records in {@link AuthDtos}.
 */
class AuthDtosTest {

    @Test
    void loginRequestShouldExposeComponents() {
        LoginRequest dto = new LoginRequest("sara@example.it", "segretissimo");

        assertEquals("sara@example.it", dto.email());
        assertEquals("segretissimo", dto.password());
    }

    @Test
    void registerRequestShouldExposeComponents() {
        RegisterRequest dto = new RegisterRequest(
                "sara@example.it", "Sara", "Rossi", "segretissimo", "STUDENT", Date.valueOf("2000-01-15"));

        assertEquals("sara@example.it", dto.email());
        assertEquals("Sara", dto.firstName());
        assertEquals("Rossi", dto.lastName());
        assertEquals("segretissimo", dto.password());
        assertEquals("STUDENT", dto.role());
        assertEquals(Date.valueOf("2000-01-15"), dto.dataNascita());
    }

    @Test
    void authResponseShouldBeEqualToAnIdenticalCopy() {
        UUID userId = UUID.fromString("605f7d60-824d-4048-8747-231c2a2c73ae");
        List<String> roles = List.of("STUDENT");

        AuthResponse dto = new AuthResponse("token.jwt", userId, "sara@example.it", "Sara Rossi", roles);
        AuthResponse copy = new AuthResponse("token.jwt", userId, "sara@example.it", "Sara Rossi", roles);

        assertEquals(userId, dto.userId());
        assertEquals("token.jwt", dto.token());
        assertEquals(roles, dto.roles());
        assertEquals(copy, dto);
        assertEquals(copy.hashCode(), dto.hashCode());
    }

    @Test
    void authResponseShouldDifferOnToken() {
        UUID userId = UUID.fromString("605f7d60-824d-4048-8747-231c2a2c73ae");

        AuthResponse dto = new AuthResponse("token-a", userId, "sara@example.it", "Sara Rossi", List.of("STUDENT"));
        AuthResponse other = new AuthResponse("token-b", userId, "sara@example.it", "Sara Rossi", List.of("STUDENT"));

        assertNotEquals(dto, other);
    }

    @Test
    void userProfileShouldExposeComponentsAndEquality() {
        UUID userId = UUID.fromString("605f7d60-824d-4048-8747-231c2a2c73ae");
        List<String> roles = List.of("STUDENT", "TEACHER");

        UserProfile dto = new UserProfile(userId, "sara@example.it", "Sara Rossi", roles);
        UserProfile copy = new UserProfile(userId, "sara@example.it", "Sara Rossi", roles);

        assertEquals(userId, dto.userId());
        assertEquals("sara@example.it", dto.email());
        assertEquals("Sara Rossi", dto.fullName());
        assertEquals(roles, dto.roles());
        assertEquals(copy, dto);
    }

    @Test
    void loginRequestShouldRejectInvalidCredentials() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();

            Set<ConstraintViolation<LoginRequest>> violations =
                    validator.validate(new LoginRequest("non-una-email", ""));

            assertEquals(2, violations.size(), () -> "attese 2 violazioni, trovate: " + violations);
        }
    }

    @Test
    void registerRequestShouldRejectInvalidInput() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();

            Set<ConstraintViolation<RegisterRequest>> violations =
                    validator.validate(new RegisterRequest("non-una-email", "", "", "corta", "STUDENT", null));

            assertEquals(4, violations.size(), () -> "attese 4 violazioni, trovate: " + violations);
        }
    }

    @Test
    void registerRequestShouldAcceptValidInput() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();

            Set<ConstraintViolation<RegisterRequest>> violations =
                    validator.validate(new RegisterRequest("sara@example.it", "Sara", "Rossi", "segretissimo", null, Date.valueOf("2000-01-15")));

            assertTrue(violations.isEmpty(), () -> "violazioni inattese: " + violations);
        }
    }
}
