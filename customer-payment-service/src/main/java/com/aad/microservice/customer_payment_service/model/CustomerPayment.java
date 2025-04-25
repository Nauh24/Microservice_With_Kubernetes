package com.aad.microservice.customer_payment_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String paymentCode;          // Mã thanh toán
    private LocalDateTime paymentDate;   // Ngày thanh toán
    private Integer paymentMethod;       // Phương thức thanh toán
    private Double paymentAmount;        // Số tiền thanh toán
    private String note;                 // Ghi chú thanh toán

    // Quan hệ với các đối tượng khác
    private Long customerContractId;     // ID hợp đồng
    private Long customerId;             // ID khách hàng

    // Trạng thái và thông tin hệ thống
    private Boolean isDeleted;       // Đánh dấu đã xóa
    private LocalDateTime createdAt; // Thời gian tạo
    private LocalDateTime updatedAt; // Thời gian cập nhật
}
