package com.aad.microservice.job_service.repository;

import com.aad.microservice.job_service.model.JobCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobCategoryRepository extends JpaRepository<JobCategory, Long> {
    List<JobCategory> findByIsDeletedFalse();
    Optional<JobCategory> findByIdAndIsDeletedFalse(Long id);
    
    Boolean existsByNameAndIsDeletedFalse(String name);
    Boolean existsByNameAndIsDeletedFalseAndIdNot(String name, Long id);
}
