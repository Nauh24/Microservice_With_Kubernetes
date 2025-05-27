package com.aad.microservice.customer_payment_service.client;

import com.aad.microservice.customer_payment_service.model.Customer;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "customer-service", url = "${customer.service.url}")
public interface CustomerClient {

    @GetMapping("/{id}")
    Customer getCustomerById(@PathVariable Long id);

    @GetMapping("/{id}/check-customer-exists")
    Boolean checkCustomerExists(@PathVariable Long id);

    @GetMapping
    List<Customer> getAllCustomers();

    @GetMapping("/search")
    List<Customer> searchCustomers(
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String phoneNumber);
}
