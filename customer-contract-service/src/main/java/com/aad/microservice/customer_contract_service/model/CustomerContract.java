package com.aad.microservice.customer_contract_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_contracts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerContract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String contractCode;
    private Long customerId;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double totalValue;
    private String location;
    
    @Enumerated(EnumType.STRING)
    private ContractStatus status;
    
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
