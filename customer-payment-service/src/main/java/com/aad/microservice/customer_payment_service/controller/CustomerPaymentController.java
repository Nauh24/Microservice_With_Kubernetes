package com.aad.microservice.customer_payment_service.controller;

import com.aad.microservice.customer_payment_service.model.Customer;
import com.aad.microservice.customer_payment_service.model.CustomerContract;
import com.aad.microservice.customer_payment_service.model.CustomerPayment;
import com.aad.microservice.customer_payment_service.service.CustomerPaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer-payment")
public class CustomerPaymentController {
    private final CustomerPaymentService paymentService;

    public CustomerPaymentController(CustomerPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Customer Payment Service is working!");
    }

    @GetMapping("/payment/{id}")
    public ResponseEntity<CustomerPayment> getPaymentById(@PathVariable Long id) {
        CustomerPayment payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(payment);
    }

    @PostMapping
    public ResponseEntity<CustomerPayment> createPayment(@RequestBody CustomerPayment payment) {
        return ResponseEntity.ok(paymentService.createPayment(payment));
    }

    @GetMapping
    public ResponseEntity<List<CustomerPayment>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/customer/search")
    public ResponseEntity<List<Customer>> searchCustomers(
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String phoneNumber) {
        return ResponseEntity.ok(paymentService.searchCustomers(fullName, phoneNumber));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<CustomerPayment>> getPaymentsByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(paymentService.getPaymentsByCustomerId(customerId));
    }

    @GetMapping("/contract/{contractId}")
    public ResponseEntity<List<CustomerPayment>> getPaymentsByContractId(@PathVariable Long contractId) {
        return ResponseEntity.ok(paymentService.getPaymentsByContractId(contractId));
    }

    @GetMapping("/customer/{customerId}/active-contracts")
    public ResponseEntity<List<CustomerContract>> getActiveContractsByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(paymentService.getActiveContractsByCustomerId(customerId));
    }

    @GetMapping("/contract/{contractId}/payment-info")
    public ResponseEntity<CustomerContract> getContractPaymentInfo(@PathVariable Long contractId) {
        return ResponseEntity.ok(paymentService.getContractPaymentInfo(contractId));
    }

    @GetMapping("/contract/{contractId}/total-paid")
    public ResponseEntity<Double> getTotalPaidAmountByContractId(@PathVariable Long contractId) {
        return ResponseEntity.ok(paymentService.getTotalPaidAmountByContractId(contractId));
    }

    @GetMapping("/contract/{contractId}/remaining-amount")
    public ResponseEntity<Double> getRemainingAmountByContractId(@PathVariable Long contractId) {
        return ResponseEntity.ok(paymentService.getRemainingAmountByContractId(contractId));
    }
}
