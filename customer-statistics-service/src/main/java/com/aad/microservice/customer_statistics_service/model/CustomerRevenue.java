package com.aad.microservice.customer_statistics_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerRevenue {
    private Long id;
    private String fullName;
    private String companyName;
    private String phoneNumber;
    private String address;
    private Integer contractCount;
    private Double totalRevenue;
}
