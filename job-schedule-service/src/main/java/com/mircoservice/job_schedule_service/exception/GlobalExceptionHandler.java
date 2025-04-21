package com.mircoservice.job_schedule_service.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    ResponseEntity<AppException> handleAppException(AppException ex) {
        return ResponseEntity.badRequest().body(ex);
    }
}
