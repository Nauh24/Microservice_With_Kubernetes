package com.aad.microservice.worker_service.controller;

import com.aad.microservice.worker_service.model.Worker;
import com.aad.microservice.worker_service.service.IWorkerService;
import com.aad.microservice.worker_service.service.WorkerServiceImpl;
import feign.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/worker")
//@RequiredArgsConstructor
public class WorkerController {
    private final IWorkerService _workerService;

    public WorkerController(IWorkerService workerService) {
        _workerService = workerService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Worker> GetWorkerById(@PathVariable Long id){
        Worker worker = _workerService.getWorkerById(id);
        return ResponseEntity.ok(worker);
    }

    @PostMapping
    public ResponseEntity<Worker> createWorker(@RequestBody Worker worker) {
        return ResponseEntity.ok(_workerService.createWorker(worker));
    }

    @GetMapping
    public ResponseEntity<List<Worker>> getAllWorkers() {
        return ResponseEntity.ok(_workerService.getAllWorkers());
    }

    @PutMapping()
    public ResponseEntity<Worker> updateWorker(@RequestBody Worker worker) {
        return ResponseEntity.ok(_workerService.updateWorker(worker));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorker(@PathVariable Long id) {
        _workerService.deleteWorker(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/check-worker-exists")
    public ResponseEntity<Boolean> checkWorkerExists(@PathVariable Long id) {
        boolean existed = _workerService.checkWorkerExists(id);
        return ResponseEntity.ok(existed);
    }
}
