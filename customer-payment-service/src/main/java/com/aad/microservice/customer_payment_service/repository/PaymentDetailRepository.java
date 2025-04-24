package com.aad.microservice.customer_payment_service.repository;

import com.aad.microservice.customer_payment_service.model.PaymentDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentDetailRepository extends JpaRepository<PaymentDetail, Long> {
    List<PaymentDetail> findByIsDeletedFalse();
    Optional<PaymentDetail> findByIdAndIsDeletedFalse(Long id);
    
    List<PaymentDetail> findByContractIdAndIsDeletedFalse(Long contractId);
    List<PaymentDetail> findByContractIdAndStatusAndIsDeletedFalse(Long contractId, Integer status);
    
    List<PaymentDetail> findByCustomerPaymentIdAndIsDeletedFalse(Long customerPaymentId);
}
