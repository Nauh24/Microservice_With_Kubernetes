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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

        // Tạm thời bỏ validation ngày bắt đầu để test
        // if (contract.getStartingDate().isBefore(LocalDate.now())) {
        //     throw new AppException(ErrorCode.InvalidDate_Exception, "Ngày bắt đầu phải từ ngày hiện tại trở đi");
        // }

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

                // Validate JobDetail fields
                if (jobDetail.getStartDate() == null) {
                    throw new AppException(ErrorCode.InvalidInput_Exception, "Ngày bắt đầu công việc không được để trống");
                }

                if (jobDetail.getEndDate() == null) {
                    throw new AppException(ErrorCode.InvalidInput_Exception, "Ngày kết thúc công việc không được để trống");
                }

                if (jobDetail.getStartDate().isAfter(jobDetail.getEndDate())) {
                    throw new AppException(ErrorCode.InvalidDate_Exception, "Ngày bắt đầu công việc phải trước ngày kết thúc");
                }

                if (jobDetail.getWorkLocation() == null || jobDetail.getWorkLocation().trim().isEmpty()) {
                    throw new AppException(ErrorCode.InvalidInput_Exception, "Địa điểm làm việc không được để trống");
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
                    // Validate work shift fields
                    if (workShift.getStartTime() == null || workShift.getStartTime().trim().isEmpty()) {
                        throw new AppException(ErrorCode.InvalidInput_Exception, "Giờ bắt đầu ca làm việc không được để trống");
                    }

                    if (workShift.getEndTime() == null || workShift.getEndTime().trim().isEmpty()) {
                        throw new AppException(ErrorCode.InvalidInput_Exception, "Giờ kết thúc ca làm việc không được để trống");
                    }

                    if (workShift.getNumberOfWorkers() == null || workShift.getNumberOfWorkers() <= 0) {
                        throw new AppException(ErrorCode.InvalidInput_Exception, "Số lượng nhân công phải lớn hơn 0");
                    }

                    if (workShift.getSalary() == null || workShift.getSalary() < 0) {
                        throw new AppException(ErrorCode.InvalidInput_Exception, "Mức lương phải lớn hơn hoặc bằng 0");
                    }

                    if (workShift.getWorkingDays() == null || workShift.getWorkingDays().trim().isEmpty()) {
                        throw new AppException(ErrorCode.InvalidInput_Exception, "Ngày làm việc không được để trống");
                    }

                    // Thiết lập các giá trị mặc định cho WorkShift
                    workShift.setCreatedAt(LocalDateTime.now());
                    workShift.setUpdatedAt(LocalDateTime.now());
                    workShift.setIsDeleted(false);
                    workShift.setJobDetail(jobDetail);
                }
            }
        }

        // Calculate and validate total amount
        double calculatedAmount = calculateTotalAmount(contract);
        if (Math.abs(contract.getTotalAmount() - calculatedAmount) > 0.01) {
            // Allow small floating point differences, but update to calculated amount
            contract.setTotalAmount(calculatedAmount);
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

                // Validate JobDetail fields
                if (jobDetail.getStartDate() == null) {
                    throw new AppException(ErrorCode.InvalidInput_Exception, "Ngày bắt đầu công việc không được để trống");
                }

                if (jobDetail.getEndDate() == null) {
                    throw new AppException(ErrorCode.InvalidInput_Exception, "Ngày kết thúc công việc không được để trống");
                }

                if (jobDetail.getStartDate().isAfter(jobDetail.getEndDate())) {
                    throw new AppException(ErrorCode.InvalidDate_Exception, "Ngày bắt đầu công việc phải trước ngày kết thúc");
                }

                if (jobDetail.getWorkLocation() == null || jobDetail.getWorkLocation().trim().isEmpty()) {
                    throw new AppException(ErrorCode.InvalidInput_Exception, "Địa điểm làm việc không được để trống");
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
                    // Validate work shift fields
                    if (workShift.getStartTime() == null || workShift.getStartTime().trim().isEmpty()) {
                        throw new AppException(ErrorCode.InvalidInput_Exception, "Giờ bắt đầu ca làm việc không được để trống");
                    }

                    if (workShift.getEndTime() == null || workShift.getEndTime().trim().isEmpty()) {
                        throw new AppException(ErrorCode.InvalidInput_Exception, "Giờ kết thúc ca làm việc không được để trống");
                    }

                    if (workShift.getNumberOfWorkers() == null || workShift.getNumberOfWorkers() <= 0) {
                        throw new AppException(ErrorCode.InvalidInput_Exception, "Số lượng nhân công phải lớn hơn 0");
                    }

                    if (workShift.getSalary() == null || workShift.getSalary() < 0) {
                        throw new AppException(ErrorCode.InvalidInput_Exception, "Mức lương phải lớn hơn hoặc bằng 0");
                    }

                    if (workShift.getWorkingDays() == null || workShift.getWorkingDays().trim().isEmpty()) {
                        throw new AppException(ErrorCode.InvalidInput_Exception, "Ngày làm việc không được để trống");
                    }

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

        // Calculate and validate total amount if job details were updated
        if (contract.getJobDetails() != null && !contract.getJobDetails().isEmpty()) {
            double calculatedAmount = calculateTotalAmount(currentContract);
            if (Math.abs(currentContract.getTotalAmount() - calculatedAmount) > 0.01) {
                // Allow small floating point differences, but update to calculated amount
                currentContract.setTotalAmount(calculatedAmount);
            }
        }

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

    @Override
    public List<String> calculateWorkingDatesForShift(LocalDate startDate, LocalDate endDate, String workingDays) {
        return calculateWorkingDates(startDate, endDate, workingDays);
    }

    /**
     * Calculate the total amount for a contract based on work shifts
     * @param contract The contract to calculate total amount for
     * @return The calculated total amount
     */
    private double calculateTotalAmount(CustomerContract contract) {
        if (contract.getJobDetails() == null || contract.getJobDetails().isEmpty()) {
            return 0.0;
        }

        double totalAmount = 0.0;

        for (JobDetail jobDetail : contract.getJobDetails()) {
            if (jobDetail.getWorkShifts() == null || jobDetail.getWorkShifts().isEmpty()) {
                continue;
            }

            for (WorkShift workShift : jobDetail.getWorkShifts()) {
                if (workShift.getSalary() == null || workShift.getNumberOfWorkers() == null ||
                    workShift.getWorkingDays() == null || workShift.getWorkingDays().trim().isEmpty()) {
                    continue;
                }

                // Use job detail dates if available, otherwise use contract dates
                LocalDate startDate = jobDetail.getStartDate() != null ? jobDetail.getStartDate() : contract.getStartingDate();
                LocalDate endDate = jobDetail.getEndDate() != null ? jobDetail.getEndDate() : contract.getEndingDate();

                if (startDate == null || endDate == null) {
                    continue;
                }

                // Calculate working days count
                int workingDaysCount = calculateWorkingDaysCount(startDate, endDate, workShift.getWorkingDays());

                // Calculate amount for this shift: salary × numberOfWorkers × workingDaysCount
                double shiftAmount = workShift.getSalary() * workShift.getNumberOfWorkers() * workingDaysCount;
                totalAmount += shiftAmount;
            }
        }

        return totalAmount;
    }

    /**
     * Calculate the number of working days between start and end dates based on selected working days
     * @param startDate Start date
     * @param endDate End date
     * @param workingDays Comma-separated string of day numbers (1-7, where 1=Monday, 7=Sunday)
     * @return Number of working days
     */
    private int calculateWorkingDaysCount(LocalDate startDate, LocalDate endDate, String workingDays) {
        if (startDate == null || endDate == null || workingDays == null || workingDays.trim().isEmpty()) {
            return 0;
        }

        // Parse working days
        String[] dayStrings = workingDays.split(",");
        Set<Integer> workingDaySet = new HashSet<>();
        for (String dayStr : dayStrings) {
            try {
                int day = Integer.parseInt(dayStr.trim());
                if (day >= 1 && day <= 7) {
                    workingDaySet.add(day);
                }
            } catch (NumberFormatException e) {
                // Skip invalid day numbers
            }
        }

        if (workingDaySet.isEmpty()) {
            return 0;
        }

        int count = 0;
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            // Convert Java DayOfWeek to our format (1=Monday, 7=Sunday)
            int dayOfWeek = currentDate.getDayOfWeek().getValue(); // 1=Monday, 7=Sunday

            if (workingDaySet.contains(dayOfWeek)) {
                count++;
            }

            currentDate = currentDate.plusDays(1);
        }

        return count;
    }

    /**
     * Calculate actual working dates between start and end dates based on selected working days
     * @param startDate Start date
     * @param endDate End date
     * @param workingDays Comma-separated string of day numbers (1-7, where 1=Monday, 7=Sunday)
     * @return List of working dates in DD/MM/YYYY format
     */
    private List<String> calculateWorkingDates(LocalDate startDate, LocalDate endDate, String workingDays) {
        List<String> workingDatesList = new ArrayList<>();

        if (startDate == null || endDate == null || workingDays == null || workingDays.trim().isEmpty()) {
            return workingDatesList;
        }

        // Parse working days
        String[] dayStrings = workingDays.split(",");
        Set<Integer> workingDaySet = new HashSet<>();
        for (String dayStr : dayStrings) {
            try {
                int day = Integer.parseInt(dayStr.trim());
                if (day >= 1 && day <= 7) {
                    workingDaySet.add(day);
                }
            } catch (NumberFormatException e) {
                // Skip invalid day numbers
            }
        }

        if (workingDaySet.isEmpty()) {
            return workingDatesList;
        }

        LocalDate currentDate = startDate;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        while (!currentDate.isAfter(endDate)) {
            // Convert Java DayOfWeek to our format (1=Monday, 7=Sunday)
            int dayOfWeek = currentDate.getDayOfWeek().getValue(); // 1=Monday, 7=Sunday

            if (workingDaySet.contains(dayOfWeek)) {
                workingDatesList.add(currentDate.format(formatter));
            }

            currentDate = currentDate.plusDays(1);
        }

        return workingDatesList;
    }
}
