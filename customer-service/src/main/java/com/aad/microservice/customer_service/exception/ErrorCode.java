package com.aad.microservice.customer_service.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    Unknow_Exception(999),
    NotFound_Exception(1000),
    NotAllowCreate_Exception(1001),
    NotAllowUpdate_Exception(1002),
    NotAllowDelete_Exception(1003),
    Duplicated_Exception(1004),
    ;

    ErrorCode(int code) {
        this.code = code;
    }

    private int code;
}
