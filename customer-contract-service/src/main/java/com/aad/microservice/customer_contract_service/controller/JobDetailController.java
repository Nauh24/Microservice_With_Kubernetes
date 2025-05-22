package com.aad.microservice.customer_contract_service.controller;

import com.aad.microservice.customer_contract_service.model.JobDetail;
import com.aad.microservice.customer_contract_service.service.JobDetailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for JobDetail entity
 */
@RestController
@RequestMapping("/api/job-detail")
public class JobDetailController {
    
    private final JobDetailService jobDetailService;
    
    public JobDetailController(JobDetailService jobDetailService) {
        this.jobDetailService = jobDetailService;
    }
    
    /**
     * Get a job detail by ID
     * @param id Job detail ID
     * @return ResponseEntity containing the job detail
     */
    @GetMapping("/{id}")
    public ResponseEntity<JobDetail> getJobDetailById(@PathVariable Long id) {
        JobDetail jobDetail = jobDetailService.getJobDetailById(id);
        return ResponseEntity.ok(jobDetail);
    }
    
    /**
     * Create a new job detail
     * @param jobDetail Job detail to create
     * @return ResponseEntity containing the created job detail
     */
    @PostMapping
    public ResponseEntity<JobDetail> createJobDetail(@RequestBody JobDetail jobDetail) {
        return ResponseEntity.ok(jobDetailService.createJobDetail(jobDetail));
    }
    
    /**
     * Get all job details
     * @return ResponseEntity containing the list of job details
     */
    @GetMapping
    public ResponseEntity<List<JobDetail>> getAllJobDetails() {
        return ResponseEntity.ok(jobDetailService.getAllJobDetails());
    }
    
    /**
     * Update a job detail
     * @param jobDetail Job detail to update
     * @return ResponseEntity containing the updated job detail
     */
    @PutMapping
    public ResponseEntity<JobDetail> updateJobDetail(@RequestBody JobDetail jobDetail) {
        return ResponseEntity.ok(jobDetailService.updateJobDetail(jobDetail));
    }
    
    /**
     * Delete a job detail
     * @param id Job detail ID
     * @return ResponseEntity with no content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJobDetail(@PathVariable Long id) {
        jobDetailService.deleteJobDetail(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Get all job details for a contract
     * @param contractId Contract ID
     * @return ResponseEntity containing the list of job details
     */
    @GetMapping("/contract/{contractId}")
    public ResponseEntity<List<JobDetail>> getJobDetailsByContractId(@PathVariable Long contractId) {
        return ResponseEntity.ok(jobDetailService.getJobDetailsByContractId(contractId));
    }
    
    /**
     * Get all job details for a job category
     * @param jobCategoryId Job category ID
     * @return ResponseEntity containing the list of job details
     */
    @GetMapping("/job-category/{jobCategoryId}")
    public ResponseEntity<List<JobDetail>> getJobDetailsByJobCategoryId(@PathVariable Long jobCategoryId) {
        return ResponseEntity.ok(jobDetailService.getJobDetailsByJobCategoryId(jobCategoryId));
    }
    
    /**
     * Check if a job detail exists
     * @param id Job detail ID
     * @return ResponseEntity containing true if the job detail exists, false otherwise
     */
    @GetMapping("/{id}/check-job-detail-exists")
    public ResponseEntity<Boolean> checkJobDetailExists(@PathVariable Long id) {
        boolean exists = jobDetailService.checkJobDetailExists(id);
        return ResponseEntity.ok(exists);
    }
}
