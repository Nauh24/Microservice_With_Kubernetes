package com.aad.microservice.customer_contract_service.service.impl;

import com.aad.microservice.customer_contract_service.client.JobCategoryClient;
import com.aad.microservice.customer_contract_service.exception.AppException;
import com.aad.microservice.customer_contract_service.exception.ErrorCode;
import com.aad.microservice.customer_contract_service.model.CustomerContract;
import com.aad.microservice.customer_contract_service.model.JobDetail;
import com.aad.microservice.customer_contract_service.model.WorkShift;
import com.aad.microservice.customer_contract_service.repository.CustomerContractRepository;
import com.aad.microservice.customer_contract_service.repository.JobDetailRepository;
import com.aad.microservice.customer_contract_service.service.JobDetailService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of JobDetailService
 */
@Service
public class JobDetailServiceImpl implements JobDetailService {

    private final JobDetailRepository jobDetailRepository;
    private final CustomerContractRepository contractRepository;
    private final JobCategoryClient jobCategoryClient;

    public JobDetailServiceImpl(JobDetailRepository jobDetailRepository,
                               CustomerContractRepository contractRepository,
                               JobCategoryClient jobCategoryClient) {
        this.jobDetailRepository = jobDetailRepository;
        this.contractRepository = contractRepository;
        this.jobCategoryClient = jobCategoryClient;
    }

    @Override
    public JobDetail createJobDetail(JobDetail jobDetail) {
        // Validate contract
        if (jobDetail.getContract() == null || jobDetail.getContract().getId() == null) {
            throw new AppException(ErrorCode.InvalidInput_Exception, "Hợp đồng không được để trống");
        }

        Optional<CustomerContract> contractOpt = contractRepository.findByIdAndIsDeletedFalse(jobDetail.getContract().getId());
        if (contractOpt.isEmpty()) {
            throw new AppException(ErrorCode.NotFound_Exception, "Không tìm thấy thông tin hợp đồng");
        }

        // Validate job category
        if (jobDetail.getJobCategoryId() == null) {
            throw new AppException(ErrorCode.InvalidInput_Exception, "Loại công việc không được để trống");
        }

        try {
            Boolean jobCategoryExists = jobCategoryClient.checkJobCategoryExists(jobDetail.getJobCategoryId());
            if (!jobCategoryExists) {
                throw new AppException(ErrorCode.JobCategoryNotFound_Exception, "Không tìm thấy thông tin loại công việc");
            }
        } catch (Exception e) {
            // If we can't connect to job-service, log the error but continue
            System.out.println("Không thể kết nối đến job-service: " + e.getMessage());
        }

        // Validate dates
        if (jobDetail.getStartDate() != null && jobDetail.getEndDate() != null) {
            if (jobDetail.getStartDate().isAfter(jobDetail.getEndDate())) {
                throw new AppException(ErrorCode.InvalidDate_Exception, "Ngày bắt đầu phải trước ngày kết thúc");
            }

            // Ensure job detail dates are within contract dates
            CustomerContract contract = contractOpt.get();
            if (contract.getStartingDate() != null && jobDetail.getStartDate().isBefore(contract.getStartingDate())) {
                throw new AppException(ErrorCode.InvalidDate_Exception,
                    "Ngày bắt đầu công việc không thể trước ngày bắt đầu hợp đồng");
            }

            if (contract.getEndingDate() != null && jobDetail.getEndDate().isAfter(contract.getEndingDate())) {
                throw new AppException(ErrorCode.InvalidDate_Exception,
                    "Ngày kết thúc công việc không thể sau ngày kết thúc hợp đồng");
            }
        }

        // Kiểm tra xem có ít nhất một ca làm việc không
        if (jobDetail.getWorkShifts() == null || jobDetail.getWorkShifts().isEmpty()) {
            throw new AppException(ErrorCode.InvalidInput_Exception, "Mỗi loại công việc phải có ít nhất một ca làm việc");
        }

        // Set default values
        jobDetail.setCreatedAt(LocalDateTime.now());
        jobDetail.setUpdatedAt(LocalDateTime.now());
        jobDetail.setIsDeleted(false);

        // Set contract reference
        jobDetail.setContract(contractOpt.get());

        // Thiết lập các giá trị mặc định cho WorkShift
        for (WorkShift workShift : jobDetail.getWorkShifts()) {
            workShift.setCreatedAt(LocalDateTime.now());
            workShift.setUpdatedAt(LocalDateTime.now());
            workShift.setIsDeleted(false);
            workShift.setJobDetail(jobDetail);
        }

        return jobDetailRepository.save(jobDetail);
    }

