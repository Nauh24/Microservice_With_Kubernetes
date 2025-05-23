package com.aad.microservice.customer_payment_service.repository;

import com.aad.microservice.customer_payment_service.model.CustomerPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerPaymentRepository extends JpaRepository<CustomerPayment, Long> {
    List<CustomerPayment> findByIsDeletedFalse();
    Optional<CustomerPayment> findByIdAndIsDeletedFalse(Long id);

    List<CustomerPayment> findByCustomerIdAndIsDeletedFalse(Long customerId);
    List<CustomerPayment> findByCustomerContractIdAndIsDeletedFalse(Long customerContractId);

    @Query("SELECT SUM(p.paymentAmount) FROM CustomerPayment p WHERE p.customerContractId = :contractId AND p.isDeleted = false")
    Double getTotalPaidAmountByContractId(@Param("contractId") Long contractId);


}
