package com.aad.microservice.customer_payment_service.constant;

public class ContractStatusConstants {
    public static final int PENDING = 0;
    public static final int ACTIVE = 1;
    public static final int COMPLETED = 2;
    public static final int CANCELLED = 3;
    
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
