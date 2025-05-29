# Frontend API Gateway Configuration

## Overview
This document outlines the changes made to reconfigure the React frontend to properly connect to all microservices through the API Gateway instead of directly connecting to individual services.

## Changes Made

### 1. Cleaned Up API Client (`microservice_fe/src/services/api/apiClient.ts`)
- **Removed**: All direct service connection fallback logic
- **Simplified**: GET, POST, PUT, DELETE methods to only use API Gateway
- **Maintained**: Proper error handling and logging
- **Configuration**: Uses `REACT_APP_API_URL` environment variable (defaults to `http://localhost:8080`)

### 2. Simplified Customer Statistics Service (`microservice_fe/src/services/statistics/customerStatisticsService.ts`)
- **Removed**: All direct connection attempts and health checks
- **Removed**: Hardcoded service URLs (localhost:8085)
- **Simplified**: All methods to use only API Gateway routing
- **Fixed**: TypeScript errors in CustomerPayment mapping

### 3. Verified Service Endpoint Mappings
All frontend services now correctly use API Gateway routes:

| Frontend Service | Endpoint | API Gateway Route | Target Service |
|-----------------|----------|-------------------|----------------|
| customerService | `/api/customer` | `/api/customer/**` | customer-service:8081 |
| jobCategoryService | `/api/job-category` | `/api/job-category/**` | job-service:8082 |
| contractService | `/api/customer-contract`, `/api/job-detail`, `/api/work-shift` | `/api/customer-contract/**`, `/api/job-detail/**`, `/api/work-shift/**` | customer-contract-service:8083 |
| customerPaymentService | `/api/customer-payment` | `/api/customer-payment/**` | customer-payment-service:8084 |
| customerStatisticsService | `/api/customer-statistics` | `/api/customer-statistics/**` | customer-statistics-service:8085 |

### 4. Environment Configuration
- **File**: `microservice_fe/.env`
- **Content**: 
  ```
  PORT=3000
  REACT_APP_API_URL=http://localhost:8080
  ```

## API Gateway Routes Configuration
The API Gateway (`api-gateway/src/main/resources/application.yml`) is configured with the following routes:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: customer-service
          uri: http://customer-service:8081
          predicates:
            - Path=/api/customer/**
        - id: job-service
          uri: http://job-service:8082
          predicates:
            - Path=/api/job/**,/api/job-category/**
        - id: customer-contract-service
          uri: http://customer-contract-service:8083
          predicates:
            - Path=/api/customer-contract/**,/api/contracts/**
        - id: customer-payment-service
          uri: http://customer-payment-service:8084
          predicates:
            - Path=/api/customer-payment/**,/api/payments/**
        - id: job-detail-service
          uri: http://customer-contract-service:8083
          predicates:
            - Path=/api/job-detail/**
        - id: work-shift-service
          uri: http://customer-contract-service:8083
          predicates:
            - Path=/api/work-shift/**
        - id: customer-statistics-service
          uri: http://customer-statistics-service:8085
          predicates:
            - Path=/api/customer-statistics/**
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins: "*"
            allowedMethods: "*"
            allowedHeaders: "*"
```

## Benefits of This Configuration

1. **Centralized Routing**: All API calls go through a single entry point (API Gateway)
2. **Simplified Frontend**: No complex fallback logic or direct service connections
3. **Better Error Handling**: Consistent error handling through the API Gateway
4. **Scalability**: Easy to add new services or change service locations
5. **Security**: Single point for authentication, authorization, and rate limiting
6. **Monitoring**: Centralized logging and monitoring of all API calls

## Testing Instructions

### 1. Start All Services
```bash
# Start API Gateway
cd api-gateway
mvn spring-boot:run

# Start all microservices
cd customer-service && mvn spring-boot:run &
cd job-service && mvn spring-boot:run &
cd customer-contract-service && mvn spring-boot:run &
cd customer-payment-service && mvn spring-boot:run &
cd customer-statistics-service && mvn spring-boot:run &

# Start Frontend
cd microservice_fe
npm start
```

### 2. Verify API Gateway Connectivity
Test that all services are accessible through the API Gateway:

```bash
# Test customer service
curl http://localhost:8080/api/customer

# Test job service
curl http://localhost:8080/api/job-category

# Test customer contract service
curl http://localhost:8080/api/customer-contract

# Test customer payment service
curl http://localhost:8080/api/customer-payment

# Test customer statistics service
curl http://localhost:8080/api/customer-statistics/health
```

### 3. Frontend Testing
1. Open browser to `http://localhost:3000`
2. Navigate through all modules:
   - Customer Management
   - Job Category Management
   - Labor Hiring Contract
   - Payment Receipt
   - Customer Revenue Statistics
3. Check browser console for API calls - all should go to `http://localhost:8080`
4. Verify no direct service calls (localhost:8081, 8082, etc.)

### 4. Network Monitoring
Use browser developer tools to monitor network requests:
- All API calls should have base URL `http://localhost:8080`
- No requests should go directly to ports 8081-8085

## Troubleshooting

### Common Issues
1. **CORS Errors**: Ensure API Gateway CORS configuration is properly set
2. **Service Not Found**: Verify all microservices are running and healthy
3. **Timeout Errors**: Check if API Gateway can reach the target services
4. **Route Not Found**: Verify API Gateway route configuration matches frontend endpoints

### Debugging Steps
1. Check API Gateway logs for routing information
2. Verify service health endpoints: `http://localhost:808X/actuator/health`
3. Test direct service access to isolate issues
4. Check frontend console for detailed error messages

## Maintenance

### Adding New Services
1. Add route configuration to API Gateway
2. Create frontend service file with appropriate endpoint
3. Update this documentation

### Modifying Endpoints
1. Update API Gateway route predicates
2. Update frontend service endpoints
3. Test all affected functionality

This configuration establishes a proper microservice architecture where the frontend communicates exclusively with the API Gateway, which then routes requests to the appropriate backend services.
