package com.aad.microservice.customer_payment_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractPaymentInfo {
    private Long contractId;
    private String contractCode;
    private LocalDate startingDate;
    private LocalDate endingDate;
    private Double totalAmount;
    private Double totalPaid;
    private Double totalDue;
    private String customerName; // Tên khách hàng (từ fullname)
    private Long customerId;
    private Integer status;
}
