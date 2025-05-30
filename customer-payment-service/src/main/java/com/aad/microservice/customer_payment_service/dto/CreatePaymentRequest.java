package com.aad.microservice.customer_payment_service.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaymentRequest {
    private LocalDateTime paymentDate;
    private Integer paymentMethod;
    private Double totalAmount;
    private String note;
    private Long customerId;
    private List<ContractPaymentDto> contractPayments;
}
