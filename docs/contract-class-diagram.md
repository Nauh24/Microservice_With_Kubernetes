# Biểu đồ lớp chi tiết cho chức năng "Ký hợp đồng với khách thuê lao động"

Biểu đồ lớp dưới đây mô tả chi tiết các lớp và mối quan hệ giữa chúng trong chức năng "Ký hợp đồng với khách thuê lao động" theo kiến trúc vi dịch vụ (microservices). Được cập nhật dựa trên code thực tế trong customer-contract-service.

## Các thành phần chính

1. **frontend**: Giao diện người dùng React với PrimeReact
2. **api-gateway**: Cổng vào cho tất cả các request từ frontend đến các microservice
3. **customer-service**: Quản lý thông tin khách hàng
4. **job-service**: Quản lý thông tin loại công việc
5. **customer-contract-service**: Quản lý hợp đồng, chi tiết công việc và ca làm việc

## Biểu đồ lớp chi tiết

```mermaid
classDiagram
    %% Frontend Components
    namespace frontend {
        class CustomerContractList {
            +contracts: CustomerContract[]
            +loading: boolean
            +fetchContracts()
            +handleCreateContract()
            +handleViewContract(id)
            +handleDeleteContract(id)
        }

        class ContractForm {
            +contract: CustomerContract
            +customers: Customer[]
            +jobCategories: JobCategory[]
            +onSubmit(contract)
            +onCancel()
            +calculateTotalAmount()
            +validateForm()
        }

        class ContractDetail {
            +contract: CustomerContract
            +workSchedule: WorkScheduleItem[]
            +formatCurrency(amount)
            +formatDate(date)
            +calculateWorkingDates()
        }

        class CustomerSelectionDialog {
            +customers: Customer[]
            +selectedCustomer: Customer
            +searchTerm: string
            +onSelect(customer)
            +onClose()
            +searchCustomers()
        }
    }

    %% API Gateway
    namespace api-gateway {
        class ApiGateway {
            +routeToCustomerService()
            +routeToJobService()
            +routeToContractService()
            +handleAuthentication()
            +loadBalance()
        }
    }

    %% Customer Service
    namespace customer-service {
        class Customer {
            -id: Long
            -fullName: String
            -companyName: String
            -phoneNumber: String
            -email: String
            -address: String
            -isDeleted: Boolean
            -createdAt: LocalDateTime
            -updatedAt: LocalDateTime
        }

        class CustomerController {
            +getCustomerById(id): Customer
            +getAllCustomers(): List~Customer~
            +createCustomer(customer): Customer
            +updateCustomer(customer): Customer
            +deleteCustomer(id): void
            +checkCustomerExists(id): Boolean
            +searchCustomers(fullName, phoneNumber): List~Customer~
        }

        class CustomerService {
            <<interface>>
            +getCustomerById(id): Customer
            +getAllCustomers(): List~Customer~
            +createCustomer(customer): Customer
            +updateCustomer(customer): Customer
            +deleteCustomer(id): void
            +checkCustomerExists(id): Boolean
            +searchCustomers(fullName, phoneNumber): List~Customer~
        }

        class CustomerServiceImpl {
            -customerRepository: CustomerRepository
            +getCustomerById(id): Customer
            +getAllCustomers(): List~Customer~
            +createCustomer(customer): Customer
            +updateCustomer(customer): Customer
            +deleteCustomer(id): void
            +checkCustomerExists(id): Boolean
            +searchCustomers(fullName, phoneNumber): List~Customer~
        }

        class CustomerRepository {
            <<interface>>
            +findByIdAndIsDeletedFalse(id): Optional~Customer~
            +findByIsDeletedFalse(): List~Customer~
            +findByFullNameContainingAndIsDeletedFalse(fullName): List~Customer~
            +findByPhoneNumberContainingAndIsDeletedFalse(phoneNumber): List~Customer~
        }
    }

    %% Job Service
    namespace job-service {
        class JobCategory {
            -id: Long
            -name: String
            -description: String
            -isDeleted: Boolean
            -createdAt: LocalDateTime
            -updatedAt: LocalDateTime
        }

        class JobCategoryController {
            +getJobCategoryById(id): JobCategory
            +getAllJobCategories(): List~JobCategory~
            +createJobCategory(jobCategory): JobCategory
            +updateJobCategory(jobCategory): JobCategory
            +deleteJobCategory(id): void
            +checkJobCategoryExists(id): Boolean
        }

        class JobCategoryService {
            <<interface>>
            +getJobCategoryById(id): JobCategory
            +getAllJobCategories(): List~JobCategory~
            +createJobCategory(jobCategory): JobCategory
            +updateJobCategory(jobCategory): JobCategory
            +deleteJobCategory(id): void
            +checkJobCategoryExists(id): Boolean
        }

        class JobCategoryServiceImpl {
            -jobCategoryRepository: JobCategoryRepository
            +getJobCategoryById(id): JobCategory
            +getAllJobCategories(): List~JobCategory~
            +createJobCategory(jobCategory): JobCategory
            +updateJobCategory(jobCategory): JobCategory
            +deleteJobCategory(id): void
            +checkJobCategoryExists(id): Boolean
        }

        class JobCategoryRepository {
            <<interface>>
            +findByIdAndIsDeletedFalse(id): Optional~JobCategory~
            +findByIsDeletedFalse(): List~JobCategory~
        }
    }

    %% Customer Contract Service - Main Entities
    namespace customer-contract-service {
        class CustomerContract {
            -id: Long
            -startingDate: LocalDate
            -endingDate: LocalDate
            -totalAmount: Double
            -totalPaid: Double
            -address: String
            -description: String
            -customerId: Long
            -status: Integer
            -isDeleted: Boolean
            -createdAt: LocalDateTime
            -updatedAt: LocalDateTime
            -jobDetails: List~JobDetail~
            +addJobDetail(jobDetail): void
            +removeJobDetail(jobDetail): void
        }

        class JobDetail {
            -id: Long
            -startDate: LocalDate
            -endDate: LocalDate
            -workLocation: String
            -jobCategoryId: Long
            -isDeleted: Boolean
            -createdAt: LocalDateTime
            -updatedAt: LocalDateTime
            -contract: CustomerContract
            -workShifts: List~WorkShift~
        }

        class WorkShift {
            -id: Long
            -startTime: String
            -endTime: String
            -numberOfWorkers: Integer
            -salary: Double
            -workingDays: String
            -isDeleted: Boolean
            -createdAt: LocalDateTime
            -updatedAt: LocalDateTime
            -jobDetail: JobDetail
        }

        %% Controllers
        class CustomerContractController {
            -contractService: CustomerContractService
            +getContractById(id): CustomerContract
            +createContract(contract): CustomerContract
            +getAllContracts(): List~CustomerContract~
            +updateContract(contract): CustomerContract
            +deleteContract(id): void
            +getContractsByCustomerId(customerId): List~CustomerContract~
            +getContractsByStatus(status): List~CustomerContract~
            +getContractsByDateRange(startDate, endDate): List~CustomerContract~
            +updateContractStatus(id, status): CustomerContract
            +checkContractExists(id): Boolean
            +calculateWorkingDates(startDate, endDate, workingDays): List~String~
        }

        class JobDetailController {
            -jobDetailService: JobDetailService
            +getJobDetailById(id): JobDetail
            +createJobDetail(jobDetail): JobDetail
            +getAllJobDetails(): List~JobDetail~
            +updateJobDetail(jobDetail): JobDetail
            +deleteJobDetail(id): void
            +getJobDetailsByContractId(contractId): List~JobDetail~
            +getJobDetailsByJobCategoryId(jobCategoryId): List~JobDetail~
            +checkJobDetailExists(id): Boolean
        }

        class WorkShiftController {
            -workShiftService: WorkShiftService
            +getWorkShiftById(id): WorkShift
            +createWorkShift(workShift): WorkShift
            +getAllWorkShifts(): List~WorkShift~
            +updateWorkShift(workShift): WorkShift
            +deleteWorkShift(id): void
            +getWorkShiftsByJobDetailId(jobDetailId): List~WorkShift~
            +checkWorkShiftExists(id): Boolean
        }

        %% Services
        class CustomerContractService {
            <<interface>>
            +createContract(contract): CustomerContract
            +updateContract(contract): CustomerContract
            +deleteContract(id): void
            +getContractById(id): CustomerContract
            +getAllContracts(): List~CustomerContract~
            +getContractsByCustomerId(customerId): List~CustomerContract~
            +getContractsByStatus(status): List~CustomerContract~
            +getContractsByDateRange(startDate, endDate): List~CustomerContract~
            +updateContractStatus(id, status): CustomerContract
            +checkContractExists(id): Boolean
            +calculateWorkingDatesForShift(startDate, endDate, workingDays): List~String~
        }

        class CustomerContractServiceImpl {
            -contractRepository: CustomerContractRepository
            -customerClient: CustomerClient
            -jobCategoryClient: JobCategoryClient
            -entityManager: EntityManager
            +createContract(contract): CustomerContract
            +updateContract(contract): CustomerContract
            +deleteContract(id): void
            +getContractById(id): CustomerContract
            +getAllContracts(): List~CustomerContract~
            +getContractsByCustomerId(customerId): List~CustomerContract~
            +getContractsByStatus(status): List~CustomerContract~
            +getContractsByDateRange(startDate, endDate): List~CustomerContract~
            +updateContractStatus(id, status): CustomerContract
            +checkContractExists(id): Boolean
            +calculateWorkingDatesForShift(startDate, endDate, workingDays): List~String~
            -validateContract(contract): void
            -calculateTotalAmount(contract): Double
        }

        class JobDetailService {
            <<interface>>
            +createJobDetail(jobDetail): JobDetail
            +updateJobDetail(jobDetail): JobDetail
            +deleteJobDetail(id): void
            +getJobDetailById(id): JobDetail
            +getAllJobDetails(): List~JobDetail~
            +getJobDetailsByContractId(contractId): List~JobDetail~
            +getJobDetailsByJobCategoryId(jobCategoryId): List~JobDetail~
            +checkJobDetailExists(id): Boolean
        }

        class JobDetailServiceImpl {
            -jobDetailRepository: JobDetailRepository
            -contractRepository: CustomerContractRepository
            -jobCategoryClient: JobCategoryClient
            +createJobDetail(jobDetail): JobDetail
            +updateJobDetail(jobDetail): JobDetail
            +deleteJobDetail(id): void
            +getJobDetailById(id): JobDetail
            +getAllJobDetails(): List~JobDetail~
            +getJobDetailsByContractId(contractId): List~JobDetail~
            +getJobDetailsByJobCategoryId(jobCategoryId): List~JobDetail~
            +checkJobDetailExists(id): Boolean
        }

        class WorkShiftService {
            <<interface>>
            +createWorkShift(workShift): WorkShift
            +updateWorkShift(workShift): WorkShift
            +deleteWorkShift(id): void
            +getWorkShiftById(id): WorkShift
            +getAllWorkShifts(): List~WorkShift~
            +getWorkShiftsByJobDetailId(jobDetailId): List~WorkShift~
            +checkWorkShiftExists(id): Boolean
        }

        class WorkShiftServiceImpl {
            -workShiftRepository: WorkShiftRepository
            -jobDetailRepository: JobDetailRepository
            +createWorkShift(workShift): WorkShift
            +updateWorkShift(workShift): WorkShift
            +deleteWorkShift(id): void
            +getWorkShiftById(id): WorkShift
            +getAllWorkShifts(): List~WorkShift~
            +getWorkShiftsByJobDetailId(jobDetailId): List~WorkShift~
            +checkWorkShiftExists(id): Boolean
        }

        %% Repositories
        class CustomerContractRepository {
            <<interface>>
            +findByIsDeletedFalse(): List~CustomerContract~
            +findByIdAndIsDeletedFalse(id): Optional~CustomerContract~
            +findByCustomerIdAndIsDeletedFalse(customerId): List~CustomerContract~
            +findByStatusAndIsDeletedFalse(status): List~CustomerContract~
            +findByStartingDateBetweenAndIsDeletedFalse(startDate, endDate): List~CustomerContract~
            +findByCustomerIdAndStartingDateAndEndingDateAndIsDeletedFalse(customerId, startingDate, endingDate): List~CustomerContract~
        }

        class JobDetailRepository {
            <<interface>>
            +findByIsDeletedFalse(): List~JobDetail~
            +findByIdAndIsDeletedFalse(id): Optional~JobDetail~
            +findByContract_IdAndIsDeletedFalse(contractId): List~JobDetail~
            +findByJobCategoryIdAndIsDeletedFalse(jobCategoryId): List~JobDetail~
        }

        class WorkShiftRepository {
            <<interface>>
            +findByIsDeletedFalse(): List~WorkShift~
            +findByIdAndIsDeletedFalse(id): Optional~WorkShift~
            +findByJobDetail_IdAndIsDeletedFalse(jobDetailId): List~WorkShift~
        }

        %% External Service Clients
        class CustomerClient {
            <<FeignClient>>
            +getCustomerById(id): Customer
            +checkCustomerExists(id): Boolean
        }

        class JobCategoryClient {
            <<FeignClient>>
            +getJobCategoryById(id): JobCategory
            +checkJobCategoryExists(id): Boolean
        }

        %% Model classes for external services
        class Customer {
            -id: Long
            -fullName: String
            -companyName: String
            -phoneNumber: String
            -email: String
            -address: String
            -isDeleted: Boolean
            -createdAt: LocalDateTime
            -updatedAt: LocalDateTime
        }

        class JobCategory {
            -id: Long
            -name: String
            -description: String
            -isDeleted: Boolean
            -createdAt: LocalDateTime
            -updatedAt: LocalDateTime
        }
    }

    %% Relationships - Frontend to API Gateway
    CustomerContractList --> ApiGateway : HTTP requests
    ContractForm --> ApiGateway : HTTP requests
    ContractDetail --> ApiGateway : HTTP requests
    CustomerSelectionDialog --> ApiGateway : HTTP requests

    %% Relationships - API Gateway to Services
    ApiGateway --> CustomerController : routes to
    ApiGateway --> JobCategoryController : routes to
    ApiGateway --> CustomerContractController : routes to

    %% Relationships - Customer Service
    CustomerController --> CustomerService : uses
    CustomerServiceImpl ..|> CustomerService : implements
    CustomerServiceImpl --> CustomerRepository : uses
    CustomerRepository --> Customer : manages

    %% Relationships - Job Service
    JobCategoryController --> JobCategoryService : uses
    JobCategoryServiceImpl ..|> JobCategoryService : implements
    JobCategoryServiceImpl --> JobCategoryRepository : uses
    JobCategoryRepository --> JobCategory : manages

    %% Relationships - Customer Contract Service Controllers
    CustomerContractController --> CustomerContractService : uses
    JobDetailController --> JobDetailService : uses
    WorkShiftController --> WorkShiftService : uses

    %% Relationships - Customer Contract Service Services
    CustomerContractServiceImpl ..|> CustomerContractService : implements
    JobDetailServiceImpl ..|> JobDetailService : implements
    WorkShiftServiceImpl ..|> WorkShiftService : implements

    %% Relationships - Service to Repository
    CustomerContractServiceImpl --> CustomerContractRepository : uses
    CustomerContractServiceImpl --> CustomerClient : uses
    CustomerContractServiceImpl --> JobCategoryClient : uses
    JobDetailServiceImpl --> JobDetailRepository : uses
    JobDetailServiceImpl --> CustomerContractRepository : uses
    JobDetailServiceImpl --> JobCategoryClient : uses
    WorkShiftServiceImpl --> WorkShiftRepository : uses
    WorkShiftServiceImpl --> JobDetailRepository : uses

    %% Relationships - Repository to Entity
    CustomerContractRepository --> CustomerContract : manages
    JobDetailRepository --> JobDetail : manages
    WorkShiftRepository --> WorkShift : manages

    %% Relationships - Entity Associations
    CustomerContract ||--o{ JobDetail : contains
    JobDetail ||--o{ WorkShift : contains
    CustomerContract }o--|| Customer : belongs to
    JobDetail }o--|| JobCategory : belongs to

    %% Relationships - Feign Clients
    CustomerClient --> CustomerController : calls via HTTP
    JobCategoryClient --> JobCategoryController : calls via HTTP
```

