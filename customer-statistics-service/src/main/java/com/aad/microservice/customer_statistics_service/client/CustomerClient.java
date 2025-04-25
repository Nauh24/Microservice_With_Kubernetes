package com.aad.microservice.customer_statistics_service.client;

import com.aad.microservice.customer_statistics_service.model.Customer;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "customer-service", url = "${app.customer-service.url}")
public interface CustomerClient {

    @GetMapping("/api/customer")
    List<Customer> getAllCustomers();

    @GetMapping("/api/customer/{id}")
    Customer getCustomerById(@PathVariable Long id);
}
