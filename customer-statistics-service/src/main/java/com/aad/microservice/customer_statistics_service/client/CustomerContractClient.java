package com.aad.microservice.customer_statistics_service.client;

import com.aad.microservice.customer_statistics_service.model.CustomerContract;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@FeignClient(name = "customer-contract-service", url = "${app.customer-contract-service.url}")
public interface CustomerContractClient {

    @GetMapping("/api/customer-contract/date-range")
    List<CustomerContract> getContractsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate);

    @GetMapping("/api/customer-contract/{id}")
    CustomerContract getContractById(@PathVariable Long id);

    @GetMapping("/api/customer-contract/customer/{customerId}")
    List<CustomerContract> getContractsByCustomerId(@PathVariable Long customerId);
}
