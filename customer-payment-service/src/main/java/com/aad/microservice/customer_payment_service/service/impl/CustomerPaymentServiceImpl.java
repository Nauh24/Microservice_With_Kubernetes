package com.aad.microservice.customer_payment_service.service.impl;

import com.aad.microservice.customer_payment_service.client.CustomerClient;
import com.aad.microservice.customer_payment_service.client.CustomerContractClient;
import com.aad.microservice.customer_payment_service.client.JobCategoryClient;
import com.aad.microservice.customer_payment_service.constant.PaymentStatusConstants;
import com.aad.microservice.customer_payment_service.exception.AppException;
import com.aad.microservice.customer_payment_service.exception.ErrorCode;
import com.aad.microservice.customer_payment_service.model.*;
import com.aad.microservice.customer_payment_service.repository.CustomerPaymentRepository;
import com.aad.microservice.customer_payment_service.repository.PaymentDetailRepository;
import com.aad.microservice.customer_payment_service.service.CustomerPaymentService;
import com.aad.microservice.customer_payment_service.util.PaymentPeriodUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomerPaymentServiceImpl implements CustomerPaymentService {
    private final CustomerPaymentRepository customerPaymentRepository;
    private final PaymentDetailRepository paymentDetailRepository;
    private final CustomerClient customerClient;
    private final CustomerContractClient customerContractClient;
    private final JobCategoryClient jobCategoryClient;

    public CustomerPaymentServiceImpl(CustomerPaymentRepository customerPaymentRepository,
                                     PaymentDetailRepository paymentDetailRepository,
                                     CustomerClient customerClient,
                                     CustomerContractClient customerContractClient,
                                     JobCategoryClient jobCategoryClient) {
        this.customerPaymentRepository = customerPaymentRepository;
        this.paymentDetailRepository = paymentDetailRepository;
        this.customerClient = customerClient;
        this.customerContractClient = customerContractClient;
        this.jobCategoryClient = jobCategoryClient;
    }

    @Override
    public List<Customer> searchCustomers(String fullName, String phoneNumber) {
        try {
            return customerClient.searchCustomers(fullName, phoneNumber);
        } catch (Exception e) {
            throw new AppException(ErrorCode.Unknown_Exception, "Không thể kết nối đến customer-service: " + e.getMessage());
        }
    }

    @Override
    public Customer getCustomerById(Long id) {
        try {
            return customerClient.getCustomerById(id);
        } catch (Exception e) {
            throw new AppException(ErrorCode.CustomerNotFound_Exception, "Không tìm thấy thông tin khách hàng");
        }
    }

    @Override
    public List<PaymentDetail> getDuePayments(Long customerId) {
        // Kiểm tra khách hàng có tồn tại không
        if (!checkCustomerExists(customerId)) {
            throw new AppException(ErrorCode.CustomerNotFound_Exception, "Không tìm thấy thông tin khách hàng");
        }

        // Lấy danh sách hợp đồng đang hoạt động của khách hàng
        List<CustomerContract> activeContracts;
        try {
            activeContracts = customerContractClient.getContractsByCustomerId(customerId).stream()
                    .filter(contract -> contract.getStatus() == 1) // ACTIVE status
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new AppException(ErrorCode.Unknown_Exception, "Không thể kết nối đến customer-contract-service: " + e.getMessage());
        }

        if (activeContracts.isEmpty()) {
            return new ArrayList<>();
        }

        // Tính toán kỳ thanh toán hiện tại
        PaymentPeriod currentPeriod = PaymentPeriodUtil.getCurrentPaymentPeriod();

        // Tạo danh sách các khoản thanh toán đến hạn
        List<PaymentDetail> duePayments = new ArrayList<>();
        for (CustomerContract contract : activeContracts) {
            // Kiểm tra xem hợp đồng có nằm trong kỳ thanh toán hiện tại không
            if (isContractInCurrentPeriod(contract, currentPeriod)) {
                // Kiểm tra xem đã có thanh toán cho hợp đồng này trong kỳ hiện tại chưa
                List<PaymentDetail> existingPayments = paymentDetailRepository.findByContractIdAndStatusAndIsDeletedFalse(
                        contract.getId(), PaymentStatusConstants.PAID);
                
                boolean alreadyPaid = existingPayments.stream()
                        .anyMatch(payment -> payment.getPaymentPeriod().equals(currentPeriod.toString()));
                
                if (!alreadyPaid) {
                    // Tính toán số tiền cần thanh toán cho kỳ hiện tại
                    double paymentAmount = calculatePaymentAmount(contract);
                    
                    // Lấy thông tin loại công việc
                    JobCategory jobCategory;
                    try {
                        jobCategory = jobCategoryClient.getJobCategoryById(contract.getJobCategoryId());
                    } catch (Exception e) {
                        // Nếu không lấy được thông tin loại công việc, vẫn tạo payment detail nhưng không có tên công việc
                        jobCategory = new JobCategory();
                        jobCategory.setName("Không xác định");
                    }
                    
                    // Tạo payment detail
                    PaymentDetail paymentDetail = PaymentDetail.builder()
                            .contractId(contract.getId())
                            .contractCode(contract.getContractCode())
                            .jobName(jobCategory.getName())
                            .paymentPeriod(currentPeriod.toString())
                            .amount(paymentAmount)
                            .status(PaymentStatusConstants.UNPAID)
                            .isDeleted(false)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    
                    duePayments.add(paymentDetail);
                }
            }
        }
        
        return duePayments;
    }

    @Override
    @Transactional
    public CustomerPayment processPayment(Long customerId, List<PaymentDetail> paymentDetails) {
        // Kiểm tra khách hàng có tồn tại không
        if (!checkCustomerExists(customerId)) {
            throw new AppException(ErrorCode.CustomerNotFound_Exception, "Không tìm thấy thông tin khách hàng");
        }
        
        // Kiểm tra danh sách thanh toán không được rỗng
        if (paymentDetails == null || paymentDetails.isEmpty()) {
            throw new AppException(ErrorCode.InvalidPayment_Exception, "Danh sách thanh toán không được rỗng");
        }
        
        // Tính tổng số tiền thanh toán
        double totalAmount = paymentDetails.stream()
                .mapToDouble(PaymentDetail::getAmount)
                .sum();
        
        // Tạo thanh toán mới
        CustomerPayment payment = CustomerPayment.builder()
                .customerId(customerId)
                .totalAmount(totalAmount)
                .paymentDate(LocalDateTime.now())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        // Lưu thanh toán
        CustomerPayment savedPayment = customerPaymentRepository.save(payment);
        
        // Tạo mã thanh toán
        savedPayment.setPaymentCode("PAY" + savedPayment.getId());
        savedPayment = customerPaymentRepository.save(savedPayment);
        
        // Cập nhật trạng thái các khoản thanh toán
        for (PaymentDetail detail : paymentDetails) {
            detail.setStatus(PaymentStatusConstants.PAID);
            detail.setCustomerPayment(savedPayment);
            detail.setIsDeleted(false);
            detail.setCreatedAt(LocalDateTime.now());
            detail.setUpdatedAt(LocalDateTime.now());
        }
        
        // Lưu chi tiết thanh toán
        paymentDetailRepository.saveAll(paymentDetails);
        
        return savedPayment;
    }

    @Override
    public CustomerPayment getPaymentById(Long id) {
        Optional<CustomerPayment> payment = customerPaymentRepository.findByIdAndIsDeletedFalse(id);
        if (payment.isEmpty()) {
            throw new AppException(ErrorCode.PaymentNotFound_Exception, "Không tìm thấy thông tin thanh toán");
        }
        return payment.get();
    }

    @Override
    public List<CustomerPayment> getAllPayments() {
        return customerPaymentRepository.findByIsDeletedFalse();
    }

    @Override
    public List<CustomerPayment> getPaymentsByCustomerId(Long customerId) {
        return customerPaymentRepository.findByCustomerIdAndIsDeletedFalse(customerId);
    }

    @Override
    public List<CustomerPayment> getPaymentsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return customerPaymentRepository.findByPaymentDateBetweenAndIsDeletedFalse(startDate, endDate);
    }

    @Override
    public boolean checkCustomerExists(Long id) {
        try {
            return customerClient.checkCustomerExists(id);
        } catch (Exception e) {
            return false;
        }
    }
    
    // Helper methods
    
    private boolean isContractInCurrentPeriod(CustomerContract contract, PaymentPeriod currentPeriod) {
        // Kiểm tra xem hợp đồng có hiệu lực trong kỳ thanh toán hiện tại không
        LocalDate contractStart = contract.getStartingDate();
        LocalDate contractEnd = contract.getEndingDate();
        LocalDate periodStart = currentPeriod.getStartDate();
        LocalDate periodEnd = currentPeriod.getEndDate();
        
        // Hợp đồng có hiệu lực nếu:
        // 1. Ngày bắt đầu hợp đồng <= ngày kết thúc kỳ thanh toán VÀ
        // 2. Ngày kết thúc hợp đồng >= ngày bắt đầu kỳ thanh toán
        return !contractStart.isAfter(periodEnd) && !contractEnd.isBefore(periodStart);
    }
    
    private double calculatePaymentAmount(CustomerContract contract) {
        // Tính toán số tiền cần thanh toán cho kỳ hiện tại
        // Đơn giản hóa: chia đều tổng giá trị hợp đồng cho số tuần giữa ngày bắt đầu và kết thúc
        LocalDate startDate = contract.getStartingDate();
        LocalDate endDate = contract.getEndingDate();
        
        // Tính số tuần giữa ngày bắt đầu và kết thúc
        long totalDays = endDate.toEpochDay() - startDate.toEpochDay() + 1;
        int totalWeeks = (int) Math.ceil(totalDays / 7.0);
        
        // Tránh chia cho 0
        if (totalWeeks <= 0) {
            totalWeeks = 1;
        }
        
        // Chia đều tổng giá trị hợp đồng cho số tuần
        return contract.getTotalAmount() / totalWeeks;
    }
}
