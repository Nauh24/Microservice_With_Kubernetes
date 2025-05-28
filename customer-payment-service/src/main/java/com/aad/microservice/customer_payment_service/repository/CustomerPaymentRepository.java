package com.aad.microservice.customer_payment_service.repository;

import com.aad.microservice.customer_payment_service.model.CustomerPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CustomerPaymentRepository extends JpaRepository<CustomerPayment, Long> {
    List<CustomerPayment> findByIsDeletedFalse();
    Optional<CustomerPayment> findByIdAndIsDeletedFalse(Long id);

    List<CustomerPayment> findByCustomerIdAndIsDeletedFalse(Long customerId);
    List<CustomerPayment> findByCustomerContractIdAndIsDeletedFalse(Long customerContractId);

    @Query("SELECT SUM(p.paymentAmount) FROM CustomerPayment p WHERE p.customerContractId = :contractId AND p.isDeleted = false")
    Double getTotalPaidAmountByContractId(@Param("contractId") Long contractId);

    @Query("SELECT p FROM CustomerPayment p WHERE p.customerContractId = :contractId AND p.paymentAmount = :amount AND DATE(p.paymentDate) = :paymentDate AND p.paymentMethod = :method AND p.isDeleted = false")
    List<CustomerPayment> findByCustomerContractIdAndPaymentAmountAndPaymentDateAndPaymentMethodAndIsDeletedFalse(
        @Param("contractId") Long customerContractId,
        @Param("amount") Double paymentAmount,
        @Param("paymentDate") LocalDate paymentDate,
        @Param("method") Integer paymentMethod);

}
