package com.aad.microservice.worker_contract_service.repository;

import com.aad.microservice.worker_contract_service.model.WorkerContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface WorkerContractRepository extends JpaRepository<WorkerContract, Long> {
    List<WorkerContract> findByWorkerIdAndIsDeletedFalse(Long workerId);
    List<WorkerContract> findByIsDeletedFalse();

    // Non flict ~ :start >= c.endDate OR :end <= c.startDate
    @Query("SELECT c FROM WorkerContract c WHERE c.workerId = :workerId AND c.isDeleted = false " +
            "AND (:startDate < c.endDate AND :endDate > c.startDate)")
    List<WorkerContract> findConflictingContracts(Long workerId, LocalDate startDate, LocalDate endDate);
}
