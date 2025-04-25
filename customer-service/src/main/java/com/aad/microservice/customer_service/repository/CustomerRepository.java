package com.aad.microservice.customer_service.repository;

import com.aad.microservice.customer_service.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByIsDeletedFalse();
    Optional<Customer> findByIdAndIsDeletedFalse(Long id);

    Boolean existsByEmailAndIsDeletedFalse(String email);
    Boolean existsByPhoneNumberAndIsDeletedFalse(String phoneNumber);

    Boolean existsByEmailAndIsDeletedFalseAndIdNot(String email, Long id);
    Boolean existsByPhoneNumberAndIsDeletedFalseAndIdNot(String phoneNumber, Long id);

    // Search methods
    List<Customer> findByFullnameContainingIgnoreCaseAndIsDeletedFalse(String fullname);
    List<Customer> findByPhoneNumberContainingAndIsDeletedFalse(String phoneNumber);
    List<Customer> findByFullnameContainingIgnoreCaseAndPhoneNumberContainingAndIsDeletedFalse(String fullname, String phoneNumber);
}
