package com.aad.microservice.job_service.service.impl;

import com.aad.microservice.job_service.exception.AppException;
import com.aad.microservice.job_service.exception.ErrorCode;
import com.aad.microservice.job_service.model.JobCategory;
import com.aad.microservice.job_service.repository.JobCategoryRepository;
import com.aad.microservice.job_service.service.JobCategoryService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class JobCategoryServiceImpl implements JobCategoryService {
    private final JobCategoryRepository jobCategoryRepository;

    public JobCategoryServiceImpl(JobCategoryRepository jobCategoryRepository) {
        this.jobCategoryRepository = jobCategoryRepository;
    }

    @Override
    public JobCategory createJobCategory(JobCategory jobCategory) {
        jobCategory.setCreatedAt(LocalDateTime.now());
        jobCategory.setUpdatedAt(LocalDateTime.now());
        jobCategory.setIsDeleted(false);
        
        boolean existedName = jobCategoryRepository.existsByNameAndIsDeletedFalse(jobCategory.getName());
        if (existedName) {
            throw new AppException(ErrorCode.Duplicated_Exception, "Tên loại công việc đã tồn tại");
        }
        
        if (jobCategory.getName() == null || jobCategory.getName().trim().isEmpty()) {
            throw new AppException(ErrorCode.NotAllowCreate_Exception, "Tên loại công việc không được để trống");
        }
        
        if (jobCategory.getBaseSalary() == null || jobCategory.getBaseSalary() <= 0) {
            throw new AppException(ErrorCode.NotAllowCreate_Exception, "Mức lương cơ bản phải lớn hơn 0");
        }

        return jobCategoryRepository.save(jobCategory);
    }

    @Override
    public JobCategory updateJobCategory(JobCategory jobCategory) {
        Optional<JobCategory> existedJobCategory = jobCategoryRepository.findById(jobCategory.getId());

        if (existedJobCategory.isEmpty() || existedJobCategory.get().getIsDeleted()) {
            throw new AppException(ErrorCode.NotFound_Exception, "Không tìm thấy thông tin loại công việc");
        }

        boolean existedName = jobCategoryRepository.existsByNameAndIsDeletedFalseAndIdNot(jobCategory.getName(), jobCategory.getId());
        if (existedName) {
            throw new AppException(ErrorCode.Duplicated_Exception, "Tên loại công việc đã tồn tại");
        }
        
        if (jobCategory.getName() == null || jobCategory.getName().trim().isEmpty()) {
            throw new AppException(ErrorCode.NotAllowUpdate_Exception, "Tên loại công việc không được để trống");
        }
        
        if (jobCategory.getBaseSalary() == null || jobCategory.getBaseSalary() <= 0) {
            throw new AppException(ErrorCode.NotAllowUpdate_Exception, "Mức lương cơ bản phải lớn hơn 0");
        }

        JobCategory existingJobCategory = existedJobCategory.get();
        existingJobCategory.setName(jobCategory.getName());
        existingJobCategory.setDescription(jobCategory.getDescription());
        existingJobCategory.setBaseSalary(jobCategory.getBaseSalary());
        existingJobCategory.setUpdatedAt(LocalDateTime.now());
        
        return jobCategoryRepository.save(existingJobCategory);
    }

    @Override
    public void deleteJobCategory(Long id) {
        Optional<JobCategory> jobCategory = jobCategoryRepository.findById(id);
        if (jobCategory.isEmpty() || jobCategory.get().getIsDeleted()) {
            throw new AppException(ErrorCode.NotFound_Exception, "Không tìm thấy thông tin loại công việc");
        }

        jobCategory.get().setIsDeleted(true);
        jobCategory.get().setUpdatedAt(LocalDateTime.now());
        jobCategoryRepository.save(jobCategory.get());
    }

    @Override
    public JobCategory getJobCategoryById(Long id) {
        Optional<JobCategory> jobCategory = jobCategoryRepository.findByIdAndIsDeletedFalse(id);
        if (jobCategory.isEmpty()) {
            throw new AppException(ErrorCode.NotFound_Exception, "Không tìm thấy thông tin loại công việc");
        }
        return jobCategory.get();
    }

    @Override
    public List<JobCategory> getAllJobCategories() {
        return jobCategoryRepository.findByIsDeletedFalse();
    }

    @Override
    public boolean checkJobCategoryExists(Long id) {
        return jobCategoryRepository.findByIdAndIsDeletedFalse(id).isPresent();
    }
}
