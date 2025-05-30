# Biểu đồ lớp chi tiết - Module Ký hợp đồng thuê lao động (Microservice Architecture)

## Tổng quan kiến trúc
Module này được thiết kế theo kiến trúc microservice với các service độc lập:
- **customer-contract-service**: Quản lý hợp đồng khách hàng
- **customer-service**: Quản lý thông tin khách hàng
- **job-service**: Quản lý loại công việc
- **api-gateway**: Định tuyến và xử lý request

## Biểu đồ lớp UML

```mermaid
classDiagram
    direction TB

    %% ===== FRONTEND LAYER =====
    class Frontend {
        <<React Application>>
        +ContractCreationForm
        +ContractDetailView
        +CustomerSelectionDialog
        +JobCategorySelector
        +WorkShiftScheduler
        +ContractListView
    }

    %% ===== API GATEWAY =====
    class ApiGateway {
        <<Spring Cloud Gateway>>
        -RouteLocator routeLocator
        -CorsConfiguration corsConfig
        +routeToCustomerContractService()
        +routeToCustomerService()
        +routeToJobService()
        +handleCors()
        +loadBalancing()
    }

    %% ===== CUSTOMER-CONTRACT-SERVICE =====
    namespace CustomerContractService {
        class CustomerContractController {
            <<@RestController>>
            -CustomerContractService customerContractService
            +createContract(CustomerContractRequest): ResponseEntity~CustomerContract~
            +getContractById(Long id): ResponseEntity~CustomerContract~
            +getContractsByCustomerId(Long customerId): ResponseEntity~List~CustomerContract~~
            +updateContract(Long id, CustomerContractRequest): ResponseEntity~CustomerContract~
            +deleteContract(Long id): ResponseEntity~Void~
            +getAllContracts(Pageable): ResponseEntity~Page~CustomerContract~~
            +getContractsByStatus(Integer status): ResponseEntity~List~CustomerContract~~
            +calculateContractAmount(CustomerContractRequest): ResponseEntity~Double~
        }

        class CustomerContractService {
            <<@Service>>
            -CustomerContractRepository customerContractRepository
            -JobDetailRepository jobDetailRepository
            -WorkShiftRepository workShiftRepository
            -CustomerServiceClient customerServiceClient
            -JobServiceClient jobServiceClient
            +createContract(CustomerContract): CustomerContract
            +getContractById(Long): CustomerContract
            +getContractsByCustomerId(Long): List~CustomerContract~
            +updateContract(Long, CustomerContract): CustomerContract
            +deleteContract(Long): void
            +getAllContracts(Pageable): Page~CustomerContract~
            +calculateTotalAmount(CustomerContract): Double
            +validateContract(CustomerContract): boolean
            +validateCustomerExists(Long): boolean
            +validateJobCategories(List~Long~): boolean
            +calculateWorkingDates(WorkShift, LocalDate, LocalDate): List~LocalDate~
        }

        class CustomerContract {
            <<@Entity>>
            -Long id
            -Long customerId
            -LocalDate startDate
            -LocalDate endDate
            -Double totalAmount
            -Double paidAmount
            -String description
            -String address
            -Integer status
            -List~JobDetail~ jobDetails
            -LocalDateTime createdAt
            -LocalDateTime updatedAt
            +calculateWorkingDays(): Integer
            +calculateTotalSalary(): Double
            +isActive(): boolean
            +isPending(): boolean
            +isCompleted(): boolean
            +isCancelled(): boolean
            +getRemainingAmount(): Double
            +getPaymentProgress(): Double
        }

        class JobDetail {
            <<@Entity>>
            -Long id
            -CustomerContract contract
            -Long jobCategoryId
            -LocalDate startDate
            -LocalDate endDate
            -String workLocation
            -List~WorkShift~ workShifts
            -LocalDateTime createdAt
            -LocalDateTime updatedAt
            +calculateJobAmount(): Double
            +getWorkingDaysCount(): Integer
            +getTotalWorkers(): Integer
            +getJobCategoryName(): String
        }

        class WorkShift {
            <<@Entity>>
            -Long id
            -JobDetail jobDetail
            -String startTime
            -String endTime
            -Integer numberOfWorkers
            -Double salary
            -String workingDays
            -LocalDateTime createdAt
            -LocalDateTime updatedAt
            +calculateShiftAmount(): Double
            +getWorkingDaysList(): List~String~
            +calculateActualWorkingDates(LocalDate, LocalDate): List~LocalDate~
            +getShiftDuration(): Duration
            +isValidTimeRange(): boolean
        }

        class CustomerContractRepository {
            <<@Repository>>
            <<interface>>
            +findByCustomerId(Long): List~CustomerContract~
            +findByStatus(Integer): List~CustomerContract~
            +findByDateRange(LocalDate, LocalDate): List~CustomerContract~
            +findByCustomerIdAndStatus(Long, Integer): List~CustomerContract~
            +countByCustomerId(Long): Long
            +findActiveContractsByCustomerId(Long): List~CustomerContract~
        }

        class JobDetailRepository {
            <<@Repository>>
            <<interface>>
            +findByContractId(Long): List~JobDetail~
            +findByJobCategoryId(Long): List~JobDetail~
            +findByContractIdOrderByCreatedAt(Long): List~JobDetail~
            +countByJobCategoryId(Long): Long
        }

        class WorkShiftRepository {
            <<@Repository>>
            <<interface>>
            +findByJobDetailId(Long): List~WorkShift~
            +findByJobDetailIdOrderByStartTime(Long): List~WorkShift~
            +findOverlappingShifts(Long, String, String): List~WorkShift~
        }
    }

    %% ===== FEIGN CLIENTS =====
    namespace FeignClients {
        class CustomerServiceClient {
            <<@FeignClient>>
            <<interface>>
            +getCustomerById(Long id): Customer
            +getAllCustomers(): List~Customer~
            +searchCustomers(String keyword): List~Customer~
            +validateCustomer(Long id): boolean
        }

        class JobServiceClient {
            <<@FeignClient>>
            <<interface>>
            +getJobCategoryById(Long id): JobCategory
            +getAllJobCategories(): List~JobCategory~
            +getActiveJobCategories(): List~JobCategory~
            +validateJobCategory(Long id): boolean
        }
    }

    %% ===== CUSTOMER-SERVICE =====
    namespace CustomerService {
        class CustomerController {
            <<@RestController>>
            -CustomerService customerService
            +getCustomerById(Long id): ResponseEntity~Customer~
            +getAllCustomers(): ResponseEntity~List~Customer~~
            +searchCustomers(String keyword): ResponseEntity~List~Customer~~
            +createCustomer(CustomerRequest): ResponseEntity~Customer~
            +updateCustomer(Long id, CustomerRequest): ResponseEntity~Customer~
            +deleteCustomer(Long id): ResponseEntity~Void~
            +getCustomerContracts(Long id): ResponseEntity~List~CustomerContract~~
        }

        class CustomerService {
            <<@Service>>
            -CustomerRepository customerRepository
            +getCustomerById(Long): Customer
            +getAllCustomers(): List~Customer~
            +searchCustomers(String): List~Customer~
            +createCustomer(Customer): Customer
            +updateCustomer(Long, Customer): Customer
            +deleteCustomer(Long): void
            +validateCustomer(Customer): boolean
            +isCustomerExists(Long): boolean
        }

        class Customer {
            <<@Entity>>
            -Long id
            -String fullname
            -String companyName
            -String phoneNumber
            -String email
            -String address
            -LocalDateTime createdAt
            -LocalDateTime updatedAt
            +getDisplayName(): String
            +isValidEmail(): boolean
            +isValidPhone(): boolean
            +getFullAddress(): String
        }

        class CustomerRepository {
            <<@Repository>>
            <<interface>>
            +findByFullnameContaining(String): List~Customer~
            +findByPhoneNumberContaining(String): List~Customer~
            +findByCompanyNameContaining(String): List~Customer~
            +findByEmailContaining(String): List~Customer~
            +existsByPhoneNumber(String): boolean
            +existsByEmail(String): boolean
        }
    }

    %% ===== JOB-SERVICE =====
    namespace JobService {
        class JobCategoryController {
            <<@RestController>>
            -JobCategoryService jobCategoryService
            +getJobCategoryById(Long id): ResponseEntity~JobCategory~
            +getAllJobCategories(): ResponseEntity~List~JobCategory~~
            +getActiveJobCategories(): ResponseEntity~List~JobCategory~~
            +createJobCategory(JobCategoryRequest): ResponseEntity~JobCategory~
            +updateJobCategory(Long id, JobCategoryRequest): ResponseEntity~JobCategory~
            +deleteJobCategory(Long id): ResponseEntity~Void~
            +searchJobCategories(String keyword): ResponseEntity~List~JobCategory~~
        }

        class JobCategoryService {
            <<@Service>>
            -JobCategoryRepository jobCategoryRepository
            +getJobCategoryById(Long): JobCategory
            +getAllJobCategories(): List~JobCategory~
            +getActiveJobCategories(): List~JobCategory~
            +createJobCategory(JobCategory): JobCategory
            +updateJobCategory(Long, JobCategory): JobCategory
            +deleteJobCategory(Long): void
            +searchJobCategories(String): List~JobCategory~
            +validateJobCategory(JobCategory): boolean
        }

        class JobCategory {
            <<@Entity>>
            -Long id
            -String name
            -String description
            -Boolean isDeleted
            -LocalDateTime createdAt
            -LocalDateTime updatedAt
            +isActive(): boolean
            +getDisplayName(): String
        }

        class JobCategoryRepository {
            <<@Repository>>
            <<interface>>
            +findByIsDeletedFalse(): List~JobCategory~
            +findByNameContaining(String): List~JobCategory~
            +findByNameContainingAndIsDeletedFalse(String): List~JobCategory~
            +existsByName(String): boolean
        }
    }

    %% ===== ENUMS AND VALUE OBJECTS =====
    class ContractStatus {
        <<enumeration>>
        PENDING = 0
        ACTIVE = 1
        COMPLETED = 2
        CANCELLED = 3
        +getDisplayName(): String
        +isValidTransition(ContractStatus): boolean
    }

    class WorkingDays {
        <<value object>>
        -String daysString
        +getDaysList(): List~Integer~
        +getVietnameseDayNames(): List~String~
        +isValidDayFormat(): boolean
        +containsDay(Integer): boolean
    }

    %% ===== RELATIONSHIPS =====
    Frontend --> ApiGateway : HTTP Requests
    ApiGateway --> CustomerContractController : Route /api/contracts/**
    ApiGateway --> CustomerController : Route /api/customers/**
    ApiGateway --> JobCategoryController : Route /api/job-categories/**

    CustomerContractController --> CustomerContractService : uses
    CustomerContractService --> CustomerContractRepository : uses
    CustomerContractService --> JobDetailRepository : uses
    CustomerContractService --> WorkShiftRepository : uses
    CustomerContractService --> CustomerServiceClient : calls
    CustomerContractService --> JobServiceClient : calls

    CustomerContract ||--o{ JobDetail : "1..*"
    JobDetail ||--o{ WorkShift : "1..*"
    CustomerContract --> ContractStatus : uses
    WorkShift --> WorkingDays : uses

    CustomerServiceClient ..> CustomerController : HTTP calls
    JobServiceClient ..> JobCategoryController : HTTP calls

    CustomerController --> CustomerService : uses
    CustomerService --> CustomerRepository : uses

    JobCategoryController --> JobCategoryService : uses
    JobCategoryService --> JobCategoryRepository : uses
```

