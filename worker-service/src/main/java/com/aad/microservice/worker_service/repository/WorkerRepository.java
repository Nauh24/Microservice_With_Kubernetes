package com.aad.microservice.worker_service.repository;

import com.aad.microservice.worker_service.model.Worker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkerRepository extends JpaRepository<Worker, Long> {
    List<Worker> findByIsDeletedFalse();
    Boolean existsByEmailAndIsDeletedFalse(String email);
    Boolean existsByIdentityCardNoAndIsDeletedFalse(String identityCardNo);
    Boolean existsByEmailAndIsDeletedFalseAndIdNot(String email, Long id);
    Boolean existsByIdentityCardNoAndIsDeletedFalseAndIdNot(String identityCardNo, Long id);
}
