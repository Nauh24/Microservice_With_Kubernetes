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

    @GetMapping("/{id}")
    public ResponseEntity<JobDetail> getJobDetailById(@PathVariable Long id) {
        JobDetail jobDetail = jobDetailService.getJobDetailById(id);
        return ResponseEntity.ok(jobDetail);
    }

    @PostMapping
    public ResponseEntity<JobDetail> createJobDetail(@RequestBody JobDetail jobDetail) {
        return ResponseEntity.ok(jobDetailService.createJobDetail(jobDetail));
    }
    
    @GetMapping
    public ResponseEntity<List<JobDetail>> getAllJobDetails() {
        return ResponseEntity.ok(jobDetailService.getAllJobDetails());
    }

    @PutMapping
    public ResponseEntity<JobDetail> updateJobDetail(@RequestBody JobDetail jobDetail) {
        return ResponseEntity.ok(jobDetailService.updateJobDetail(jobDetail));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJobDetail(@PathVariable Long id) {
        jobDetailService.deleteJobDetail(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/contract/{contractId}")
    public ResponseEntity<List<JobDetail>> getJobDetailsByContractId(@PathVariable Long contractId) {
        return ResponseEntity.ok(jobDetailService.getJobDetailsByContractId(contractId));
    }

    @GetMapping("/job-category/{jobCategoryId}")
    public ResponseEntity<List<JobDetail>> getJobDetailsByJobCategoryId(@PathVariable Long jobCategoryId) {
        return ResponseEntity.ok(jobDetailService.getJobDetailsByJobCategoryId(jobCategoryId));
    }
 
    @GetMapping("/{id}/check-job-detail-exists")
    public ResponseEntity<Boolean> checkJobDetailExists(@PathVariable Long id) {
        boolean exists = jobDetailService.checkJobDetailExists(id);
        return ResponseEntity.ok(exists);
    }
}
