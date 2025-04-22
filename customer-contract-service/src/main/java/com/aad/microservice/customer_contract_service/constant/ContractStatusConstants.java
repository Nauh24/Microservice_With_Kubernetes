package com.aad.microservice.customer_contract_service.constant;

public class ContractStatusConstants {
    public static final int DRAFT = 0;       // Bản nháp
    public static final int PENDING = 1;     // Đang chờ ký
    public static final int ACTIVE = 2;      // Đang hoạt động
    public static final int COMPLETED = 3;   // Hoàn thành
    public static final int TERMINATED = 4;  // Chấm dứt trước hạn
    
    public static String getStatusName(int status) {
        return switch (status) {
            case DRAFT -> "Bản nháp";
            case PENDING -> "Đang chờ ký";
            case ACTIVE -> "Đang hoạt động";
            case COMPLETED -> "Hoàn thành";
            case TERMINATED -> "Chấm dứt";
            default -> "Không xác định";
        };
    }
}
