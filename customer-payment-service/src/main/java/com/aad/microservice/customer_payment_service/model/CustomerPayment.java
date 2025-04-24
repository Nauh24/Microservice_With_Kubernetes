package com.aad.microservice.customer_payment_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private Long customerId;             // ID khách hàng
    private Double totalAmount;          // Tổng số tiền thanh toán
    private LocalDateTime paymentDate;   // Ngày thanh toán
    
    @OneToMany(mappedBy = "customerPayment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentDetail> paymentDetails = new ArrayList<>();
    
    private Boolean isDeleted;           // Đánh dấu đã xóa
    private LocalDateTime createdAt;     // Thời gian tạo
    private LocalDateTime updatedAt;     // Thời gian cập nhật
    
    // Helper method to add payment detail
    public void addPaymentDetail(PaymentDetail paymentDetail) {
        paymentDetails.add(paymentDetail);
        paymentDetail.setCustomerPayment(this);
    }
}
