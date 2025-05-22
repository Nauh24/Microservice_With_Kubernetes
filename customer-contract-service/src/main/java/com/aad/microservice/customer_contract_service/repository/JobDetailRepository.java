package com.aad.microservice.customer_contract_service.repository;

import com.aad.microservice.customer_contract_service.model.JobDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for JobDetail entity
 */
public interface JobDetailRepository extends JpaRepository<JobDetail, Long> {
    
    /**
     * Find all job details that are not deleted
     * @return List of job details
     */
    List<JobDetail> findByIsDeletedFalse();
    
    /**
     * Find a job detail by ID that is not deleted
     * @param id Job detail ID
     * @return Optional containing the job detail if found
     */
    Optional<JobDetail> findByIdAndIsDeletedFalse(Long id);
    
    /**
     * Find all job details for a contract that are not deleted
     * @param contractId Contract ID
     * @return List of job details
     */
    List<JobDetail> findByContract_IdAndIsDeletedFalse(Long contractId);
    
    /**
     * Find all job details for a job category that are not deleted
     * @param jobCategoryId Job category ID
     * @return List of job details
     */
    List<JobDetail> findByJobCategoryIdAndIsDeletedFalse(Long jobCategoryId);
}
