package com.aad.microservice.customer_payment_service.model;

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
    // private Integer numberOfWorkers; 
    private Double totalAmount;    
    private Double totalPaid;     
    private String address;      
    private String description;  
    private Long jobCategoryId;    
    private Long customerId;    
    private String customerName;   
    private Integer status;      
    private Boolean isDeleted;   
    private LocalDateTime createdAt; 
    private LocalDateTime updatedAt; 

    private transient Double totalDue; 

    public Double getTotalDue() {
        if (totalDue != null) {
            return totalDue;
        }

        if (totalAmount == null) {
            return 0.0;
        }

        if (totalPaid == null) {
            return totalAmount;
        }

        return totalAmount - totalPaid;
    }

    public void setTotalDue(Double totalDue) {
        this.totalDue = totalDue;
    }
}
