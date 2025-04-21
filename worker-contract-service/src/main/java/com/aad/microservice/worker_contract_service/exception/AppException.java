package com.aad.microservice.worker_contract_service.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppException extends RuntimeException{
    private ErrorCode code;
    private String message;
    public AppException(ErrorCode code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}
