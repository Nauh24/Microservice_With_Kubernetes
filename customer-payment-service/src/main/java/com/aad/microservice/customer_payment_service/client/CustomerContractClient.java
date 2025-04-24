package com.aad.microservice.customer_payment_service.client;

import com.aad.microservice.customer_payment_service.model.CustomerContract;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "customer-contract-service", url = "${customer-contract.service.url:http://localhost:8087/api/customer-contract}")
public interface CustomerContractClient {

    @GetMapping("/{id}")
    CustomerContract getContractById(@PathVariable Long id);

    @GetMapping
    List<CustomerContract> getAllContracts();

    @GetMapping("/customer/{customerId}")
    List<CustomerContract> getContractsByCustomerId(@PathVariable Long customerId);

    @GetMapping("/status/{status}")
    List<CustomerContract> getContractsByStatus(@PathVariable Integer status);

    @PutMapping("/{id}/status")
    CustomerContract updateContractStatus(@PathVariable Long id, @RequestParam Integer status);

    @GetMapping("/{id}/check-contract-exists")
    Boolean checkContractExists(@PathVariable Long id);
}
