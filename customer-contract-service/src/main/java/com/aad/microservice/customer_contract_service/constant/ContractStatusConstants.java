package com.aad.microservice.customer_contract_service.constant;

public class ContractStatusConstants {
    public static final int PENDING = 0;     // Chờ xử lý
    public static final int ACTIVE = 1;      // Đang hoạt động
    public static final int COMPLETED = 2;   // Hoàn thành
    public static final int CANCELLED = 3;   // Đã hủy

    public static String getStatusName(int status) {
        return switch (status) {
            case PENDING -> "Chờ xử lý";
            case ACTIVE -> "Đang hoạt động";
            case COMPLETED -> "Hoàn thành";
            case CANCELLED -> "Đã hủy";
            default -> "Không xác định";
        };
    }
}