## Mô tả chi tiết các thành phần

### 1. Frontend Layer (React Application)
**Chức năng**: Giao diện người dùng cho module hợp đồng
- `ContractCreationForm`: Form tạo hợp đồng mới
- `ContractDetailView`: Hiển thị chi tiết hợp đồng
- `CustomerSelectionDialog`: Dialog chọn khách hàng
- `JobCategorySelector`: Component chọn loại công việc
- `WorkShiftScheduler`: Lập lịch ca làm việc
- `ContractListView`: Danh sách hợp đồng

### 2. API Gateway (Spring Cloud Gateway)
**Chức năng**: Điểm trung tâm định tuyến và xử lý request
- Định tuyến request đến các microservice
- Xử lý CORS và load balancing
- Centralized authentication/authorization

### 3. Customer-Contract-Service
**Chức năng chính**: Core service quản lý hợp đồng khách hàng

**Controller Layer**:
- `CustomerContractController`: REST API endpoints với đầy đủ CRUD operations
- Hỗ trợ pagination và filtering

**Service Layer**:
- `CustomerContractService`: Business logic phức tạp
- Validation và tính toán tự động
- Giao tiếp với external services

**Entity Layer**:
- `CustomerContract`: Entity chính với relationship mapping
- `JobDetail`: Chi tiết công việc trong hợp đồng
- `WorkShift`: Ca làm việc với thời gian và lương cụ thể

