package com.aad.microservice.customer_contract_service.service;

import com.aad.microservice.customer_contract_service.client.CustomerClient;
import com.aad.microservice.customer_contract_service.exception.AppException;
import com.aad.microservice.customer_contract_service.exception.ErrorCode;
import com.aad.microservice.customer_contract_service.model.CustomerContract;
import com.aad.microservice.customer_contract_service.model.ContractStatus;
import com.aad.microservice.customer_contract_service.repository.CustomerContractRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerContractServiceImpl implements ICustomerContractService {
    private final CustomerContractRepository contractRepository;
    private final CustomerClient customerClient;

    public CustomerContractServiceImpl(CustomerContractRepository contractRepository, CustomerClient customerClient) {
        this.contractRepository = contractRepository;
        this.customerClient = customerClient;
    }

    @Override
    public CustomerContract createContract(CustomerContract contract) {
        // Kiểm tra khách hàng có tồn tại không
        Boolean customerExists = customerClient.checkCustomerExists(contract.getCustomerId());
        if (!customerExists) {
            throw new AppException(ErrorCode.CustomerNotFound_Exception, "Không tìm thấy thông tin khách hàng");
        }
        
        // Kiểm tra ngày bắt đầu và kết thúc
        if (contract.getStartDate() == null || contract.getEndDate() == null) {
            throw new AppException(ErrorCode.NotAllowCreate_Exception, "Ngày bắt đầu và kết thúc không được để trống");
        }
        
        if (contract.getStartDate().isAfter(contract.getEndDate())) {
            throw new AppException(ErrorCode.InvalidDate_Exception, "Ngày bắt đầu phải trước ngày kết thúc");
        }
        
        if (contract.getStartDate().isBefore(LocalDate.now())) {
            throw new AppException(ErrorCode.InvalidDate_Exception, "Ngày bắt đầu phải từ ngày hiện tại trở đi");
        }
        
        // Thiết lập các giá trị mặc định
        contract.setCreatedAt(LocalDateTime.now());
        contract.setUpdatedAt(LocalDateTime.now());
        contract.setIsDeleted(false);
        
        // Nếu không có status, thiết lập mặc định là DRAFT
        if (contract.getStatus() == null) {
            contract.setStatus(ContractStatus.DRAFT);
        }
        
        // Lưu hợp đồng
        CustomerContract savedContract = contractRepository.save(contract);
        
        // Tạo mã hợp đồng
        if (savedContract.getContractCode() == null || savedContract.getContractCode().isEmpty()) {
            savedContract.setContractCode("CC" + savedContract.getId());
            savedContract = contractRepository.save(savedContract);
        }
        
        return savedContract;
    }

    @Override
    public CustomerContract updateContract(CustomerContract contract) {
        // Kiểm tra hợp đồng có tồn tại không
        Optional<CustomerContract> existingContract = contractRepository.findByIdAndIsDeletedFalse(contract.getId());
        if (existingContract.isEmpty()) {
            throw new AppException(ErrorCode.NotFound_Exception, "Không tìm thấy thông tin hợp đồng");
        }
        
        CustomerContract currentContract = existingContract.get();
        
        // Không cho phép cập nhật hợp đồng đã hoàn thành hoặc chấm dứt
        if (currentContract.getStatus() == ContractStatus.COMPLETED || 
            currentContract.getStatus() == ContractStatus.TERMINATED ||
            currentContract.getStatus() == ContractStatus.EXPIRED) {
            throw new AppException(ErrorCode.NotAllowUpdate_Exception, 
                "Không thể cập nhật hợp đồng đã " + 
                (currentContract.getStatus() == ContractStatus.COMPLETED ? "hoàn thành" : 
                 currentContract.getStatus() == ContractStatus.TERMINATED ? "chấm dứt" : "hết hạn"));
        }
        
        // Kiểm tra ngày bắt đầu và kết thúc
        if (contract.getStartDate() != null && contract.getEndDate() != null) {
            if (contract.getStartDate().isAfter(contract.getEndDate())) {
                throw new AppException(ErrorCode.InvalidDate_Exception, "Ngày bắt đầu phải trước ngày kết thúc");
            }
            
            // Nếu hợp đồng đã ACTIVE, không cho phép thay đổi ngày bắt đầu
            if (currentContract.getStatus() == ContractStatus.ACTIVE && 
                !contract.getStartDate().isEqual(currentContract.getStartDate())) {
                throw new AppException(ErrorCode.NotAllowUpdate_Exception, 
                    "Không thể thay đổi ngày bắt đầu của hợp đồng đang hoạt động");
            }
        }
        
        // Cập nhật thông tin
        if (contract.getTitle() != null) {
            currentContract.setTitle(contract.getTitle());
        }
        
        if (contract.getDescription() != null) {
            currentContract.setDescription(contract.getDescription());
        }
        
        if (contract.getStartDate() != null) {
            currentContract.setStartDate(contract.getStartDate());
        }
        
        if (contract.getEndDate() != null) {
            currentContract.setEndDate(contract.getEndDate());
        }
        
        if (contract.getTotalValue() != null) {
            currentContract.setTotalValue(contract.getTotalValue());
        }
        
        if (contract.getLocation() != null) {
            currentContract.setLocation(contract.getLocation());
        }
        
        if (contract.getStatus() != null) {
            currentContract.setStatus(contract.getStatus());
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
        
        // Không cho phép xóa hợp đồng đang hoạt động
        if (currentContract.getStatus() == ContractStatus.ACTIVE) {
            throw new AppException(ErrorCode.NotAllowDelete_Exception, "Không thể xóa hợp đồng đang hoạt động");
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
    public List<CustomerContract> getContractsByStatus(ContractStatus status) {
        return contractRepository.findByStatusAndIsDeletedFalse(status);
    }

    @Override
    public List<CustomerContract> getContractsByDateRange(LocalDate startDate, LocalDate endDate) {
        return contractRepository.findByStartDateBetweenAndIsDeletedFalse(startDate, endDate);
    }

    @Override
    public CustomerContract updateContractStatus(Long id, ContractStatus status) {
        Optional<CustomerContract> contract = contractRepository.findByIdAndIsDeletedFalse(id);
        if (contract.isEmpty()) {
            throw new AppException(ErrorCode.NotFound_Exception, "Không tìm thấy thông tin hợp đồng");
        }
        
        CustomerContract currentContract = contract.get();
        
        // Kiểm tra logic chuyển trạng thái
        switch (status) {
            case ACTIVE:
                if (currentContract.getStatus() != ContractStatus.PENDING) {
                    throw new AppException(ErrorCode.NotAllowUpdate_Exception, 
                        "Chỉ có thể kích hoạt hợp đồng đang ở trạng thái chờ ký");
                }
                break;
            case COMPLETED:
                if (currentContract.getStatus() != ContractStatus.ACTIVE) {
                    throw new AppException(ErrorCode.NotAllowUpdate_Exception, 
                        "Chỉ có thể hoàn thành hợp đồng đang hoạt động");
                }
                break;
            case TERMINATED:
                if (currentContract.getStatus() != ContractStatus.ACTIVE) {
                    throw new AppException(ErrorCode.NotAllowUpdate_Exception, 
                        "Chỉ có thể chấm dứt hợp đồng đang hoạt động");
                }
                break;
            case PENDING:
                if (currentContract.getStatus() != ContractStatus.DRAFT) {
                    throw new AppException(ErrorCode.NotAllowUpdate_Exception, 
                        "Chỉ có thể chuyển sang trạng thái chờ ký từ bản nháp");
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
