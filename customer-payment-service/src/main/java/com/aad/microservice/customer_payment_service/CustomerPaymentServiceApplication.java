package com.aad.microservice.customer_payment_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CustomerPaymentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CustomerPaymentServiceApplication.class, args);
    }
}
