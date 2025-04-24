package com.aad.microservice.customer_contract_service.client;

import com.aad.microservice.customer_contract_service.model.Customer;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "customer-service", url = "${customer.service.url:http://localhost:8085/api/customer}")
public interface CustomerClient {

    @GetMapping("/{id}")
    Customer getCustomerById(@PathVariable Long id);

    @GetMapping("/{id}/check-customer-exists")
    Boolean checkCustomerExists(@PathVariable Long id);
}
