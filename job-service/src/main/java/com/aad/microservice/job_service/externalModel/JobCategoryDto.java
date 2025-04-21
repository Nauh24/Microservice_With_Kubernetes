package com.aad.microservice.job_service.externalModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobCategoryDto {
    private Long id;
    private String name;
    private String description;
    private Double baseSalary;
}
