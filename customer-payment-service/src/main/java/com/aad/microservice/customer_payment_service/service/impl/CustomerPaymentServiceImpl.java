package com.aad.microservice.customer_payment_service.service.impl;

import com.aad.microservice.customer_payment_service.client.CustomerClient;
import com.aad.microservice.customer_payment_service.client.CustomerContractClient;
import com.aad.microservice.customer_payment_service.constant.ContractStatusConstants;
import com.aad.microservice.customer_payment_service.model.ContractPaymentInfo;
import com.aad.microservice.customer_payment_service.exception.AppException;
import com.aad.microservice.customer_payment_service.exception.ErrorCode;
import com.aad.microservice.customer_payment_service.model.Customer;
import com.aad.microservice.customer_payment_service.model.CustomerContract;
import com.aad.microservice.customer_payment_service.model.CustomerPayment;
import com.aad.microservice.customer_payment_service.repository.CustomerPaymentRepository;
import com.aad.microservice.customer_payment_service.service.CustomerPaymentService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomerPaymentServiceImpl implements CustomerPaymentService {
    private final CustomerPaymentRepository paymentRepository;
    private final CustomerClient customerClient;
    private final CustomerContractClient contractClient;

    public CustomerPaymentServiceImpl(CustomerPaymentRepository paymentRepository,
                                     CustomerClient customerClient,
                                     CustomerContractClient contractClient) {
        this.paymentRepository = paymentRepository;
        this.customerClient = customerClient;
        this.contractClient = contractClient;
    }

    @Override
    public CustomerPayment createPayment(CustomerPayment payment) {
        // Kiểm tra hợp đồng có tồn tại không
        try {
            Boolean contractExists = contractClient.checkContractExists(payment.getContractId());
            if (!contractExists) {
                throw new AppException(ErrorCode.ContractNotFound_Exception, "Không tìm thấy thông tin hợp đồng");
            }

            // Kiểm tra hợp đồng có đang hoạt động hoặc chờ xử lý không
            CustomerContract contract = contractClient.getContractById(payment.getContractId());
            System.out.println("Creating payment for contract ID: " + payment.getContractId() +
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

            // Kiểm tra số tiền thanh toán
            if (payment.getAmount() == null || payment.getAmount() <= 0) {
                throw new AppException(ErrorCode.InvalidAmount_Exception, "Số tiền thanh toán phải lớn hơn 0");
            }

            // Kiểm tra số tiền thanh toán không vượt quá số tiền còn lại của hợp đồng
            Double remainingAmount = getRemainingAmountByContractId(payment.getContractId());
            if (payment.getAmount() > remainingAmount) {
                throw new AppException(ErrorCode.InvalidAmount_Exception,
                        "Số tiền thanh toán không được vượt quá số tiền còn lại của hợp đồng");
            }

        } catch (Exception e) {
            if (e instanceof AppException) {
                throw e;
            }
            // Nếu không thể kết nối đến các service khác, ghi log lỗi
            System.out.println("Không thể kết nối đến service: " + e.getMessage());
            // Trong môi trường production, nên sử dụng logger thay vì System.out.println
        }

        // Thiết lập các giá trị mặc định
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        payment.setIsDeleted(false);
        payment.setPaymentDate(LocalDate.now());

        // Lưu thanh toán
        CustomerPayment savedPayment = paymentRepository.save(payment);

        // Tạo mã thanh toán
        if (savedPayment.getPaymentCode() == null || savedPayment.getPaymentCode().isEmpty()) {
            savedPayment.setPaymentCode("PAY" + savedPayment.getId());
            savedPayment = paymentRepository.save(savedPayment);
        }

        return savedPayment;
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
        return paymentRepository.findByContractIdAndIsDeletedFalse(contractId);
    }

    @Override
    public List<Customer> searchCustomers(String fullName, String phoneNumber) {
        try {
            return customerClient.searchCustomers(fullName, phoneNumber);
        } catch (Exception e) {
            System.out.println("Không thể kết nối đến customer-service: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<ContractPaymentInfo> getActiveContractsByCustomerId(Long customerId) {
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
                        System.out.println("Contract " + contract.getContractCode() + " status: " + contract.getStatus() +
                                          " - Is ACTIVE: " + (contract.getStatus() == ContractStatusConstants.ACTIVE) +
                                          " - Is PENDING: " + (contract.getStatus() == ContractStatusConstants.PENDING));
                        boolean include = contract.getStatus() == ContractStatusConstants.ACTIVE ||
                                         contract.getStatus() == ContractStatusConstants.PENDING;
                        System.out.println("Including contract " + contract.getContractCode() + ": " + include);
                        return include;
                    })
                    .collect(Collectors.toList());

            System.out.println("Found " + activeContracts.size() + " active contracts");

            // Chuyển đổi sang model
            List<ContractPaymentInfo> result = activeContracts.stream()
                    .map(contract -> {
                        Double totalPaid = getTotalPaidAmountByContractId(contract.getId());
                        Double totalDue = contract.getTotalAmount() - totalPaid;

                        System.out.println("Contract " + contract.getContractCode() +
                                " - Total: " + contract.getTotalAmount() +
                                ", Paid: " + totalPaid +
                                ", Due: " + totalDue);

                        return ContractPaymentInfo.builder()
                                .contractId(contract.getId())
                                .contractCode(contract.getContractCode())
                                .startingDate(contract.getStartingDate())
                                .endingDate(contract.getEndingDate())
                                .totalAmount(contract.getTotalAmount())
                                .totalPaid(totalPaid)
                                .totalDue(totalDue)
                                .customerName(customer.getFullName())
                                .customerId(customer.getId())
                                .status(contract.getStatus())
                                .build();
                    })
                    .collect(Collectors.toList());

            System.out.println("Returning " + result.size() + " contract payment info objects");
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

    @Override
    public ContractPaymentInfo getContractPaymentInfo(Long contractId) {
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
            Double totalDue = contract.getTotalAmount() - totalPaid;

            return ContractPaymentInfo.builder()
                    .contractId(contract.getId())
                    .contractCode(contract.getContractCode())
                    .startingDate(contract.getStartingDate())
                    .endingDate(contract.getEndingDate())
                    .totalAmount(contract.getTotalAmount())
                    .totalPaid(totalPaid)
                    .totalDue(totalDue)
                    .customerName(customer.getFullName())
                    .customerId(customer.getId())
                    .status(contract.getStatus())
                    .build();

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
        Double totalPaid = paymentRepository.getTotalPaidAmountByContractId(contractId);
        return totalPaid != null ? totalPaid : 0.0;
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
}
