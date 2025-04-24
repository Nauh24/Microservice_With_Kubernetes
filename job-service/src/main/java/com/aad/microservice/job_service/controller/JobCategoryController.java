package com.aad.microservice.job_service.controller;

import com.aad.microservice.job_service.model.JobCategory;
import com.aad.microservice.job_service.service.JobCategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/job-category")
public class JobCategoryController {
    private final JobCategoryService jobCategoryService;

    public JobCategoryController(JobCategoryService jobCategoryService) {
        this.jobCategoryService = jobCategoryService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobCategory> getJobCategoryById(@PathVariable Long id) {
        JobCategory jobCategory = jobCategoryService.getJobCategoryById(id);
        return ResponseEntity.ok(jobCategory);
    }

    @PostMapping
    public ResponseEntity<JobCategory> createJobCategory(@RequestBody JobCategory jobCategory) {
        return ResponseEntity.ok(jobCategoryService.createJobCategory(jobCategory));
    }

    @GetMapping
    public ResponseEntity<List<JobCategory>> getAllJobCategories() {
        return ResponseEntity.ok(jobCategoryService.getAllJobCategories());
    }

    @PutMapping
    public ResponseEntity<JobCategory> updateJobCategory(@RequestBody JobCategory jobCategory) {
        return ResponseEntity.ok(jobCategoryService.updateJobCategory(jobCategory));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJobCategory(@PathVariable Long id) {
        jobCategoryService.deleteJobCategory(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/check-job-category-exists")
    public ResponseEntity<Boolean> checkJobCategoryExists(@PathVariable Long id) {
        boolean exists = jobCategoryService.checkJobCategoryExists(id);
        return ResponseEntity.ok(exists);
    }
}
