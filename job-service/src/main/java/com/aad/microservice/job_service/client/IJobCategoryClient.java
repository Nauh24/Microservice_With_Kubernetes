package com.aad.microservice.job_service.client;

import com.aad.microservice.job_service.externalModel.JobCategoryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "job-service", url = "http://localhost:8086/api/job-category")
public interface IJobCategoryClient {
    @RequestMapping(method = RequestMethod.GET, value = "/{id}/check-job-category-exists")
    Boolean checkJobCategoryExists(@PathVariable long id);

    @RequestMapping(method = RequestMethod.GET, value = "/{id}")
    JobCategoryDto getJobCategoryById(@PathVariable long id);
}
