package org.inno.controller.advise;

import jakarta.persistence.EntityNotFoundException;
import org.apache.coyote.BadRequestException;
import org.hibernate.exception.ConstraintViolationException;
import org.inno.exception.DuplicateEmailException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ExceptionHandlerAdvice {
    @ExceptionHandler({IllegalArgumentException.class,
            NullPointerException.class,
            DataIntegrityViolationException.class,
            BadRequestException.class,
            ConstraintViolationException.class,
            MissingRequestHeaderException.class
    })
    public ResponseEntity<Object> badRequestException(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Input validation failed")
                .errors(errors)
                .path(ex.getParameter().getMethod().getName())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Void> handleNotFound(EntityNotFoundException e) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Void> handleConflict(DuplicateEmailException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
}
