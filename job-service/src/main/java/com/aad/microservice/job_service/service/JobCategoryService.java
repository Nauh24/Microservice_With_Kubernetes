package com.aad.microservice.job_service.service;

import com.aad.microservice.job_service.model.JobCategory;

import java.util.List;

public interface JobCategoryService {
    JobCategory createJobCategory(JobCategory jobCategory);
    JobCategory updateJobCategory(JobCategory jobCategory);
    void deleteJobCategory(Long id);
    JobCategory getJobCategoryById(Long id);
    List<JobCategory> getAllJobCategories();
    
    boolean checkJobCategoryExists(Long id);
}
