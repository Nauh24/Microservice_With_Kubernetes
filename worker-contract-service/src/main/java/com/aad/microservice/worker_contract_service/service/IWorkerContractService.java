package com.aad.microservice.worker_contract_service.service;

import com.aad.microservice.worker_contract_service.model.WorkerContract;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IWorkerContractService {
    WorkerContract createContract(WorkerContract contract);
    WorkerContract updateContract(WorkerContract contract);

    void deleteContract(Long id);
    WorkerContract getContractById(Long id);
//    List<WorkerContract> getContractsByWorkerId(Long workerId);
    List<WorkerContract> getAllContracts();
//    WorkerContract terminateContractEarly(Long id); // Cân nhắc nghiệp vụ

    Boolean CheckExistsActiveWorkerContractByWorkerId(long workerId);
}
