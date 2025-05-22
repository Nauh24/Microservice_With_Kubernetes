package com.aad.microservice.customer_statistics_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRevenue extends Customer {
    // Mark as JsonIgnore to exclude from API responses as per user preference
    @JsonIgnore
    private Integer contractCount;

    private Double totalRevenue;
}
