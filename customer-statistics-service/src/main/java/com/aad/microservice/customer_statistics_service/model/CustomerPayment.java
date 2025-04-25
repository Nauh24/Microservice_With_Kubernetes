package com.aad.microservice.customer_statistics_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerPayment {
    private Long id;
    private String paymentCode;
    private LocalDate paymentDate;
    private Double amount;
    private String paymentMethod;
    private String description;
    private Long contractId;
    private String contractCode;
    private Long customerId;
    private LocalDateTime createdAt;
}
