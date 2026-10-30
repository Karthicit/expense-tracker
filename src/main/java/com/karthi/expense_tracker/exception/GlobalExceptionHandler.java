package com.karthi.expense_tracker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return body(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    // NEW in Step 7
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleStatus(ResponseStatusException ex) {
        return body(HttpStatus.valueOf(ex.getStatusCode().value()), ex.getReason(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> fieldErrors.putIfAbsent(fe.getField(), fe.getDefaultMessage()));
        return body(HttpStatus.BAD_REQUEST, "Validation failed", fieldErrors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleBadJson(HttpMessageNotReadableException ex) {
        return body(HttpStatus.BAD_REQUEST,
                "Malformed JSON or invalid value (type must be INCOME or EXPENSE, date must be yyyy-MM-dd)", null);
    }

    private ResponseEntity<Map<String, Object>> body(HttpStatus status, String message, Map<String, String> fieldErrors) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("status", status.value());
        map.put("error", status.getReasonPhrase());
        map.put("message", message);
        if (fieldErrors != null) {
            map.put("fieldErrors", fieldErrors);
        }
        return ResponseEntity.status(status).body(map);
    }
}