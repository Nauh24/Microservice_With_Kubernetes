package com.aad.microservice.customer_payment_service.repository;

import com.aad.microservice.customer_payment_service.model.ContractPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractPaymentRepository extends JpaRepository<ContractPayment, Long> {

    // Tìm tất cả thanh toán theo contract ID
    List<ContractPayment> findByContractId(Long contractId);

    // Tìm tất cả thanh toán theo payment ID
    @Query("SELECT cp FROM ContractPayment cp WHERE cp.payment.id = :paymentId")
    List<ContractPayment> findByPaymentId(@Param("paymentId") Long paymentId);

    // Tính tổng số tiền đã thanh toán cho một hợp đồng
    @Query("SELECT SUM(cp.allocatedAmount) FROM ContractPayment cp WHERE cp.contractId = :contractId")
    Double getTotalPaidAmountByContractId(@Param("contractId") Long contractId);

    // Tìm tất cả thanh toán theo customer ID (thông qua payment)
    @Query("SELECT cp FROM ContractPayment cp JOIN cp.payment p WHERE p.customerId = :customerId")
    List<ContractPayment> findByCustomerId(@Param("customerId") Long customerId);

    // Kiểm tra xem một hợp đồng đã có thanh toán nào chưa
    boolean existsByContractId(Long contractId);

    // Xóa tất cả thanh toán của một payment
    @Modifying
    @Query("DELETE FROM ContractPayment cp WHERE cp.payment.id = :paymentId")
    void deleteByPaymentId(@Param("paymentId") Long paymentId);
}
