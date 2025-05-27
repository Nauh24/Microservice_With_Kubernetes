package com.aad.microservice.customer_contract_service.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.aad.microservice.customer_contract_service.client")
public class FeignConfig {
}
