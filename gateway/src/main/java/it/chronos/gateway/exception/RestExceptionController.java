package it.chronos.gateway.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Gestione degli errori del gateway.
 *
 * <p>Un token scaduto o non valido non e' un errore di input (400): e' un problema
 * di autenticazione, quindi risponde <b>401</b>.</p>
 */
@RestControllerAdvice
public class RestExceptionController {

    @ExceptionHandler(ExpiredJwtException.class)
    public ErrorResponse jwtExpired(ExpiredJwtException ex) {
        return ErrorResponse.create(ex, HttpStatus.UNAUTHORIZED, "Il token e' scaduto");
    }

    @ExceptionHandler(JwtException.class)
    public ErrorResponse jwtInvalid(JwtException ex) {
        return ErrorResponse.create(ex, HttpStatus.UNAUTHORIZED, "Il token non e' valido");
    }
}
