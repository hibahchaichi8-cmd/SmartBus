package tn.smartbus.notification.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handle(
            Exception exception,
            HttpServletRequest request) {

        int status = 500;
        String message = "Unexpected server error";

        Map<String, String> errors = new LinkedHashMap<>();

        if (exception instanceof ApiException apiException) {

            status = apiException.getStatus();
            message = apiException.getMessage();

        } else if (
                exception instanceof MethodArgumentNotValidException validation
        ) {

            status = 400;
            message = "Please check the submitted information";

            validation.getBindingResult()
                    .getFieldErrors()
                    .forEach(error -> errors.putIfAbsent(
                            error.getField(),
                            Objects.requireNonNullElse(
                                    error.getDefaultMessage(),
                                    "Invalid value"
                            )
                    ));

        } else if (
                exception instanceof HttpMessageNotReadableException
                || exception instanceof MethodArgumentTypeMismatchException
                || exception instanceof MissingServletRequestParameterException
                || exception instanceof IllegalArgumentException
        ) {

            status = 400;
            message = "Invalid request values";

        } else if (exception instanceof DuplicateKeyException) {

            status = 409;
            message = "Notification already exists";

        } else if (
                exception instanceof OptimisticLockingFailureException
        ) {

            status = 409;
            message = "Notification changed. Refresh and try again";

        } else if (exception instanceof AccessDeniedException) {

            status = 403;
            message = "Access denied";

        } else if (exception instanceof NoResourceFoundException) {

            status = 404;
            message = "Endpoint not found";
        }

        Map<String, Object> body = Map.of(
                "timestamp", Instant.now().toString(),
                "status", status,
                "code", "HTTP_" + status,
                "message", message,
                "path", request.getRequestURI(),
                "errors", errors
        );

        return ResponseEntity.status(status).body(body);
    }
}