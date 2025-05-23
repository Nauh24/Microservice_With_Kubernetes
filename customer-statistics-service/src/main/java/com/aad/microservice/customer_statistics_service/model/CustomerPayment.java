package com.aad.microservice.customer_statistics_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerPayment {
    private Long id;
    private LocalDateTime paymentDate;
    private Integer paymentMethod;
    private Double paymentAmount;
    private String note;
    private Long customerContractId;
    private Long customerId;
    private Boolean isDeleted;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;
}