## Mô tả chi tiết các thành phần

### 1. Frontend (React + PrimeReact)
- **CustomerContractList**: Component hiển thị danh sách hợp đồng với khách hàng
- **ContractForm**: Form tạo/chỉnh sửa hợp đồng với workflow: chọn khách hàng → chọn loại công việc → định nghĩa ca làm việc → chọn ngày làm việc
- **ContractDetail**: Component hiển thị chi tiết hợp đồng với bảng lịch làm việc và thông tin thanh toán
- **CustomerSelectionDialog**: Dialog tìm kiếm và chọn khách hàng

### 2. API Gateway
- **ApiGateway**: Điều hướng các request từ frontend đến các microservice tương ứng
- Chạy trên port 8080 và là điểm vào duy nhất cho frontend

### 3. Customer Service (Port 8081)
- **Customer**: Entity chứa thông tin khách hàng (tên, công ty, số điện thoại, email, địa chỉ)
- **CustomerController**: REST API endpoints cho quản lý khách hàng
- **CustomerService/Impl**: Business logic cho khách hàng
- **CustomerRepository**: Data access layer cho Customer entity

### 4. Job Service (Port 8082)
- **JobCategory**: Entity chứa thông tin loại công việc (tên, mô tả)
- **JobCategoryController**: REST API endpoints cho quản lý loại công việc
- **JobCategoryService/Impl**: Business logic cho loại công việc
- **JobCategoryRepository**: Data access layer cho JobCategory entity

