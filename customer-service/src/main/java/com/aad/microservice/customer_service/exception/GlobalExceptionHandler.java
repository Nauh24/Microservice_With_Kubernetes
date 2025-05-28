package com.aad.microservice.customer_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<Map<String, Object>> handleAppException(AppException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", ex.getCode().getCode());
        body.put("message", ex.getMessage());

        HttpStatus status = HttpStatus.BAD_REQUEST;

        // Determine HTTP status based on error code
        if (ex.getCode() == ErrorCode.NotFound_Exception) {
            status = HttpStatus.NOT_FOUND;
        } else if (ex.getCode() == ErrorCode.Duplicated_Exception) {
            status = HttpStatus.BAD_REQUEST;
        } else if (ex.getCode() == ErrorCode.NotAllowCreate_Exception) {
            status = HttpStatus.BAD_REQUEST;
        }

        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", ErrorCode.Unknown_Exception.getCode());
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
