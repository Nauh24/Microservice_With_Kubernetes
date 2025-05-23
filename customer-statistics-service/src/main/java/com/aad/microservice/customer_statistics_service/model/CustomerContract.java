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
    private LocalDate startingDate;
    private LocalDate endingDate;
    private Integer numberOfWorkers;
    private Double totalAmount;
    private Double totalPaid;
    private String address;
    private String description;
    private Integer status;
    private Long jobCategoryId;
    private Long customerId;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
