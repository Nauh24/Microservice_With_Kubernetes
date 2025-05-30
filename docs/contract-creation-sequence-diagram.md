# Sơ Đồ Tuần Tự - Tạo Hợp Đồng Khách Hàng

## Mô tả
Sơ đồ tuần tự chi tiết mô tả luồng hoạt động khi tạo hợp đồng khách hàng trong customer-contract-service, bao gồm tất cả các lớp, service, repository và microservice liên quan.

## Sơ Đồ Tuần Tự

```mermaid
sequenceDiagram
    participant FE as Frontend
    participant AG as API Gateway
    participant CC as CustomerContractController
    participant CCS as CustomerContractService
    participant CCSI as CustomerContractServiceImpl
    participant CCR as CustomerContractRepository
    participant CClient as CustomerClient
    participant JCClient as JobCategoryClient
    participant JDS as JobDetailService
    participant JDSI as JobDetailServiceImpl
    participant JDR as JobDetailRepository
    participant WSS as WorkShiftService
    participant WSSI as WorkShiftServiceImpl
    participant WSR as WorkShiftRepository
    participant EM as EntityManager
    participant DB as PostgreSQL Database
    participant CS as Customer Service
    participant JS as Job Service
    participant GEH as GlobalExceptionHandler

    Note over FE,GEH: Luồng Tạo Hợp Đồng Khách Hàng

    %% 1. Request từ Frontend
    FE->>AG: POST /api/customer-contract (CustomerContract data)
    AG->>CC: Forward request to customer-contract-service

    %% 2. Controller nhận request
    CC->>CCS: createContract(CustomerContract contract)
    CCS->>CCSI: createContract(CustomerContract contract)

    %% 3. Validation cơ bản
    Note over CCSI: @Transactional(SERIALIZABLE)
    CCSI->>CCSI: Validate null checks
    CCSI->>CCSI: Validate date logic
    
    %% 4. Kiểm tra duplicate
    CCSI->>CCR: findByCustomerIdAndStartingDateAndEndingDateAndIsDeletedFalse()
    CCR->>DB: SELECT query
    DB-->>CCR: Existing contracts
    CCR-->>CCSI: List<CustomerContract>
    CCSI->>CCSI: Check for duplicates

    %% 5. Validation qua microservices
    CCSI->>CClient: checkCustomerExists(customerId)
    CClient->>CS: GET /api/customer/{id}/check-customer-exists
    CS-->>CClient: Boolean exists
    CClient-->>CCSI: Boolean exists

    %% 6. Validation JobCategory cho từng JobDetail
    loop For each JobDetail
        CCSI->>JCClient: checkJobCategoryExists(jobCategoryId)
        JCClient->>JS: GET /api/job-category/{id}/check-job-category-exists
        JS-->>JCClient: Boolean exists
        JCClient-->>CCSI: Boolean exists
        
        %% 7. Validation JobDetail
        CCSI->>CCSI: Validate JobDetail dates
        CCSI->>CCSI: Set default workLocation if empty
        
        %% 8. Validation WorkShift cho từng JobDetail
        loop For each WorkShift
            CCSI->>CCSI: Validate startTime, endTime
            CCSI->>CCSI: Validate numberOfWorkers > 0
            CCSI->>CCSI: Validate salary >= 0
            CCSI->>CCSI: Validate workingDays not empty
            CCSI->>CCSI: Set WorkShift metadata (createdAt, updatedAt, isDeleted)
            CCSI->>CCSI: Set bidirectional relationship
        end
        
        CCSI->>CCSI: Set JobDetail metadata
        CCSI->>CCSI: Set bidirectional relationship with Contract
    end

    %% 9. Tính toán tổng tiền
    CCSI->>CCSI: calculateTotalAmount(contract)
    loop For each JobDetail
        loop For each WorkShift
            CCSI->>CCSI: calculateWorkingDaysCount(startDate, endDate, workingDays)
            CCSI->>CCSI: Calculate: salary × workers × workingDays
        end
    end
    CCSI->>CCSI: Sum all shift amounts

    %% 10. Thiết lập metadata
    CCSI->>CCSI: Set contract metadata (createdAt, updatedAt, status=0, totalPaid=0.0)
    CCSI->>CCSI: Generate processing key for duplicate prevention

    %% 11. Lưu vào database
    CCSI->>CCR: save(CustomerContract)
    CCR->>DB: INSERT contract, job_details, work_shifts (CASCADE)
    DB-->>CCR: Saved entities with generated IDs
    CCR-->>CCSI: CustomerContract with ID

    %% 12. Force flush
    CCSI->>EM: flush()
    EM->>DB: Force immediate persistence
    DB-->>EM: Confirmation
    EM-->>CCSI: Success

    %% 13. Return response
    CCSI-->>CCS: CustomerContract saved
    CCS-->>CC: CustomerContract saved
    CC-->>AG: ResponseEntity<CustomerContract>
    AG-->>FE: HTTP 200 OK + CustomerContract data

    %% Error Handling
    alt Validation Error
        CCSI->>GEH: throw AppException
        GEH->>GEH: Map error code to HTTP status
        GEH-->>CC: ResponseEntity<ErrorResponse>
        CC-->>AG: HTTP 4xx + Error details
        AG-->>FE: Error response
    end

    alt Database Error
        CCR->>GEH: throw Exception
        GEH->>GEH: Handle generic exception
        GEH-->>CC: ResponseEntity<ErrorResponse>
        CC-->>AG: HTTP 500 + Error details
        AG-->>FE: Error response
    end

    Note over FE,GEH: Kết thúc luồng tạo hợp đồng
```

## Giải thích các bước chính

### 1. Request Processing (Bước 1-2)
- Frontend gửi POST request qua API Gateway
- Request được route đến CustomerContractController
- Controller delegate đến Service layer

### 2. Validation Phase (Bước 3-8)
- **Validation cơ bản**: Null checks, date logic validation
- **Duplicate check**: Kiểm tra hợp đồng trùng lặp trong database
- **Microservice validation**: Validate customer và job category qua Feign clients
- **Detail validation**: Validate từng JobDetail và WorkShift

### 3. Business Logic (Bước 9-10)
- **Tính toán tổng tiền**: Dựa trên công thức salary × workers × working days
- **Metadata setup**: Thiết lập các thông tin audit và trạng thái

### 4. Persistence (Bước 11-12)
- **Database save**: Lưu hợp đồng với cascade cho JobDetail và WorkShift
- **Force flush**: Đảm bảo dữ liệu được persist ngay lập tức

### 5. Response (Bước 13)
- Trả về CustomerContract đã được lưu với ID
- Response được truyền ngược lại Frontend

### 6. Error Handling
- **Validation errors**: AppException được map sang HTTP 4xx
- **System errors**: Generic Exception được map sang HTTP 500

## Các lớp tham gia

### Core Components
- **CustomerContractController**: REST endpoint handler
- **CustomerContractServiceImpl**: Business logic implementation
- **CustomerContractRepository**: Data access layer

### Supporting Services
- **JobDetailService/Impl**: Quản lý chi tiết công việc
- **WorkShiftService/Impl**: Quản lý ca làm việc
- **JobDetailRepository/WorkShiftRepository**: Data access cho entities con

### External Integration
- **CustomerClient**: Feign client gọi customer-service
- **JobCategoryClient**: Feign client gọi job-service
- **Customer Service**: External microservice (port 8081)
- **Job Service**: External microservice (port 8082)

### Infrastructure
- **EntityManager**: JPA persistence context management
- **GlobalExceptionHandler**: Centralized error handling
- **PostgreSQL Database**: Data storage layer
