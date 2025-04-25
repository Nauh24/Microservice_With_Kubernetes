package com.aad.microservice.customer_statistics_service.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.aad.microservice.customer_statistics_service.client")
public class FeignConfig {
}
