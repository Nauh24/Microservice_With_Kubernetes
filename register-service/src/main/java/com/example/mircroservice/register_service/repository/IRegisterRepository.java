package com.example.mircroservice.register_service.repository;

import com.example.mircroservice.register_service.constant.RegisterStatus;
import com.example.mircroservice.register_service.model.Register;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IRegisterRepository extends JpaRepository<Register, Long> {
    @Query("SELECT r FROM Registers r WHERE r.jobId = :jobId AND r.status = :status AND r.isDeleted = false")
    List<Register> findByJobIdAndStatus(@Param("jobId") long jobId, @Param("status") RegisterStatus status);


    List<Register> findByWorkerIdAndIsDeletedFalse(Long workerId);
    boolean existsByWorkerIdAndJobIdAndIsDeletedFalse(Long workerId, Long jobId);

    List<Register> findByWorkerIdAndIsDeletedFalseAndStatusIn(Long workerId, List<RegisterStatus> statuses);

}
