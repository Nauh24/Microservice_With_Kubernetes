package com.aad.microservice.customer_payment_service.service;

import com.aad.microservice.customer_payment_service.dto.CreatePaymentRequest;
import com.aad.microservice.customer_payment_service.model.Customer;
import com.aad.microservice.customer_payment_service.model.CustomerContract;
import com.aad.microservice.customer_payment_service.model.CustomerPayment;
import com.aad.microservice.customer_payment_service.model.ContractPayment;

import java.util.List;

public interface CustomerPaymentService {
    // Phương thức cũ - giữ lại để tương thích ngược
    CustomerPayment createPayment(CustomerPayment payment);

    // Phương thức mới - hỗ trợ many-to-many
    CustomerPayment createPaymentWithMultipleContracts(CreatePaymentRequest request);

    CustomerPayment getPaymentById(Long id);
    List<CustomerPayment> getAllPayments();
    List<CustomerPayment> getPaymentsByCustomerId(Long customerId);
    List<CustomerPayment> getPaymentsByContractId(Long contractId);

    // Phương thức quản lý thanh toán hợp đồng
    List<ContractPayment> getContractPaymentsByPaymentId(Long paymentId);
    List<ContractPayment> getContractPaymentsByContractId(Long contractId);

    List<Customer> searchCustomers(String fullName, String phoneNumber);
    List<CustomerContract> getActiveContractsByCustomerId(Long customerId);
    CustomerContract getContractPaymentInfo(Long contractId);
    Double getTotalPaidAmountByContractId(Long contractId);
    Double getRemainingAmountByContractId(Long contractId);
}
