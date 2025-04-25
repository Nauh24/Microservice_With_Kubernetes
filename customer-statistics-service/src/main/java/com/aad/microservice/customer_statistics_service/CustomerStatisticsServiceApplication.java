package com.aad.microservice.customer_statistics_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CustomerStatisticsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CustomerStatisticsServiceApplication.class, args);
    }
}
