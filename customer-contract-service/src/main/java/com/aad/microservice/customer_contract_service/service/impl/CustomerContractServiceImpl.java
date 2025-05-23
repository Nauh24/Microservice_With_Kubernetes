package com.aad.microservice.customer_contract_service.service.impl;

import com.aad.microservice.customer_contract_service.client.CustomerClient;
import com.aad.microservice.customer_contract_service.client.JobCategoryClient;
import com.aad.microservice.customer_contract_service.constant.ContractStatusConstants;
import com.aad.microservice.customer_contract_service.exception.AppException;
import com.aad.microservice.customer_contract_service.exception.ErrorCode;
import com.aad.microservice.customer_contract_service.model.CustomerContract;
import com.aad.microservice.customer_contract_service.model.JobDetail;
import com.aad.microservice.customer_contract_service.model.WorkShift;
import com.aad.microservice.customer_contract_service.repository.CustomerContractRepository;
import com.aad.microservice.customer_contract_service.service.CustomerContractService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomerContractServiceImpl implements CustomerContractService {
    private final CustomerContractRepository contractRepository;
    private final CustomerClient customerClient;
    private final JobCategoryClient jobCategoryClient;

    public CustomerContractServiceImpl(CustomerContractRepository contractRepository,
                                      CustomerClient customerClient,
                                      JobCategoryClient jobCategoryClient) {
        this.contractRepository = contractRepository;
        this.customerClient = customerClient;
        this.jobCategoryClient = jobCategoryClient;
    }

    @Override
    public CustomerContract createContract(CustomerContract contract) {

        // Kiểm tra khách hàng có tồn tại không
        try {
            Boolean customerExists = customerClient.checkCustomerExists(contract.getCustomerId());
            if (!customerExists) {
                throw new AppException(ErrorCode.CustomerNotFound_Exception, "Không tìm thấy thông tin khách hàng");
            }
        } catch (Exception e) {
            System.out.println("Không thể kết nối đến customer-service: " + e.getMessage());
        }

        // Kiểm tra ngày bắt đầu và kết thúc
        if (contract.getStartingDate() == null || contract.getEndingDate() == null) {
            throw new AppException(ErrorCode.NotAllowCreate_Exception, "Ngày bắt đầu và kết thúc không được để trống");
        }

        if (contract.getStartingDate().isAfter(contract.getEndingDate())) {
            throw new AppException(ErrorCode.InvalidDate_Exception, "Ngày bắt đầu phải trước ngày kết thúc");
        }

        if (contract.getStartingDate().isBefore(LocalDate.now())) {
            throw new AppException(ErrorCode.InvalidDate_Exception, "Ngày bắt đầu phải từ ngày hiện tại trở đi");
        }

        // Thiết lập các giá trị mặc định
        contract.setCreatedAt(LocalDateTime.now());
        contract.setUpdatedAt(LocalDateTime.now());
        contract.setIsDeleted(false);

        // Nếu không có totalPaid, thiết lập mặc định là 0
        if (contract.getTotalPaid() == null) {
            contract.setTotalPaid(0.0);
        }

        // Nếu không có status, thiết lập mặc định là PENDING
        if (contract.getStatus() == null) {
            contract.setStatus(ContractStatusConstants.PENDING);
        }

        // Xử lý JobDetails nếu có
        if (contract.getJobDetails() != null && !contract.getJobDetails().isEmpty()) {
            for (JobDetail jobDetail : contract.getJobDetails()) {
                // Kiểm tra loại công việc có tồn tại không
                if (jobDetail.getJobCategoryId() != null) {
                    try {
                        Boolean jobCategoryExists = jobCategoryClient.checkJobCategoryExists(jobDetail.getJobCategoryId());
                        if (!jobCategoryExists) {
                            throw new AppException(ErrorCode.JobCategoryNotFound_Exception, "Không tìm thấy thông tin loại công việc");
                        }
                    } catch (Exception e) {
                        System.out.println("Không thể kết nối đến job-service: " + e.getMessage());
                    }
                }

                // Kiểm tra xem có ít nhất một ca làm việc không
                if (jobDetail.getWorkShifts() == null || jobDetail.getWorkShifts().isEmpty()) {
                    throw new AppException(ErrorCode.InvalidInput_Exception, "Mỗi loại công việc phải có ít nhất một ca làm việc");
                }

                // Thiết lập các giá trị mặc định cho JobDetail
                jobDetail.setCreatedAt(LocalDateTime.now());
                jobDetail.setUpdatedAt(LocalDateTime.now());
                jobDetail.setIsDeleted(false);
                jobDetail.setContract(contract);

                // Xử lý WorkShifts nếu có
                for (WorkShift workShift : jobDetail.getWorkShifts()) {
                    // Thiết lập các giá trị mặc định cho WorkShift
                    workShift.setCreatedAt(LocalDateTime.now());
                    workShift.setUpdatedAt(LocalDateTime.now());
                    workShift.setIsDeleted(false);
                    workShift.setJobDetail(jobDetail);
                }
            }
        }

        // Lưu hợp đồng
        return contractRepository.save(contract);
    }

    @Override
    public CustomerContract updateContract(CustomerContract contract) {
        // Kiểm tra hợp đồng có tồn tại không
        Optional<CustomerContract> existingContract = contractRepository.findByIdAndIsDeletedFalse(contract.getId());
        if (existingContract.isEmpty()) {
            throw new AppException(ErrorCode.NotFound_Exception, "Không tìm thấy thông tin hợp đồng");
        }

        CustomerContract currentContract = existingContract.get();

        // Không cho phép cập nhật hợp đồng đã hoàn thành hoặc đã hủy
        if (currentContract.getStatus() == ContractStatusConstants.COMPLETED ||
            currentContract.getStatus() == ContractStatusConstants.CANCELLED) {
            throw new AppException(ErrorCode.NotAllowUpdate_Exception,
                "Không thể cập nhật hợp đồng đã " +
                (currentContract.getStatus() == ContractStatusConstants.COMPLETED ? "hoàn thành" : "hủy"));
        }

        // Kiểm tra ngày bắt đầu và kết thúc
        if (contract.getStartingDate() != null && contract.getEndingDate() != null) {
            if (contract.getStartingDate().isAfter(contract.getEndingDate())) {
                throw new AppException(ErrorCode.InvalidDate_Exception, "Ngày bắt đầu phải trước ngày kết thúc");
            }

            // Nếu hợp đồng đã ACTIVE, không cho phép thay đổi ngày bắt đầu
            if (currentContract.getStatus() == ContractStatusConstants.ACTIVE &&
                !contract.getStartingDate().isEqual(currentContract.getStartingDate())) {
                throw new AppException(ErrorCode.NotAllowUpdate_Exception,
                    "Không thể thay đổi ngày bắt đầu của hợp đồng đang hoạt động");
            }
        }

        // Cập nhật thông tin
        if (contract.getDescription() != null) {
            currentContract.setDescription(contract.getDescription());
        }

        if (contract.getStartingDate() != null) {
            currentContract.setStartingDate(contract.getStartingDate());
        }

        if (contract.getEndingDate() != null) {
            currentContract.setEndingDate(contract.getEndingDate());
        }

        if (contract.getTotalAmount() != null) {
            currentContract.setTotalAmount(contract.getTotalAmount());
        }

        if (contract.getAddress() != null) {
            currentContract.setAddress(contract.getAddress());
        }

        if (contract.getStatus() != null) {
            currentContract.setStatus(contract.getStatus());
        }

        // Xử lý JobDetails nếu có
        if (contract.getJobDetails() != null && !contract.getJobDetails().isEmpty()) {
            // Xóa các JobDetail cũ
            currentContract.getJobDetails().clear();

            // Thêm các JobDetail mới
            for (JobDetail jobDetail : contract.getJobDetails()) {
                // Kiểm tra loại công việc có tồn tại không
                if (jobDetail.getJobCategoryId() != null) {
                    try {
                        Boolean jobCategoryExists = jobCategoryClient.checkJobCategoryExists(jobDetail.getJobCategoryId());
                        if (!jobCategoryExists) {
                            throw new AppException(ErrorCode.JobCategoryNotFound_Exception, "Không tìm thấy thông tin loại công việc");
                        }
                    } catch (Exception e) {
                        // Nếu không thể kết nối đến job-service, vẫn cho phép cập nhật hợp đồng
                        // nhưng ghi log lỗi
                        System.out.println("Không thể kết nối đến job-service: " + e.getMessage());
                    }
                }

                // Kiểm tra xem có ít nhất một ca làm việc không
                if (jobDetail.getWorkShifts() == null || jobDetail.getWorkShifts().isEmpty()) {
                    throw new AppException(ErrorCode.InvalidInput_Exception, "Mỗi loại công việc phải có ít nhất một ca làm việc");
                }

                // Thiết lập các giá trị mặc định cho JobDetail
                if (jobDetail.getCreatedAt() == null) {
                    jobDetail.setCreatedAt(LocalDateTime.now());
                }
                jobDetail.setUpdatedAt(LocalDateTime.now());
                jobDetail.setIsDeleted(false);
                jobDetail.setContract(currentContract);

                // Xử lý WorkShifts
                for (WorkShift workShift : jobDetail.getWorkShifts()) {
                    // Thiết lập các giá trị mặc định cho WorkShift
                    if (workShift.getCreatedAt() == null) {
                        workShift.setCreatedAt(LocalDateTime.now());
                    }
                    workShift.setUpdatedAt(LocalDateTime.now());
                    workShift.setIsDeleted(false);
                    workShift.setJobDetail(jobDetail);
                }

                currentContract.addJobDetail(jobDetail);
            }
        }

        currentContract.setUpdatedAt(LocalDateTime.now());

        return contractRepository.save(currentContract);
    }

    @Override
    public void deleteContract(Long id) {
        Optional<CustomerContract> contract = contractRepository.findByIdAndIsDeletedFalse(id);
        if (contract.isEmpty()) {
            throw new AppException(ErrorCode.NotFound_Exception, "Không tìm thấy thông tin hợp đồng");
        }

        CustomerContract currentContract = contract.get();

        // Không cho phép xóa hợp đồng đang hoạt động hoặc đã hoàn thành
        if (currentContract.getStatus() == ContractStatusConstants.ACTIVE ||
            currentContract.getStatus() == ContractStatusConstants.COMPLETED) {
            throw new AppException(ErrorCode.NotAllowDelete_Exception,
                "Không thể xóa hợp đồng " +
                (currentContract.getStatus() == ContractStatusConstants.ACTIVE ? "đang hoạt động" : "đã hoàn thành"));
        }

        currentContract.setIsDeleted(true);
        currentContract.setUpdatedAt(LocalDateTime.now());
        contractRepository.save(currentContract);
    }

    @Override
    public CustomerContract getContractById(Long id) {
        Optional<CustomerContract> contract = contractRepository.findByIdAndIsDeletedFalse(id);
        if (contract.isEmpty()) {
            throw new AppException(ErrorCode.NotFound_Exception, "Không tìm thấy thông tin hợp đồng");
        }
        return contract.get();
    }

    @Override
    public List<CustomerContract> getAllContracts() {
        return contractRepository.findByIsDeletedFalse();
    }

    @Override
    public List<CustomerContract> getContractsByCustomerId(Long customerId) {
        return contractRepository.findByCustomerIdAndIsDeletedFalse(customerId);
    }

    @Override
    public List<CustomerContract> getContractsByStatus(Integer status) {
        return contractRepository.findByStatusAndIsDeletedFalse(status);
    }

    @Override
    public List<CustomerContract> getContractsByDateRange(LocalDate startDate, LocalDate endDate) {
        return contractRepository.findByStartingDateBetweenAndIsDeletedFalse(startDate, endDate);
    }

    @Override
    public List<CustomerContract> getContractsByJobCategoryId(Long jobCategoryId) {
        // Find all contracts
        List<CustomerContract> allContracts = contractRepository.findByIsDeletedFalse();

        // Filter contracts that have job details with the specified job category ID
        return allContracts.stream()
            .filter(contract -> contract.getJobDetails().stream()
                .anyMatch(jobDetail -> jobDetail.getJobCategoryId().equals(jobCategoryId) && !jobDetail.getIsDeleted()))
            .collect(Collectors.toList());
    }

    @Override
    public CustomerContract updateContractStatus(Long id, Integer status) {
        Optional<CustomerContract> contract = contractRepository.findByIdAndIsDeletedFalse(id);
        if (contract.isEmpty()) {
            throw new AppException(ErrorCode.NotFound_Exception, "Không tìm thấy thông tin hợp đồng");
        }

        CustomerContract currentContract = contract.get();

        // Kiểm tra logic chuyển trạng thái
        switch (status) {
            case ContractStatusConstants.ACTIVE:
                if (currentContract.getStatus() != ContractStatusConstants.PENDING) {
                    throw new AppException(ErrorCode.NotAllowUpdate_Exception,
                        "Chỉ có thể kích hoạt hợp đồng đang ở trạng thái chờ xử lý");
                }

                break;
            case ContractStatusConstants.COMPLETED:
                if (currentContract.getStatus() != ContractStatusConstants.ACTIVE) {
                    throw new AppException(ErrorCode.NotAllowUpdate_Exception,
                        "Chỉ có thể hoàn thành hợp đồng đang hoạt động");
                }
                break;
            case ContractStatusConstants.CANCELLED:
                if (currentContract.getStatus() != ContractStatusConstants.ACTIVE &&
                    currentContract.getStatus() != ContractStatusConstants.PENDING) {
                    throw new AppException(ErrorCode.NotAllowUpdate_Exception,
                        "Chỉ có thể hủy hợp đồng đang hoạt động hoặc đang chờ xử lý");
                }
                break;
            default:
                break;
        }

        currentContract.setStatus(status);
        currentContract.setUpdatedAt(LocalDateTime.now());

        return contractRepository.save(currentContract);
    }

    @Override
    public boolean checkContractExists(Long id) {
        return contractRepository.findByIdAndIsDeletedFalse(id).isPresent();
    }
}
