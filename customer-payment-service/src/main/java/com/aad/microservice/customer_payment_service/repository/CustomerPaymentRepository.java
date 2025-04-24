package com.aad.microservice.customer_payment_service.repository;

import com.aad.microservice.customer_payment_service.model.CustomerPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CustomerPaymentRepository extends JpaRepository<CustomerPayment, Long> {
    List<CustomerPayment> findByIsDeletedFalse();
    Optional<CustomerPayment> findByIdAndIsDeletedFalse(Long id);
    
    List<CustomerPayment> findByCustomerIdAndIsDeletedFalse(Long customerId);
    List<CustomerPayment> findByPaymentDateBetweenAndIsDeletedFalse(LocalDateTime startDate, LocalDateTime endDate);
    
    Optional<CustomerPayment> findByPaymentCodeAndIsDeletedFalse(String paymentCode);
}
