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
public class CustomerContract {
    private Long id;
    private String contractCode;
    private LocalDate startingDate;
    private LocalDate endingDate;
    private LocalDate signedDate;
    private Integer numberOfWorkers;
    private Double totalAmount;
    private Double totalPaid;
    private String address;
    private String description;
    private Long jobCategoryId;
    private Long customerId;
    private Integer status;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
