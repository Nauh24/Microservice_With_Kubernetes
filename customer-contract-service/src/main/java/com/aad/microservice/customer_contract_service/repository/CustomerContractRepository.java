package com.aad.microservice.customer_contract_service.repository;

import com.aad.microservice.customer_contract_service.model.CustomerContract;
import com.aad.microservice.customer_contract_service.model.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CustomerContractRepository extends JpaRepository<CustomerContract, Long> {
    List<CustomerContract> findByIsDeletedFalse();
    Optional<CustomerContract> findByIdAndIsDeletedFalse(Long id);
    
    List<CustomerContract> findByCustomerIdAndIsDeletedFalse(Long customerId);
    List<CustomerContract> findByStatusAndIsDeletedFalse(ContractStatus status);
    List<CustomerContract> findByStartDateBetweenAndIsDeletedFalse(LocalDate startDate, LocalDate endDate);
    
    Boolean existsByContractCodeAndIsDeletedFalse(String contractCode);
    Boolean existsByContractCodeAndIsDeletedFalseAndIdNot(String contractCode, Long id);
}
