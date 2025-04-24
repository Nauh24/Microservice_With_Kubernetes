package com.aad.microservice.customer_payment_service.constant;

public class PaymentStatusConstants {
    public static final int UNPAID = 0;     // Chưa thanh toán
    public static final int PAID = 1;       // Đã thanh toán

    public static String getStatusName(int status) {
        return switch (status) {
            case UNPAID -> "Chưa thanh toán";
            case PAID -> "Đã thanh toán";
            default -> "Không xác định";
        };
    }
}
