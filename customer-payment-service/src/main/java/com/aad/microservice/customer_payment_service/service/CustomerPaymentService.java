package com.aad.microservice.customer_payment_service.service;

import com.aad.microservice.customer_payment_service.model.Customer;
import com.aad.microservice.customer_payment_service.model.CustomerPayment;
import com.aad.microservice.customer_payment_service.model.PaymentDetail;

import java.time.LocalDateTime;
import java.util.List;

public interface CustomerPaymentService {
    // Customer search methods
    List<Customer> searchCustomers(String fullName, String phoneNumber);
    Customer getCustomerById(Long id);
    
    // Payment methods
    List<PaymentDetail> getDuePayments(Long customerId);
    CustomerPayment processPayment(Long customerId, List<PaymentDetail> paymentDetails);
    
    // Payment management methods
    CustomerPayment getPaymentById(Long id);
    List<CustomerPayment> getAllPayments();
    List<CustomerPayment> getPaymentsByCustomerId(Long customerId);
    List<CustomerPayment> getPaymentsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    // Helper methods
    boolean checkCustomerExists(Long id);
}
