package com.aad.microservice.customer_contract_service.repository;

import com.aad.microservice.customer_contract_service.model.CustomerContract;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for CustomerContract entity
 */
public interface CustomerContractRepository extends JpaRepository<CustomerContract, Long> {

    List<CustomerContract> findByIsDeletedFalse();

    Optional<CustomerContract> findByIdAndIsDeletedFalse(Long id);

    List<CustomerContract> findByCustomerIdAndIsDeletedFalse(Long customerId);

    List<CustomerContract> findByStatusAndIsDeletedFalse(Integer status);

    List<CustomerContract> findByStartingDateBetweenAndIsDeletedFalse(LocalDate startDate, LocalDate endDate);

    List<CustomerContract> findByCustomerIdAndStartingDateAndEndingDateAndIsDeletedFalse(Long customerId, LocalDate startingDate, LocalDate endingDate);

}
