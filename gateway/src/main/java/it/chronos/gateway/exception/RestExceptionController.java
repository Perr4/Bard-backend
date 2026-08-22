package it.chronos.gateway.exception;

import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionController {

    @ExceptionHandler(ExpiredJwtException.class)
    public ErrorResponse jwtExpired(ExpiredJwtException ex){
        return ErrorResponse.create(
                ex, HttpStatus.BAD_REQUEST,
                "Il token è scaduto"
        );
    }
}
