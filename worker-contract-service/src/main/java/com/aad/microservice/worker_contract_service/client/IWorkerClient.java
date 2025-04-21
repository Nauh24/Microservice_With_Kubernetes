package com.aad.microservice.worker_contract_service.client;

import com.aad.microservice.worker_contract_service.externalModel.Worker;
import feign.Request;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "worker-service", url = "http://localhost:8080/api/worker")
public interface
IWorkerClient {
    @RequestMapping(method = RequestMethod.GET, value = "/{id}/check-worker-exists")
    Boolean CheckWorkerExists(@PathVariable long id);

    @RequestMapping(method = RequestMethod.GET, value="/{id}")
    Worker GetWorkerById(@PathVariable long id);
}