### 5. Customer Contract Service (Port 8083)

#### Entities
- **CustomerContract**: Entity chính chứa thông tin hợp đồng
  - Quan hệ 1-n với JobDetail
  - Chứa thông tin: ngày bắt đầu/kết thúc, tổng tiền, địa chỉ, mô tả, trạng thái
- **JobDetail**: Entity chứa chi tiết công việc trong hợp đồng
  - Quan hệ n-1 với CustomerContract
  - Quan hệ 1-n với WorkShift
  - Chứa thông tin: ngày bắt đầu/kết thúc, địa điểm làm việc, jobCategoryId
- **WorkShift**: Entity chứa thông tin ca làm việc
  - Quan hệ n-1 với JobDetail
  - Chứa thông tin: giờ bắt đầu/kết thúc, số lượng công nhân, lương, ngày làm việc

#### Controllers
- **CustomerContractController**: REST API cho quản lý hợp đồng
- **JobDetailController**: REST API cho quản lý chi tiết công việc
- **WorkShiftController**: REST API cho quản lý ca làm việc

#### Services
- **CustomerContractService/Impl**: Business logic cho hợp đồng
  - Validation hợp đồng
  - Tính toán tổng tiền tự động
  - Tính toán ngày làm việc
- **JobDetailService/Impl**: Business logic cho chi tiết công việc
- **WorkShiftService/Impl**: Business logic cho ca làm việc

