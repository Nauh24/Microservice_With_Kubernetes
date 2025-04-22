package com.aad.microservice.customer_contract_service.model;

public enum ContractStatus {
    DRAFT,        // Bản nháp
    PENDING,      // Đang chờ ký
    ACTIVE,       // Đang hoạt động
    COMPLETED,    // Hoàn thành
    TERMINATED,   // Chấm dứt trước hạn
    EXPIRED       // Hết hạn
}
