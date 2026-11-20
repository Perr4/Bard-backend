package io.bard.uscite_service.exception;

import jakarta.persistence.EntityNotFoundException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * Gestione degli errori di uscite-service.
 *
 * <p>Formato uniforme RFC 7807 (ProblemDetail).</p>
 */
@RestControllerAdvice
public class ExceptionController {

    /**
     * Input non valido: raccoglie tutti i messaggi dei campi.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse validazioneFallita(MethodArgumentNotValidException ex) {
        String dettagli = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ErrorResponse.builder(ex, HttpStatus.BAD_REQUEST, dettagli).build();
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ErrorResponse nonTrovato(EntityNotFoundException ex) {
        return ErrorResponse.builder(ex, HttpStatus.NOT_FOUND, "Risorsa non trovata").build();
    }

    /**
     * Errori dichiarati con {@link ResponseStatusException} (es. 404).
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ErrorResponse statusException(ResponseStatusException ex) {
        return ErrorResponse.builder(ex, ex.getStatusCode(), ex.getReason()).build();
    }
}