#### Repositories
- **CustomerContractRepository**: Data access cho CustomerContract
- **JobDetailRepository**: Data access cho JobDetail
- **WorkShiftRepository**: Data access cho WorkShift

#### External Service Clients
- **CustomerClient**: Feign client gọi đến customer-service
- **JobCategoryClient**: Feign client gọi đến job-service

## Luồng hoạt động chính

### 1. Tạo hợp đồng mới
1. Frontend gọi API Gateway
2. API Gateway chuyển tiếp đến CustomerContractController
3. Controller gọi CustomerContractService
4. Service validate thông tin qua CustomerClient và JobCategoryClient
5. Service tính toán tổng tiền và lưu vào database
6. Trả về thông tin hợp đồng đã tạo

### 2. Hiển thị chi tiết hợp đồng
1. Frontend request thông tin hợp đồng
2. Service lấy thông tin hợp đồng cùng với JobDetail và WorkShift
3. Tính toán và hiển thị lịch làm việc chi tiết
4. Hiển thị thông tin thanh toán và trạng thái

### 3. Tính toán tự động
- **Tổng tiền hợp đồng**: Tự động tính dựa trên (lương × số công nhân × số ngày làm việc) của tất cả ca làm việc
- **Ngày làm việc**: Tự động tính dựa trên khoảng thời gian hợp đồng và các ngày trong tuần được chọn
- **Trạng thái hợp đồng**: 0-Chờ duyệt, 1-Đang hoạt động, 2-Hoàn thành, 3-Đã hủy

## Đặc điểm kỹ thuật

### 1. Kiến trúc Microservices
- Mỗi service độc lập với database riêng
- Giao tiếp qua HTTP/REST API
- Sử dụng Feign Client cho inter-service communication

### 2. Entity Relationships
- Sử dụng JPA annotations (@OneToMany, @ManyToOne)
- JSON serialization với @JsonManagedReference/@JsonBackReference
- Cascade operations và orphan removal

### 3. Data Validation
- Validation ở service layer
- Kiểm tra tồn tại của Customer và JobCategory qua external services
- Soft delete pattern (isDeleted flag)

### 4. Frontend Integration
- React với TypeScript
- PrimeReact UI components
- Vietnamese date/currency formatting
- Real-time calculation và validation
