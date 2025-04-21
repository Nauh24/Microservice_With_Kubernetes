package com.example.mircroservice.register_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "worker-contract-service", url = "http://localhost:8084/api/worker-contract")
public interface IWorkerContractClient {

    @RequestMapping(method = RequestMethod.GET, value = "/check-exists-active-worker-contract-by-worker-id")
    boolean CheckExistsActiveWorkerContractByWorkerId(@RequestParam long workerId);
}
