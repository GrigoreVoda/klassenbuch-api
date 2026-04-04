package com.grigore.klassenbuch.api.exception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 404 Not Found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorBody> handleNotFound(
            ResourceNotFoundException ex) {
        log.warn("Not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorBody.of(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    // 409 Conflict
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorBody> handleConflict(
            ConflictException ex) {
        log.warn("Conflict: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorBody.of(HttpStatus.CONFLICT, ex.getMessage()));
    }

    // 400 Validation failures
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status,
            WebRequest request) {

        List<String> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();

        log.warn("Validation failed: {}", errors);
        return ResponseEntity.badRequest()
                .body(new ValidationErrorBody(
                        Instant.now(), 400,
                        "Bad Request", "Validation failed", errors));
    }

    // 500 Catch-all
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorBody> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.internalServerError()
                .body(ErrorBody.of(HttpStatus.INTERNAL_SERVER_ERROR,
                        "An unexpected error occurred"));
    }

    // Error response records
    public record ErrorBody(
            Instant timestamp, int status,
            String error, String message) {
        static ErrorBody of(HttpStatus s, String msg) {
            return new ErrorBody(Instant.now(), s.value(),
                    s.getReasonPhrase(), msg);
        }
    }

    public record ValidationErrorBody(
            Instant timestamp, int status,
            String error, String message,
            List<String> errors) {}

    // Inner exception class for 409 Conflict
    public static class ConflictException extends RuntimeException {
        public ConflictException(String message) { super(message); }
    }
}
