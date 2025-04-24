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
public class PaymentPeriod {
    private Integer weekNumber;
    private Integer monthNumber;
    private Integer year;
    private LocalDate startDate;
    private LocalDate endDate;
    
    @Override
    public String toString() {
        return "Tuần " + weekNumber + " - Tháng " + monthNumber + "/" + year;
    }
}
