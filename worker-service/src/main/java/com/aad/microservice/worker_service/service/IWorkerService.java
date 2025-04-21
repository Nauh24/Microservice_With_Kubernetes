package com.aad.microservice.worker_service.service;

import com.aad.microservice.worker_service.model.Worker;

import java.util.List;
import java.util.Optional;

public interface IWorkerService {
    Worker createWorker(Worker worker);
    Worker updateWorker(Worker worker);
    void deleteWorker(Long id);
    Worker getWorkerById(Long id);
    List<Worker> getAllWorkers();

    boolean checkWorkerExists(Long id);
}