    @Override
    public JobDetail updateJobDetail(JobDetail jobDetail) {
        // Validate job detail exists
        Optional<JobDetail> existingJobDetail = jobDetailRepository.findByIdAndIsDeletedFalse(jobDetail.getId());
        if (existingJobDetail.isEmpty()) {
            throw new AppException(ErrorCode.NotFound_Exception, "Không tìm thấy thông tin chi tiết công việc");
        }

        JobDetail currentJobDetail = existingJobDetail.get();

        // Validate job category
        if (jobDetail.getJobCategoryId() != null) {
            try {
                Boolean jobCategoryExists = jobCategoryClient.checkJobCategoryExists(jobDetail.getJobCategoryId());
                if (!jobCategoryExists) {
                    throw new AppException(ErrorCode.JobCategoryNotFound_Exception, "Không tìm thấy thông tin loại công việc");
                }
                currentJobDetail.setJobCategoryId(jobDetail.getJobCategoryId());
            } catch (Exception e) {
                // If we can't connect to job-service, log the error but continue
                System.out.println("Không thể kết nối đến job-service: " + e.getMessage());
                currentJobDetail.setJobCategoryId(jobDetail.getJobCategoryId());
            }
        }

        // Validate dates
        if (jobDetail.getStartDate() != null && jobDetail.getEndDate() != null) {
            if (jobDetail.getStartDate().isAfter(jobDetail.getEndDate())) {
                throw new AppException(ErrorCode.InvalidDate_Exception, "Ngày bắt đầu phải trước ngày kết thúc");
            }

            // Ensure job detail dates are within contract dates
            CustomerContract contract = currentJobDetail.getContract();
            if (contract.getStartingDate() != null && jobDetail.getStartDate().isBefore(contract.getStartingDate())) {
                throw new AppException(ErrorCode.InvalidDate_Exception,
                    "Ngày bắt đầu công việc không thể trước ngày bắt đầu hợp đồng");
            }

            if (contract.getEndingDate() != null && jobDetail.getEndDate().isAfter(contract.getEndingDate())) {
                throw new AppException(ErrorCode.InvalidDate_Exception,
                    "Ngày kết thúc công việc không thể sau ngày kết thúc hợp đồng");
            }

            currentJobDetail.setStartDate(jobDetail.getStartDate());
            currentJobDetail.setEndDate(jobDetail.getEndDate());
        }

        // Update other fields
        if (jobDetail.getWorkLocation() != null) {
            currentJobDetail.setWorkLocation(jobDetail.getWorkLocation());
        }

        // Kiểm tra xem có ít nhất một ca làm việc không
        if (jobDetail.getWorkShifts() != null) {
            // Nếu có cập nhật danh sách ca làm việc, kiểm tra xem có ít nhất một ca làm việc không
            if (jobDetail.getWorkShifts().isEmpty()) {
                throw new AppException(ErrorCode.InvalidInput_Exception, "Mỗi loại công việc phải có ít nhất một ca làm việc");
            }

            // Cập nhật danh sách ca làm việc
            currentJobDetail.getWorkShifts().clear();
            for (WorkShift workShift : jobDetail.getWorkShifts()) {
                workShift.setJobDetail(currentJobDetail);
                if (workShift.getCreatedAt() == null) {
                    workShift.setCreatedAt(LocalDateTime.now());
                }
                workShift.setUpdatedAt(LocalDateTime.now());
                workShift.setIsDeleted(false);
                currentJobDetail.getWorkShifts().add(workShift);
            }
        } else {
            // Nếu không cập nhật danh sách ca làm việc, kiểm tra xem danh sách hiện tại có ít nhất một ca làm việc không
            if (currentJobDetail.getWorkShifts() == null || currentJobDetail.getWorkShifts().isEmpty()) {
                throw new AppException(ErrorCode.InvalidInput_Exception, "Mỗi loại công việc phải có ít nhất một ca làm việc");
            }
        }

        // Update audit fields
        currentJobDetail.setUpdatedAt(LocalDateTime.now());

        return jobDetailRepository.save(currentJobDetail);
    }

    @Override
    public void deleteJobDetail(Long id) {
        Optional<JobDetail> jobDetail = jobDetailRepository.findByIdAndIsDeletedFalse(id);
        if (jobDetail.isEmpty()) {
            throw new AppException(ErrorCode.NotFound_Exception, "Không tìm thấy thông tin chi tiết công việc");
        }

        JobDetail currentJobDetail = jobDetail.get();
        currentJobDetail.setIsDeleted(true);
        currentJobDetail.setUpdatedAt(LocalDateTime.now());

        jobDetailRepository.save(currentJobDetail);
    }

    @Override
    public JobDetail getJobDetailById(Long id) {
        Optional<JobDetail> jobDetail = jobDetailRepository.findByIdAndIsDeletedFalse(id);
        if (jobDetail.isEmpty()) {
            throw new AppException(ErrorCode.NotFound_Exception, "Không tìm thấy thông tin chi tiết công việc");
        }

        return jobDetail.get();
    }

    @Override
    public List<JobDetail> getAllJobDetails() {
        return jobDetailRepository.findByIsDeletedFalse();
    }

    @Override
    public List<JobDetail> getJobDetailsByContractId(Long contractId) {
        return jobDetailRepository.findByContract_IdAndIsDeletedFalse(contractId);
    }

    @Override
    public List<JobDetail> getJobDetailsByJobCategoryId(Long jobCategoryId) {
        return jobDetailRepository.findByJobCategoryIdAndIsDeletedFalse(jobCategoryId);
    }

    @Override
    public boolean checkJobDetailExists(Long id) {
        return jobDetailRepository.findByIdAndIsDeletedFalse(id).isPresent();
    }
}
