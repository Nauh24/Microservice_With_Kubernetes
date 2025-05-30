package com.aad.microservice.customer_payment_service.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractPaymentDto {
    private Long contractId;
    private Double allocatedAmount;
    private String contractDescription; // Để hiển thị thông tin hợp đồng
    private Double contractTotalAmount; // Tổng số tiền của hợp đồng
    private Double contractTotalPaid;   // Số tiền đã thanh toán trước đó
    private Double contractRemainingAmount; // Số tiền còn lại
}
