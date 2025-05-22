package com.aad.microservice.customer_contract_service.service;

import com.aad.microservice.customer_contract_service.model.JobDetail;

import java.util.List;

/**
 * Service interface for JobDetail entity
 */
public interface JobDetailService {
    
    /**
     * Create a new job detail
     * @param jobDetail Job detail to create
     * @return Created job detail
     */
    JobDetail createJobDetail(JobDetail jobDetail);
    
    /**
     * Update an existing job detail
     * @param jobDetail Job detail to update
     * @return Updated job detail
     */
    JobDetail updateJobDetail(JobDetail jobDetail);
    
    /**
     * Delete a job detail
     * @param id Job detail ID
     */
    void deleteJobDetail(Long id);
    
    /**
     * Get a job detail by ID
     * @param id Job detail ID
     * @return Job detail
     */
    JobDetail getJobDetailById(Long id);
    
    /**
     * Get all job details
     * @return List of job details
     */
    List<JobDetail> getAllJobDetails();
    
    /**
     * Get all job details for a contract
     * @param contractId Contract ID
     * @return List of job details
     */
    List<JobDetail> getJobDetailsByContractId(Long contractId);
    
    /**
     * Get all job details for a job category
     * @param jobCategoryId Job category ID
     * @return List of job details
     */
    List<JobDetail> getJobDetailsByJobCategoryId(Long jobCategoryId);
    
    /**
     * Check if a job detail exists
     * @param id Job detail ID
     * @return True if the job detail exists, false otherwise
     */
    boolean checkJobDetailExists(Long id);
}
