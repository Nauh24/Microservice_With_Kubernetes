package com.aad.microservice.customer_payment_service.exception;

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
        body.put("code", ex.getErrorCode().getCode());
        body.put("message", ex.getMessage());

        HttpStatus status = switch (ex.getErrorCode()) {
            case NotFound_Exception, CustomerNotFound_Exception, ContractNotFound_Exception, PaymentNotFound_Exception -> HttpStatus.NOT_FOUND;
            case NotAllowCreate_Exception, NotAllowUpdate_Exception, NotAllowDelete_Exception, InvalidDate_Exception, InvalidPayment_Exception -> HttpStatus.BAD_REQUEST;
            case Duplicated_Exception -> HttpStatus.CONFLICT;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };

        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", ErrorCode.Unknown_Exception.getCode());
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
