package com.aad.microservice.customer_contract_service.repository;

import com.aad.microservice.customer_contract_service.model.WorkShift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for WorkShift entity
 */
public interface WorkShiftRepository extends JpaRepository<WorkShift, Long> {
    
    /**
     * Find all work shifts that are not deleted
     * @return List of work shifts
     */
    List<WorkShift> findByIsDeletedFalse();
    
    /**
     * Find a work shift by ID that is not deleted
     * @param id Work shift ID
     * @return Optional containing the work shift if found
     */
    Optional<WorkShift> findByIdAndIsDeletedFalse(Long id);
    
    /**
     * Find all work shifts for a job detail that are not deleted
     * @param jobDetailId Job detail ID
     * @return List of work shifts
     */
    List<WorkShift> findByJobDetail_IdAndIsDeletedFalse(Long jobDetailId);
}
