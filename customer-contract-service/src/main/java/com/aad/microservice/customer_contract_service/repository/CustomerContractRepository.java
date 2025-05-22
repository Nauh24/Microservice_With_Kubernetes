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
    /**
     * Find all contracts that are not deleted
     * @return List of contracts
     */
    List<CustomerContract> findByIsDeletedFalse();

    /**
     * Find a contract by ID that is not deleted
     * @param id Contract ID
     * @return Optional containing the contract if found
     */
    Optional<CustomerContract> findByIdAndIsDeletedFalse(Long id);

    /**
     * Find all contracts for a customer that are not deleted
     * @param customerId Customer ID
     * @return List of contracts
     */
    List<CustomerContract> findByCustomerIdAndIsDeletedFalse(Long customerId);

    /**
     * Find all contracts with a specific status that are not deleted
     * @param status Contract status
     * @return List of contracts
     */
    List<CustomerContract> findByStatusAndIsDeletedFalse(Integer status);

    /**
     * Find all contracts with a starting date between the specified dates that are not deleted
     * @param startDate Start date
     * @param endDate End date
     * @return List of contracts
     */
    List<CustomerContract> findByStartingDateBetweenAndIsDeletedFalse(LocalDate startDate, LocalDate endDate);

    /**
     * Check if a contract with the specified code exists and is not deleted
     * @param contractCode Contract code
     * @return True if the contract exists, false otherwise
     */
    Boolean existsByContractCodeAndIsDeletedFalse(String contractCode);

    /**
     * Check if a contract with the specified code exists and is not deleted, excluding the specified ID
     * @param contractCode Contract code
     * @param id Contract ID to exclude
     * @return True if the contract exists, false otherwise
     */
    Boolean existsByContractCodeAndIsDeletedFalseAndIdNot(String contractCode, Long id);
}
