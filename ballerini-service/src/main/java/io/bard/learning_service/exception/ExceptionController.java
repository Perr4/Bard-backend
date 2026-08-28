package io.bard.learning_service.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Controller;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionController {

    @ExceptionHandler(EntityNotFoundException.class)
    public ErrorResponse entityNotFound(EntityNotFoundException error) {
        return ErrorResponse.builder(
                error,
                ProblemDetail.forStatus(HttpStatus.NOT_FOUND)
        ).build();
    }

}
