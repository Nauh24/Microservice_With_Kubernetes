package com.aad.microservice.customer_contract_service.controller;


import com.aad.microservice.customer_contract_service.model.CustomerContract;
import com.aad.microservice.customer_contract_service.service.CustomerContractService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/customer-contract")
public class CustomerContractController {
    private final CustomerContractService contractService;

    public CustomerContractController(CustomerContractService contractService) {
        this.contractService = contractService;
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Customer Contract Service is working!");
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerContract> getContractById(@PathVariable Long id) {
        CustomerContract contract = contractService.getContractById(id);
        return ResponseEntity.ok(contract);
    }

    @PostMapping
    public ResponseEntity<CustomerContract> createContract(@RequestBody CustomerContract contract) {
        return ResponseEntity.ok(contractService.createContract(contract));
    }

    @GetMapping
    public ResponseEntity<List<CustomerContract>> getAllContracts() {
        return ResponseEntity.ok(contractService.getAllContracts());
    }

    @PutMapping
    public ResponseEntity<CustomerContract> updateContract(@RequestBody CustomerContract contract) {
        return ResponseEntity.ok(contractService.updateContract(contract));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContract(@PathVariable Long id) {
        contractService.deleteContract(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<CustomerContract>> getContractsByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(contractService.getContractsByCustomerId(customerId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<CustomerContract>> getContractsByStatus(@PathVariable Integer status) {
        return ResponseEntity.ok(contractService.getContractsByStatus(status));
    }

    @GetMapping("/job-category/{jobCategoryId}")
    public ResponseEntity<List<CustomerContract>> getContractsByJobCategoryId(@PathVariable Long jobCategoryId) {
        return ResponseEntity.ok(contractService.getContractsByJobCategoryId(jobCategoryId));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<CustomerContract>> getContractsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(contractService.getContractsByDateRange(startDate, endDate));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<CustomerContract> updateContractStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        return ResponseEntity.ok(contractService.updateContractStatus(id, status));
    }



    @GetMapping("/{id}/check-contract-exists")
    public ResponseEntity<Boolean> checkContractExists(@PathVariable Long id) {
        boolean exists = contractService.checkContractExists(id);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/calculate-working-dates")
    public ResponseEntity<List<String>> calculateWorkingDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam String workingDays) {
        return ResponseEntity.ok(contractService.calculateWorkingDatesForShift(startDate, endDate, workingDays));
    }
}
