package com.aad.microservice.customer_contract_service.repository;

import com.aad.microservice.customer_contract_service.model.WorkShift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for WorkShift entity
 */
public interface WorkShiftRepository extends JpaRepository<WorkShift, Long> {
    
    List<WorkShift> findByIsDeletedFalse();

    Optional<WorkShift> findByIdAndIsDeletedFalse(Long id);

    List<WorkShift> findByJobDetail_IdAndIsDeletedFalse(Long jobDetailId);
}
