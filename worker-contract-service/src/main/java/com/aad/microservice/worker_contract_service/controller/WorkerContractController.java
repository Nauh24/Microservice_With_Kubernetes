package com.aad.microservice.worker_contract_service.controller;

import com.aad.microservice.worker_contract_service.model.WorkerContract;
import com.aad.microservice.worker_contract_service.service.IWorkerContractService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/worker-contract")
public class WorkerContractController {

    private final IWorkerContractService contractService;

    public WorkerContractController(IWorkerContractService contractService) {
        this.contractService = contractService;
    }

    @PostMapping
    public ResponseEntity<WorkerContract> createContract(@RequestBody WorkerContract contract) {
        return ResponseEntity.ok(contractService.createContract(contract));
    }

    @GetMapping
    public ResponseEntity<List<WorkerContract>> getAllContracts() {
        return ResponseEntity.ok(contractService.getAllContracts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkerContract> getContractById(@PathVariable Long id) {
        return ResponseEntity.ok(contractService.getContractById(id));
    }

//    @GetMapping("/worker/{workerId}")
//    public ResponseEntity<List<WorkerContract>> getContractsByWorker(@PathVariable Long workerId) {
//        return ResponseEntity.ok(contractService.getContractsByWorkerId(workerId));
//    }

    @PutMapping("")
    public ResponseEntity<WorkerContract> updateContract(@RequestBody WorkerContract contract) {
        return ResponseEntity.ok(contractService.updateContract(contract));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContract(@PathVariable Long id) {
        contractService.deleteContract(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("check-exists-active-worker-contract-by-worker-id")
    public Boolean CheckExistsActiveWorkerContractByWorkerId(@RequestParam long workerId){
        return contractService.CheckExistsActiveWorkerContractByWorkerId(workerId);
    };
}
