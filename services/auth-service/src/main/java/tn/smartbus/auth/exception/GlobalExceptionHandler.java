package tn.smartbus.auth.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.*;
import org.springframework.dao.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handle(Exception e, HttpServletRequest request) {
    int status = 500;
    String message = "Unexpected server error";
    Map<String, String> fields = new LinkedHashMap<>();
    if (e instanceof org.springframework.web.servlet.resource.NoResourceFoundException) {
      status = 404;
      message = "Endpoint not found";
    } else if (e instanceof ApiException a) {
      status = a.status;
      message = a.getMessage();
    } else if (e instanceof MethodArgumentNotValidException v) {
      status = 400;
      message = "Please check the form";
      v.getBindingResult()
          .getFieldErrors()
          .forEach(f -> fields.put(f.getField(), f.getDefaultMessage()));
    } else if (e instanceof DuplicateKeyException) {
      status = 409;
      message = "A record with these unique values already exists";
    } else if (e instanceof OptimisticLockingFailureException) {
      status = 409;
      message = "Record changed. Refresh and try again";
    } else if (e instanceof AccessDeniedException) {
      status = 403;
      message = "Access denied";
    } else if (e instanceof IllegalArgumentException
        || e instanceof org.springframework.http.converter.HttpMessageNotReadableException
        || e
            instanceof
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException) {
      status = 400;
      message = "Invalid request values";
    } else if (e instanceof org.springframework.web.client.RestClientException) {
      status = 503;
      message = "A required service is unavailable";
    }
    return ResponseEntity.status(status)
        .body(
            new ApiError(
                Instant.now(), status, "HTTP_" + status, message, request.getRequestURI(), fields));
  }
}
