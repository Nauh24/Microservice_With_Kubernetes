package com.aad.microservice.customer_payment_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long contractId;             // ID hợp đồng
    private String contractCode;         // Mã hợp đồng
    private String jobName;              // Tên đầu việc
    private String paymentPeriod;        // Kỳ thanh toán (ví dụ: "Tuần 3 - Tháng 4/2025")
    private Double amount;               // Số tiền cần thanh toán
    private Integer status;              // Trạng thái: 0 - Chưa trả, 1 - Đã trả
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_payment_id")
    private CustomerPayment customerPayment;
    
    private Boolean isDeleted;           // Đánh dấu đã xóa
    private LocalDateTime createdAt;     // Thời gian tạo
    private LocalDateTime updatedAt;     // Thời gian cập nhật
}
