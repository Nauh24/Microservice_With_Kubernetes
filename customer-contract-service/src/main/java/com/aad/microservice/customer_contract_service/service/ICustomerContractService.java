package com.aad.microservice.customer_contract_service.service;

import com.aad.microservice.customer_contract_service.model.CustomerContract;
import com.aad.microservice.customer_contract_service.model.ContractStatus;

import java.time.LocalDate;
import java.util.List;

public interface ICustomerContractService {
    CustomerContract createContract(CustomerContract contract);
    CustomerContract updateContract(CustomerContract contract);
    void deleteContract(Long id);
    CustomerContract getContractById(Long id);
    List<CustomerContract> getAllContracts();
    
    List<CustomerContract> getContractsByCustomerId(Long customerId);
    List<CustomerContract> getContractsByStatus(ContractStatus status);
    List<CustomerContract> getContractsByDateRange(LocalDate startDate, LocalDate endDate);
    
    CustomerContract updateContractStatus(Long id, ContractStatus status);
    
    boolean checkContractExists(Long id);
}
