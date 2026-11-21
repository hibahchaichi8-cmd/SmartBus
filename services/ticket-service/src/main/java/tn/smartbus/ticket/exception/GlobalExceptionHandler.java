package tn.smartbus.ticket.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(
            ApiException exception,
            HttpServletRequest request) {

        return respond(
                exception.getStatus(),
                exception.getMessage(),
                request,
                Map.of()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        Map<String, String> errors = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errors.putIfAbsent(
                        error.getField(),
                        error.getDefaultMessage() == null
                                ? "Invalid value"
                                : error.getDefaultMessage()
                ));

        return respond(
                400,
                "Please check the submitted information",
                request,
                errors
        );
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ErrorResponse> handleInvalidRequest(
            Exception exception,
            HttpServletRequest request) {

        return respond(
                400,
                "Invalid request values",
                request,
                Map.of()
        );
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(
            DuplicateKeyException exception,
            HttpServletRequest request) {

        return respond(
                409,
                "Duplicate record or purchase request",
                request,
                Map.of()
        );
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleConcurrentUpdate(
            OptimisticLockingFailureException exception,
            HttpServletRequest request) {

        return respond(
                409,
                "The ticket has changed. Refresh and try again",
                request,
                Map.of()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException exception,
            HttpServletRequest request) {

        return respond(
                403,
                "Access denied",
                request,
                Map.of()
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            NoResourceFoundException exception,
            HttpServletRequest request) {

        return respond(
                404,
                "Endpoint not found",
                request,
                Map.of()
        );
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ErrorResponse> handleDependencyFailure(
            RestClientException exception,
            HttpServletRequest request) {

        return respond(
                503,
                "A required service is unavailable",
                request,
                Map.of()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedError(
            Exception exception,
            HttpServletRequest request) {

        return respond(
                500,
                "Unexpected server error",
                request,
                Map.of()
        );
    }

    private ResponseEntity<ErrorResponse> respond(
            int status,
            String message,
            HttpServletRequest request,
            Map<String, String> errors) {

        ErrorResponse body = new ErrorResponse(
                Instant.now(),
                status,
                "HTTP_" + status,
                message,
                request.getRequestURI(),
                errors
        );

        return ResponseEntity.status(status).body(body);
    }

    public record ErrorResponse(
            Instant timestamp,
            int status,
            String code,
            String message,
            String path,
            Map<String, String> errors
    ) {
    }
}