package com.aad.microservice.customer_payment_service.service;

import com.aad.microservice.customer_payment_service.model.Customer;
import com.aad.microservice.customer_payment_service.model.CustomerContract;
import com.aad.microservice.customer_payment_service.model.CustomerPayment;

import java.util.List;

public interface CustomerPaymentService {
    CustomerPayment createPayment(CustomerPayment payment);
    CustomerPayment getPaymentById(Long id);
    List<CustomerPayment> getAllPayments();
    List<CustomerPayment> getPaymentsByCustomerId(Long customerId);
    List<CustomerPayment> getPaymentsByContractId(Long contractId);

    List<Customer> searchCustomers(String fullname, String phoneNumber);
    List<CustomerContract> getActiveContractsByCustomerId(Long customerId);
    CustomerContract getContractPaymentInfo(Long contractId);
    Double getTotalPaidAmountByContractId(Long contractId);
    Double getRemainingAmountByContractId(Long contractId);
}
