package com.aad.microservice.customer_contract_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CustomerContractServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerContractServiceApplication.class, args);
    }

}
