package com.aad.microservice.customer_payment_service.controller;

import com.aad.microservice.customer_payment_service.model.Customer;
import com.aad.microservice.customer_payment_service.model.CustomerPayment;
import com.aad.microservice.customer_payment_service.model.PaymentDetail;
import com.aad.microservice.customer_payment_service.service.CustomerPaymentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer-payment")
public class CustomerPaymentController {
    private final CustomerPaymentService paymentService;

    public CustomerPaymentController(CustomerPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/search-customers")
    public ResponseEntity<List<Customer>> searchCustomers(
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String phoneNumber) {
        return ResponseEntity.ok(paymentService.searchCustomers(fullName, phoneNumber));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long customerId) {
        return ResponseEntity.ok(paymentService.getCustomerById(customerId));
    }

    @GetMapping("/due-payments/{customerId}")
    public ResponseEntity<Map<String, Object>> getDuePayments(@PathVariable Long customerId) {
        List<PaymentDetail> duePayments = paymentService.getDuePayments(customerId);
        
        // Tính tổng số tiền cần thanh toán
        double totalAmount = duePayments.stream()
                .mapToDouble(PaymentDetail::getAmount)
                .sum();
        
        return ResponseEntity.ok(Map.of(
                "totalAmount", totalAmount,
                "paymentDetails", duePayments
        ));
    }

    @PostMapping("/process/{customerId}")
    public ResponseEntity<CustomerPayment> processPayment(
            @PathVariable Long customerId,
            @RequestBody List<PaymentDetail> paymentDetails) {
        return ResponseEntity.ok(paymentService.processPayment(customerId, paymentDetails));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerPayment> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @GetMapping
    public ResponseEntity<List<CustomerPayment>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/customer-payments/{customerId}")
    public ResponseEntity<List<CustomerPayment>> getPaymentsByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(paymentService.getPaymentsByCustomerId(customerId));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<CustomerPayment>> getPaymentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(paymentService.getPaymentsByDateRange(startDate, endDate));
    }
}
