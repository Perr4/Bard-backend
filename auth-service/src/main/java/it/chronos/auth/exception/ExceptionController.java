package it.chronos.auth.exception;

import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * Gestione degli errori di auth-service.
 *
 * <p>Prima non esisteva: un input non valido o una credenziale sbagliata
 * rispondevano con un 403 contenente il letterale "%s".</p>
 */
@RestControllerAdvice
public class ExceptionController {

    /**
     * Input non valido: raccoglie i messaggi dei campi.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse validazioneFallita(MethodArgumentNotValidException ex) {
        String dettagli = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ErrorResponse.builder(ex, HttpStatus.BAD_REQUEST, dettagli).build();
    }

    /**
     * Credenziali errate: 401, non 403.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ErrorResponse credenzialiErrate(BadCredentialsException ex) {
        return ErrorResponse.builder(ex, HttpStatus.UNAUTHORIZED, "Credenziali non valide").build();
    }

    /**
     * Errori dichiarati con {@link ResponseStatusException}
     * (409 email duplicata, 401 login fallito, 404 utente non trovato).
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ErrorResponse statusException(ResponseStatusException ex) {
        return ErrorResponse.builder(ex, ex.getStatusCode(), ex.getReason()).build();
    }
}
