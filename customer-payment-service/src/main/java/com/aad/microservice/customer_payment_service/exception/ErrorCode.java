package com.aad.microservice.customer_payment_service.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    Unknown_Exception(999),
    NotFound_Exception(1000),
    NotAllowCreate_Exception(1001),
    NotAllowUpdate_Exception(1002),
    NotAllowDelete_Exception(1003),
    Duplicated_Exception(1004),
    InvalidAmount_Exception(1005),
    ContractNotActive_Exception(1006),
    CustomerNotFound_Exception(1007),
    ContractNotFound_Exception(1008);

    ErrorCode(int code) {
        this.code = code;
    }

    private int code;
}
