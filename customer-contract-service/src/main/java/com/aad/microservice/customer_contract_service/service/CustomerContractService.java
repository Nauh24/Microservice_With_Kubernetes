package com.aad.microservice.customer_contract_service.service;

import com.aad.microservice.customer_contract_service.model.CustomerContract;

import java.time.LocalDate;
import java.util.List;

public interface CustomerContractService {
    CustomerContract createContract(CustomerContract contract);
    CustomerContract updateContract(CustomerContract contract);
    void deleteContract(Long id);
    CustomerContract getContractById(Long id);
    List<CustomerContract> getAllContracts();

    List<CustomerContract> getContractsByCustomerId(Long customerId);
    List<CustomerContract> getContractsByStatus(Integer status);
    List<CustomerContract> getContractsByDateRange(LocalDate startDate, LocalDate endDate);
    List<CustomerContract> getContractsByJobCategoryId(Long jobCategoryId);

    CustomerContract updateContractStatus(Long id, Integer status);

    boolean checkContractExists(Long id);
}
