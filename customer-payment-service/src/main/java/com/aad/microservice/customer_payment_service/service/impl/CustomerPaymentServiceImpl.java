package com.aad.microservice.customer_payment_service.service.impl;

import com.aad.microservice.customer_payment_service.client.CustomerClient;
import com.aad.microservice.customer_payment_service.client.CustomerContractClient;
import com.aad.microservice.customer_payment_service.constant.ContractStatusConstants;
import com.aad.microservice.customer_payment_service.dto.CreatePaymentRequest;
import com.aad.microservice.customer_payment_service.dto.ContractPaymentDto;
import com.aad.microservice.customer_payment_service.exception.AppException;
import com.aad.microservice.customer_payment_service.exception.ErrorCode;
import com.aad.microservice.customer_payment_service.model.Customer;
import com.aad.microservice.customer_payment_service.model.CustomerContract;
import com.aad.microservice.customer_payment_service.model.CustomerPayment;
import com.aad.microservice.customer_payment_service.model.ContractPayment;
import com.aad.microservice.customer_payment_service.repository.CustomerPaymentRepository;
import com.aad.microservice.customer_payment_service.repository.ContractPaymentRepository;
import com.aad.microservice.customer_payment_service.service.CustomerPaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomerPaymentServiceImpl implements CustomerPaymentService {
    private final CustomerPaymentRepository paymentRepository;
    private final ContractPaymentRepository contractPaymentRepository;
    private final CustomerClient customerClient;
    private final CustomerContractClient contractClient;

    @PersistenceContext
    private EntityManager entityManager;

    public CustomerPaymentServiceImpl(CustomerPaymentRepository paymentRepository,
                                     ContractPaymentRepository contractPaymentRepository,
                                     CustomerClient customerClient,
                                     CustomerContractClient contractClient) {
        this.paymentRepository = paymentRepository;
        this.contractPaymentRepository = contractPaymentRepository;
        this.customerClient = customerClient;
        this.contractClient = contractClient;
    }

    @Override
    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.SERIALIZABLE)
    public CustomerPayment createPayment(CustomerPayment payment) {
        // Validate input parameters
        if (payment == null) {
            throw new AppException(ErrorCode.NotAllowCreate_Exception, "Thông tin thanh toán không được để trống");
        }

        if (payment.getPaymentAmount() == null || payment.getPaymentAmount() <= 0) {
            throw new AppException(ErrorCode.InvalidAmount_Exception, "Số tiền thanh toán phải lớn hơn 0");
        }

        if (payment.getCustomerContractId() == null) {
            throw new AppException(ErrorCode.ContractNotFound_Exception, "Mã hợp đồng không được để trống");
        }

        // Check for potential duplicate payments
        LocalDateTime paymentDate = payment.getPaymentDate() != null ? payment.getPaymentDate() : LocalDateTime.now();
        LocalDate paymentDateOnly = paymentDate.toLocalDate();

        List<CustomerPayment> existingPayments = paymentRepository.findByCustomerContractIdAndPaymentAmountAndPaymentDateAndPaymentMethodAndIsDeletedFalse(
            payment.getCustomerContractId(), payment.getPaymentAmount(), paymentDateOnly, payment.getPaymentMethod());

        if (!existingPayments.isEmpty()) {
            for (CustomerPayment existing : existingPayments) {
                if (Objects.equals(existing.getNote(), payment.getNote())) {
                    throw new AppException(ErrorCode.Duplicated_Exception,
                        "Thanh toán tương tự đã tồn tại cho hợp đồng này với cùng số tiền và phương thức");
                }
            }
        }

        // Kiểm tra hợp đồng có tồn tại không
        try {
            Boolean contractExists = contractClient.checkContractExists(payment.getCustomerContractId());
            if (!contractExists) {
                throw new AppException(ErrorCode.ContractNotFound_Exception, "Không tìm thấy thông tin hợp đồng");
            }

            // Kiểm tra hợp đồng có đang hoạt động hoặc chờ xử lý không
            CustomerContract contract = contractClient.getContractById(payment.getCustomerContractId());
            System.out.println("Creating payment for contract ID: " + payment.getCustomerContractId() +
                              ", Status: " + contract.getStatus() +
                              " (ACTIVE=" + ContractStatusConstants.ACTIVE +
                              ", PENDING=" + ContractStatusConstants.PENDING + ")");

            boolean isActive = contract.getStatus() == ContractStatusConstants.ACTIVE;
            boolean isPending = contract.getStatus() == ContractStatusConstants.PENDING;
            System.out.println("Contract is ACTIVE: " + isActive + ", Contract is PENDING: " + isPending);

            if (!isActive && !isPending) {
                throw new AppException(ErrorCode.ContractNotActive_Exception,
                        "Chỉ có thể thanh toán cho hợp đồng đang hoạt động hoặc chờ xử lý");
            }

            // Kiểm tra khách hàng có tồn tại không
            Boolean customerExists = customerClient.checkCustomerExists(payment.getCustomerId());
            if (!customerExists) {
                throw new AppException(ErrorCode.CustomerNotFound_Exception, "Không tìm thấy thông tin khách hàng");
            }

            // Kiểm tra số tiền thanh toán không vượt quá số tiền còn lại của hợp đồng
            Double remainingAmount = getRemainingAmountByContractId(payment.getCustomerContractId());
            if (payment.getPaymentAmount() > remainingAmount) {
                throw new AppException(ErrorCode.InvalidAmount_Exception,
                        "Số tiền thanh toán (" + payment.getPaymentAmount() + " VNĐ) không được vượt quá số tiền còn lại (" + remainingAmount + " VNĐ)");
            }

            // Additional validation: Check if payment amount is reasonable (not negative or zero)
            if (payment.getPaymentAmount() <= 0) {
                throw new AppException(ErrorCode.InvalidAmount_Exception, "Số tiền thanh toán phải lớn hơn 0");
            }

        } catch (Exception e) {
            if (e instanceof AppException) {
                throw e;
            }
            System.out.println("Không thể kết nối đến service: " + e.getMessage());
        }

        // Thiết lập các giá trị mặc định
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        payment.setIsDeleted(false);
        payment.setPaymentDate(LocalDateTime.now());

        // Add unique identifier to prevent duplicate processing
        String processingKey = "payment_" + payment.getCustomerContractId() + "_" + payment.getCustomerId() + "_" + System.currentTimeMillis();
        System.out.println("Processing payment creation with key: " + processingKey);

        // Lưu thanh toán với proper transaction handling
        System.out.println("Saving payment for contract ID: " + payment.getCustomerContractId() + ", Amount: " + payment.getPaymentAmount());

        try {
            // Save payment without clearing entity manager to maintain transaction context
            CustomerPayment savedPayment = paymentRepository.save(payment);

            // Force immediate flush to database to ensure data is persisted
            entityManager.flush();

            System.out.println("Payment successfully saved with ID: " + savedPayment.getId() + " (key: " + processingKey + ")");
            return savedPayment;

        } catch (Exception e) {
            System.err.println("Error saving payment with key " + processingKey + ": " + e.getMessage());
            throw new AppException(ErrorCode.NotAllowCreate_Exception, "Không thể tạo thanh toán: " + e.getMessage());
        }
    }

    @Override
    public CustomerPayment getPaymentById(Long id) {
        Optional<CustomerPayment> payment = paymentRepository.findByIdAndIsDeletedFalse(id);
        if (payment.isEmpty()) {
            throw new AppException(ErrorCode.NotFound_Exception, "Không tìm thấy thông tin thanh toán");
        }
        return payment.get();
    }

    @Override
    public List<CustomerPayment> getAllPayments() {
        return paymentRepository.findByIsDeletedFalse();
    }

    @Override
    public List<CustomerPayment> getPaymentsByCustomerId(Long customerId) {
        return paymentRepository.findByCustomerIdAndIsDeletedFalse(customerId);
    }

    @Override
    public List<CustomerPayment> getPaymentsByContractId(Long contractId) {
        return paymentRepository.findByCustomerContractIdAndIsDeletedFalse(contractId);
    }

    @Override
    public List<Customer> searchCustomers(String fullname, String phoneNumber) {
        try {
            // Đảm bảo tham số được truyền đúng tên (fullName với chữ N viết hoa)
            return customerClient.searchCustomers(fullname, phoneNumber);
        } catch (Exception e) {
            System.out.println("Không thể kết nối đến customer-service: " + e.getMessage());
            e.printStackTrace(); // In stack trace để dễ debug
            return new ArrayList<>();
        }
    }

    @Override
    public List<CustomerContract> getActiveContractsByCustomerId(Long customerId) {
        try {
            System.out.println("Fetching active contracts for customer ID: " + customerId);

            // Kiểm tra khách hàng có tồn tại không
            Boolean customerExists = customerClient.checkCustomerExists(customerId);
            System.out.println("Customer exists check result: " + customerExists);

            if (!customerExists) {
                throw new AppException(ErrorCode.CustomerNotFound_Exception, "Không tìm thấy thông tin khách hàng");
            }

            // Lấy thông tin khách hàng
            Customer customer = customerClient.getCustomerById(customerId);
            System.out.println("Retrieved customer: " + customer.getFullName());

            // Lấy danh sách hợp đồng của khách hàng
            List<CustomerContract> contracts = contractClient.getContractsByCustomerId(customerId);
            System.out.println("Retrieved " + contracts.size() + " contracts for customer");

            // Lọc các hợp đồng đang hoạt động hoặc chờ xử lý
            System.out.println("Filtering contracts for ACTIVE (status=1) or PENDING (status=0)");
            List<CustomerContract> activeContracts = contracts.stream()
                    .filter(contract -> {
                        System.out.println("Contract ID " + contract.getId() + " status: " + contract.getStatus() +
                                          " - Is ACTIVE: " + (contract.getStatus() == ContractStatusConstants.ACTIVE) +
                                          " - Is PENDING: " + (contract.getStatus() == ContractStatusConstants.PENDING));
                        boolean include = contract.getStatus() == ContractStatusConstants.ACTIVE ||
                                         contract.getStatus() == ContractStatusConstants.PENDING;
                        System.out.println("Including contract ID " + contract.getId() + ": " + include);
                        return include;
                    })
                    .collect(Collectors.toList());

            System.out.println("Found " + activeContracts.size() + " active contracts");

            // Cập nhật thông tin thanh toán cho mỗi hợp đồng
            List<CustomerContract> result = activeContracts.stream()
                    .map(contract -> {
                        Double totalPaid = getTotalPaidAmountByContractId(contract.getId());

                        System.out.println("Contract ID " + contract.getId() +
                                " - Total: " + contract.getTotalAmount() +
                                ", Paid: " + totalPaid);

                        // Cập nhật thông tin thanh toán
                        contract.setTotalPaid(totalPaid);
                        contract.setCustomerName(getCustomerDisplayName(customer));

                        return contract;
                    })
                    .collect(Collectors.toList());

            System.out.println("Returning " + result.size() + " contract objects");
            return result;

        } catch (Exception e) {
            if (e instanceof AppException) {
                System.out.println("AppException: " + e.getMessage());
                throw e;
            }
            System.out.println("Exception when fetching active contracts: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }


    private String getCustomerDisplayName(Customer customer) {
        if (customer == null) {
            return "Không xác định";
        }
        if (customer.getFullName() != null && !customer.getFullName().isEmpty()) {
            return customer.getFullName();
        }
        if (customer.getCompanyName() != null && !customer.getCompanyName().isEmpty()) {
            return customer.getCompanyName();
        }
        return "Không xác định";
    }

    @Override
    public CustomerContract getContractPaymentInfo(Long contractId) {
        try {
            // Kiểm tra hợp đồng có tồn tại không
            Boolean contractExists = contractClient.checkContractExists(contractId);
            if (!contractExists) {
                throw new AppException(ErrorCode.ContractNotFound_Exception, "Không tìm thấy thông tin hợp đồng");
            }

            // Lấy thông tin hợp đồng
            CustomerContract contract = contractClient.getContractById(contractId);

            // Kiểm tra hợp đồng có đang hoạt động hoặc chờ xử lý không
            System.out.println("Getting payment info for contract ID: " + contractId +
                              ", Status: " + contract.getStatus() +
                              " (ACTIVE=" + ContractStatusConstants.ACTIVE +
                              ", PENDING=" + ContractStatusConstants.PENDING + ")");

            boolean isActive = contract.getStatus() == ContractStatusConstants.ACTIVE;
            boolean isPending = contract.getStatus() == ContractStatusConstants.PENDING;
            System.out.println("Contract is ACTIVE: " + isActive + ", Contract is PENDING: " + isPending);

            if (!isActive && !isPending) {
                throw new AppException(ErrorCode.ContractNotActive_Exception,
                        "Chỉ có thể thanh toán cho hợp đồng đang hoạt động hoặc chờ xử lý");
            }

            // Lấy thông tin khách hàng
            Customer customer = customerClient.getCustomerById(contract.getCustomerId());

            // Tính toán số tiền đã thanh toán và còn lại
            Double totalPaid = getTotalPaidAmountByContractId(contractId);

            // Cập nhật thông tin thanh toán
            contract.setTotalPaid(totalPaid);
            contract.setCustomerName(getCustomerDisplayName(customer));

            return contract;

        } catch (Exception e) {
            if (e instanceof AppException) {
                throw e;
            }
            System.out.println("Không thể kết nối đến service: " + e.getMessage());
            throw new AppException(ErrorCode.Unknown_Exception, "Không thể lấy thông tin thanh toán hợp đồng");
        }
    }

    @Override
    public Double getTotalPaidAmountByContractId(Long contractId) {
        // Sử dụng cả hai cách để tương thích ngược
        // 1. Từ ContractPayment (many-to-many)
        Double totalFromAllocations = contractPaymentRepository.getTotalPaidAmountByContractId(contractId);

        // 2. Từ CustomerPayment cũ (one-to-many) - để tương thích ngược
        Double totalFromOldPayments = paymentRepository.getTotalPaidAmountByContractId(contractId);

        Double totalAllocations = totalFromAllocations != null ? totalFromAllocations : 0.0;
        Double totalOldPayments = totalFromOldPayments != null ? totalFromOldPayments : 0.0;

        return totalAllocations + totalOldPayments;
    }

    @Override
    public Double getRemainingAmountByContractId(Long contractId) {
        try {
            CustomerContract contract = contractClient.getContractById(contractId);
            Double totalPaid = getTotalPaidAmountByContractId(contractId);
            return contract.getTotalAmount() - totalPaid;
        } catch (Exception e) {
            System.out.println("Không thể kết nối đến customer-contract-service: " + e.getMessage());
            throw new AppException(ErrorCode.ContractNotFound_Exception, "Không thể lấy thông tin hợp đồng");
        }
    }

    // Phương thức mới - hỗ trợ many-to-many
    @Override
    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.SERIALIZABLE)
    public CustomerPayment createPaymentWithMultipleContracts(CreatePaymentRequest request) {
        // Validate input parameters
        if (request == null) {
            throw new AppException(ErrorCode.NotAllowCreate_Exception, "Thông tin thanh toán không được để trống");
        }

        if (request.getTotalAmount() == null || request.getTotalAmount() <= 0) {
            throw new AppException(ErrorCode.InvalidAmount_Exception, "Số tiền thanh toán phải lớn hơn 0");
        }

        if (request.getCustomerId() == null) {
            throw new AppException(ErrorCode.CustomerNotFound_Exception, "Mã khách hàng không được để trống");
        }

        if (request.getContractPayments() == null || request.getContractPayments().isEmpty()) {
            throw new AppException(ErrorCode.ContractNotFound_Exception, "Phải có ít nhất một hợp đồng để thanh toán");
        }

        // Kiểm tra khách hàng có tồn tại không
        try {
            Boolean customerExists = customerClient.checkCustomerExists(request.getCustomerId());
            if (!customerExists) {
                throw new AppException(ErrorCode.CustomerNotFound_Exception, "Không tìm thấy thông tin khách hàng");
            }

            // Validate tổng số tiền phân bổ
            Double totalAllocated = request.getContractPayments().stream()
                    .mapToDouble(ContractPaymentDto::getAllocatedAmount)
                    .sum();

            // Sử dụng epsilon để so sánh số thực
            double epsilon = 0.01; // Cho phép sai lệch 1 cent
            if (Math.abs(totalAllocated - request.getTotalAmount()) > epsilon) {
                throw new AppException(ErrorCode.InvalidAmount_Exception,
                    "Tổng số tiền phân bổ (" + totalAllocated + " VNĐ) phải bằng tổng số tiền thanh toán (" + request.getTotalAmount() + " VNĐ)");
            }

            // Validate từng hợp đồng
            for (ContractPaymentDto allocation : request.getContractPayments()) {
                if (allocation.getContractId() == null) {
                    throw new AppException(ErrorCode.ContractNotFound_Exception, "Mã hợp đồng không được để trống");
                }

                if (allocation.getAllocatedAmount() == null || allocation.getAllocatedAmount() <= 0) {
                    throw new AppException(ErrorCode.InvalidAmount_Exception, "Số tiền phân bổ cho hợp đồng phải lớn hơn 0");
                }

                // Kiểm tra hợp đồng có tồn tại không
                Boolean contractExists = contractClient.checkContractExists(allocation.getContractId());
                if (!contractExists) {
                    throw new AppException(ErrorCode.ContractNotFound_Exception, "Không tìm thấy hợp đồng ID: " + allocation.getContractId());
                }

                // Kiểm tra hợp đồng có đang hoạt động hoặc chờ xử lý không
                CustomerContract contract = contractClient.getContractById(allocation.getContractId());
                boolean isActive = contract.getStatus() == ContractStatusConstants.ACTIVE;
                boolean isPending = contract.getStatus() == ContractStatusConstants.PENDING;

                if (!isActive && !isPending) {
                    throw new AppException(ErrorCode.ContractNotActive_Exception,
                            "Chỉ có thể thanh toán cho hợp đồng đang hoạt động hoặc chờ xử lý. Hợp đồng ID: " + allocation.getContractId());
                }

                // Kiểm tra số tiền thanh toán không vượt quá số tiền còn lại của hợp đồng
                Double remainingAmount = getRemainingAmountByContractId(allocation.getContractId());
                if (allocation.getAllocatedAmount() > remainingAmount) {
                    throw new AppException(ErrorCode.InvalidAmount_Exception,
                            "Số tiền thanh toán cho hợp đồng ID " + allocation.getContractId() +
                            " (" + allocation.getAllocatedAmount() + " VNĐ) không được vượt quá số tiền còn lại (" + remainingAmount + " VNĐ)");
                }
            }

        } catch (Exception e) {
            if (e instanceof AppException) {
                throw e;
            }
            System.out.println("Không thể kết nối đến service: " + e.getMessage());
            throw new AppException(ErrorCode.Unknown_Exception, "Không thể xác thực thông tin thanh toán");
        }

        // Tạo payment chính
        CustomerPayment payment = CustomerPayment.builder()
                .paymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : LocalDateTime.now())
                .paymentMethod(request.getPaymentMethod())
                .paymentAmount(request.getTotalAmount())
                .note(request.getNote())
                .customerId(request.getCustomerId())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        try {
            // Lưu payment trước
            CustomerPayment savedPayment = paymentRepository.save(payment);
            entityManager.flush();

            // Tạo các thanh toán hợp đồng
            List<ContractPayment> contractPayments = new ArrayList<>();
            for (ContractPaymentDto contractPaymentDto : request.getContractPayments()) {
                ContractPayment contractPayment = ContractPayment.builder()
                        .payment(savedPayment)
                        .contractId(contractPaymentDto.getContractId())
                        .allocatedAmount(contractPaymentDto.getAllocatedAmount())
                        .build();

                ContractPayment savedContractPayment = contractPaymentRepository.save(contractPayment);
                contractPayments.add(savedContractPayment);
            }

            entityManager.flush();

            // Set contract payments vào saved payment để trả về đầy đủ thông tin
            savedPayment.setContractPayments(contractPayments);

            System.out.println("Payment with multiple contracts successfully saved with ID: " + savedPayment.getId());
            return savedPayment;

        } catch (Exception e) {
            System.err.println("Error saving payment with multiple contracts: " + e.getMessage());
            e.printStackTrace(); // In stack trace để debug
            throw new AppException(ErrorCode.NotAllowCreate_Exception, "Không thể tạo thanh toán: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractPayment> getContractPaymentsByPaymentId(Long paymentId) {
        return contractPaymentRepository.findByPaymentId(paymentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractPayment> getContractPaymentsByContractId(Long contractId) {
        return contractPaymentRepository.findByContractId(contractId);
    }
}
