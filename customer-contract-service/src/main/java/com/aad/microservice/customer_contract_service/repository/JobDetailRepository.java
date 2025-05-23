package com.aad.microservice.customer_contract_service.repository;

import com.aad.microservice.customer_contract_service.model.JobDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for JobDetail entity
 */
public interface JobDetailRepository extends JpaRepository<JobDetail, Long> {
    
    List<JobDetail> findByIsDeletedFalse();

    Optional<JobDetail> findByIdAndIsDeletedFalse(Long id);
    
    List<JobDetail> findByContract_IdAndIsDeletedFalse(Long contractId);

    List<JobDetail> findByJobCategoryIdAndIsDeletedFalse(Long jobCategoryId);
}
