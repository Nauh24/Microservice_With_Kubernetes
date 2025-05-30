package com.aad.microservice.customer_payment_service.controller;

import com.aad.microservice.customer_payment_service.dto.CreatePaymentRequest;
import com.aad.microservice.customer_payment_service.model.Customer;
import com.aad.microservice.customer_payment_service.model.CustomerContract;
import com.aad.microservice.customer_payment_service.model.CustomerPayment;
import com.aad.microservice.customer_payment_service.model.ContractPayment;
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

    // API mới - hỗ trợ thanh toán nhiều hợp đồng
    @PostMapping("/multiple-contracts")
    public ResponseEntity<CustomerPayment> createPaymentWithMultipleContracts(@RequestBody CreatePaymentRequest request) {
        System.out.println("🚀 Controller: Received multiple contracts payment request:");
        System.out.println("Customer ID: " + request.getCustomerId());
        System.out.println("Total Amount: " + request.getTotalAmount());
        System.out.println("Payment Method: " + request.getPaymentMethod());
        System.out.println("Contract Payments Count: " + (request.getContractPayments() != null ? request.getContractPayments().size() : 0));

        try {
            CustomerPayment result = paymentService.createPaymentWithMultipleContracts(request);
            System.out.println("✅ Controller: Payment created successfully with ID: " + result.getId());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("❌ Controller: Error creating payment: " + e.getMessage());
            throw e;
        }
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

    // API lấy danh sách thanh toán theo payment ID
    @GetMapping("/payment/{paymentId}/contract-payments")
    public ResponseEntity<List<ContractPayment>> getContractPaymentsByPaymentId(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.getContractPaymentsByPaymentId(paymentId));
    }

    // API lấy danh sách thanh toán theo contract ID
    @GetMapping("/contract/{contractId}/contract-payments")
    public ResponseEntity<List<ContractPayment>> getContractPaymentsByContractId(@PathVariable Long contractId) {
        return ResponseEntity.ok(paymentService.getContractPaymentsByContractId(contractId));
    }
}