**Repository Layer**:
- Custom query methods cho business requirements
- Optimized queries với proper indexing

### 4. Customer-Service
**Chức năng**: Quản lý thông tin khách hàng độc lập
- CRUD operations cho Customer entity
- Search và validation functionality
- Data consistency và integrity

### 5. Job-Service
**Chức năng**: Quản lý danh mục loại công việc
- Quản lý JobCategory với soft delete
- Search và filtering capabilities
- Master data management

### 6. Feign Clients
**Chức năng**: Inter-service communication
- `CustomerServiceClient`: Gọi customer-service APIs
- `JobServiceClient`: Gọi job-service APIs
- Circuit breaker và retry mechanisms

### 7. Value Objects và Enums
- `ContractStatus`: Enum quản lý trạng thái hợp đồng
- `WorkingDays`: Value object xử lý ngày làm việc

## Luồng xử lý tạo hợp đồng

1. **Frontend Request**: User submit contract creation form
2. **API Gateway**: Route request to customer-contract-service
3. **Controller**: CustomerContractController receives request
4. **Service Validation**:
   - Validate contract data
   - Call CustomerServiceClient to verify customer exists
   - Call JobServiceClient to verify job categories
5. **Business Logic**:
   - Calculate total amount automatically
   - Generate working dates from shifts and date range
   - Create contract with job details and work shifts
6. **Persistence**: Save to database with transaction management
7. **Response**: Return created contract with full details

## Đặc điểm kiến trúc Microservice

### Độc lập (Independence)
- Mỗi service có database riêng biệt
- Independent deployment và scaling
- Technology stack flexibility

### Giao tiếp (Communication)
- HTTP/REST API qua Feign Client
- Asynchronous messaging cho events
- Circuit breaker pattern cho fault tolerance

### Quản lý dữ liệu (Data Management)
- Database per service pattern
- Eventual consistency
- Distributed transaction management

### Monitoring và Logging
- Centralized logging với correlation IDs
- Health checks và metrics
- Distributed tracing

### Security
- JWT token authentication
- Role-based authorization
- API rate limiting
