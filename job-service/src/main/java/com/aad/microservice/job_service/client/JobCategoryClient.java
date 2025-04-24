package com.aad.microservice.job_service.client;

import com.aad.microservice.job_service.model.JobCategory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "job-service", url = "http://localhost:8086/api/job-category")
public interface JobCategoryClient {
    @GetMapping("/{id}/check-job-category-exists")
    Boolean checkJobCategoryExists(@PathVariable long id);

    @GetMapping("/{id}")
    JobCategory getJobCategoryById(@PathVariable long id);
}
