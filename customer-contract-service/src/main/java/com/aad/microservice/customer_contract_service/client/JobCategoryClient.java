package com.aad.microservice.customer_contract_service.client;

import com.aad.microservice.customer_contract_service.model.JobCategory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "job-service", url = "${job-category.service.url:http://localhost:8086/api/job-category}")
public interface JobCategoryClient {

    @GetMapping("/{id}")
    JobCategory getJobCategoryById(@PathVariable Long id);

    @GetMapping("/{id}/check-job-category-exists")
    Boolean checkJobCategoryExists(@PathVariable Long id);
}
