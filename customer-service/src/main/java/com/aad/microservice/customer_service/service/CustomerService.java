package com.aad.microservice.customer_service.service;

import com.aad.microservice.customer_service.model.Customer;

import java.util.List;

public interface CustomerService {
    Customer createCustomer(Customer customer);
    Customer updateCustomer(Customer customer);
    void deleteCustomer(Long id);
    Customer getCustomerById(Long id);
    List<Customer> getAllCustomers();
    
    boolean checkCustomerExists(Long id);
}
