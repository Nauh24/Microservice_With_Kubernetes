package com.aad.microservice.customer_payment_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private LocalDateTime paymentDate;  
    private Integer paymentMethod;      
    private Double paymentAmount;      
    private String note;
    private Long customerContractId;     
    private Long customerId;  
    private Boolean isDeleted;      
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt; 
}
