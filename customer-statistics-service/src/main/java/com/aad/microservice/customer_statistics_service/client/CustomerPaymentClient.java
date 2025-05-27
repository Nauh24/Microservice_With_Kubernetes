package com.aad.microservice.customer_statistics_service.client;

import com.aad.microservice.customer_statistics_service.model.CustomerPayment;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@FeignClient(name = "customer-payment-service", url = "${app.customer-payment-service.url}")
public interface CustomerPaymentClient {

    @GetMapping
    List<CustomerPayment> getAllPayments();

    @GetMapping("/customer/{customerId}")
    List<CustomerPayment> getPaymentsByCustomerId(@PathVariable Long customerId);

    @GetMapping("/contract/{contractId}")
    List<CustomerPayment> getPaymentsByContractId(@PathVariable Long contractId);
}
