# Sơ đồ tuần tự cho module "Ký hợp đồng với khách thuê lao động"

Sơ đồ tuần tự dưới đây mô tả chi tiết luồng hoạt động của module "Ký hợp đồng với khách thuê lao động" theo các bước:

1. Nhân viên chọn chức năng "Ký hợp đồng với khách thuê lao động"
2. Giao diện danh sách các hợp đồng đã ký hiện lên, mặc định sắp xếp theo thứ tự thời gian mới nhất đến cũ nhất
3. Nhân viên click nút "Thêm hợp đồng" để tạo hợp đồng mới
4. Giao diện tìm khách hàng hiện lên
5. Nhân viên nhập tên khách hàng hoặc một phần tên khách hàng và click tìm
6. Giao diện hiện lên danh sách các khách hàng có tên chứa từ khóa vừa nhập
7. Nhân viên chọn đúng khách hàng
8. Nhân viên chọn đầu việc có trong danh sách, nhập thông tin hợp đồng: thời gian thuê (từ ngày - đến ngày), số lượng nhân công cần thuê,...
9. Nhân viên xác nhận lại toàn bộ thông tin với khách hàng và click lưu
10. Hệ thống lưu lại hợp đồng, báo thành công và đưa hợp đồng vào danh sách hợp đồng đã ký

```mermaid
sequenceDiagram
    actor Staff as Nhân viên
    participant ContractList as CustomerContractList
    participant SelectionDialog as CustomerSelectionDialog
    participant ContractForm as ContractForm
    participant ContractController as CustomerContractController
    participant ContractService as CustomerContractServiceImpl
    participant CustomerClient as CustomerClient
    participant JobClient as JobCategoryClient
    participant CustomerController as CustomerController
    participant CustomerService as CustomerServiceImpl
    participant JobController as JobCategoryController
    participant JobService as JobCategoryServiceImpl
    participant ContractRepo as CustomerContractRepository
    participant CustomerRepo as CustomerRepository
    participant JobRepo as JobCategoryRepository
    
    %% 1. Nhân viên chọn chức năng "Ký hợp đồng với khách thuê lao động"
    Staff->>ContractList: Chọn chức năng "Ký hợp đồng với khách thuê lao động"
    activate ContractList
    
    %% 2. Giao diện danh sách các hợp đồng đã ký hiện lên
    ContractList->>ContractController: getAllContracts()
    activate ContractController
    
    ContractController->>ContractService: getAllContracts()
    activate ContractService
    
    ContractService->>ContractRepo: findByIsDeletedFalse()
    activate ContractRepo
    ContractRepo-->>ContractService: Danh sách hợp đồng
    deactivate ContractRepo
    
    ContractService-->>ContractController: Danh sách hợp đồng
    deactivate ContractService
    
    ContractController-->>ContractList: ResponseEntity<List<CustomerContract>>
    deactivate ContractController
    
    ContractList->>ContractList: Sắp xếp hợp đồng theo thời gian mới nhất
    
    %% 3. Nhân viên click nút "Thêm hợp đồng" để tạo hợp đồng mới
    Staff->>ContractList: Click nút "Thêm hợp đồng"
    
    %% 4. Giao diện tìm khách hàng hiện lên
    ContractList->>SelectionDialog: Mở dialog tìm kiếm khách hàng
    activate SelectionDialog
    
    %% 5. Nhân viên nhập tên khách hàng và click tìm
    Staff->>SelectionDialog: Nhập tên khách hàng và click tìm
    
    %% 6. Giao diện hiện lên danh sách các khách hàng có tên chứa từ khóa vừa nhập
    SelectionDialog->>CustomerController: searchCustomers(fullName, null)
    activate CustomerController
    
    CustomerController->>CustomerService: searchCustomers(fullName, null)
    activate CustomerService
    
    CustomerService->>CustomerRepo: findByFullNameContainingAndIsDeletedFalse(fullName)
    activate CustomerRepo
    CustomerRepo-->>CustomerService: Danh sách khách hàng phù hợp
    deactivate CustomerRepo
    
    CustomerService-->>CustomerController: Danh sách khách hàng
    deactivate CustomerService
    
    CustomerController-->>SelectionDialog: ResponseEntity<List<Customer>>
    deactivate CustomerController
    
    SelectionDialog->>SelectionDialog: Hiển thị danh sách khách hàng
    
    %% 7. Nhân viên chọn đúng khách hàng
    Staff->>SelectionDialog: Chọn khách hàng
    
    SelectionDialog-->>ContractList: handleSelectCustomer(customer)
    deactivate SelectionDialog
    
    ContractList->>ContractList: setSelectedCustomer(customer)
    
    %% 8. Nhân viên chọn đầu việc và nhập thông tin hợp đồng
    ContractList->>ContractForm: Mở form thêm hợp đồng
    activate ContractForm
    
    %% Lấy danh sách loại công việc (đầu việc)
    ContractForm->>JobController: getAllJobCategories()
    activate JobController
    
    JobController->>JobService: getAllJobCategories()
    activate JobService
    
    JobService->>JobRepo: findByIsDeletedFalse()
    activate JobRepo
    JobRepo-->>JobService: Danh sách loại công việc
    deactivate JobRepo
    
    JobService-->>JobController: Danh sách loại công việc
    deactivate JobService
    
    JobController-->>ContractForm: ResponseEntity<List<JobCategory>>
    deactivate JobController
    
    %% Nhân viên nhập thông tin hợp đồng
    Staff->>ContractForm: Chọn loại công việc
    Staff->>ContractForm: Nhập thời gian thuê (từ ngày - đến ngày)
    Staff->>ContractForm: Nhập số lượng nhân công cần thuê
    Staff->>ContractForm: Nhập địa điểm làm việc
    Staff->>ContractForm: Nhập tổng giá trị hợp đồng
    Staff->>ContractForm: Nhập mô tả công việc
    
    %% 9. Nhân viên xác nhận lại toàn bộ thông tin và click lưu
    Staff->>ContractForm: Xác nhận thông tin và click "Lưu hợp đồng"
    
    ContractForm->>ContractList: handleSave()
    deactivate ContractForm
    
    %% 10. Hệ thống lưu lại hợp đồng
    ContractList->>ContractController: createContract(contract)
    activate ContractController
    
    ContractController->>ContractService: createContract(contract)
    activate ContractService
    
    %% Kiểm tra thông tin khách hàng
    ContractService->>CustomerClient: checkCustomerExists(contract.customerId)
    activate CustomerClient
    
    CustomerClient->>CustomerController: checkCustomerExists(id)
    activate CustomerController
    
    CustomerController->>CustomerService: checkCustomerExists(id)
    activate CustomerService
    
    CustomerService->>CustomerRepo: findByIdAndIsDeletedFalse(id)
    activate CustomerRepo
    CustomerRepo-->>CustomerService: Optional<Customer>
    deactivate CustomerRepo
    
    CustomerService-->>CustomerController: boolean
    deactivate CustomerService
    
    CustomerController-->>CustomerClient: ResponseEntity<Boolean>
    deactivate CustomerController
    
    CustomerClient-->>ContractService: Boolean
    deactivate CustomerClient
    
    %% Kiểm tra thông tin loại công việc
    ContractService->>JobClient: checkJobCategoryExists(contract.jobCategoryId)
    activate JobClient
    
    JobClient->>JobController: checkJobCategoryExists(id)
    activate JobController
    
    JobController->>JobService: checkJobCategoryExists(id)
    activate JobService
    
    JobService->>JobRepo: findByIdAndIsDeletedFalse(id)
    activate JobRepo
    JobRepo-->>JobService: Optional<JobCategory>
    deactivate JobRepo
    
    JobService-->>JobController: boolean
    deactivate JobService
    
    JobController-->>JobClient: ResponseEntity<Boolean>
    deactivate JobController
    
    JobClient-->>ContractService: Boolean
    deactivate JobClient
    
    %% Lưu hợp đồng
    ContractService->>ContractService: Thiết lập các giá trị mặc định (status, createdAt, updatedAt, isDeleted)
    
    ContractService->>ContractRepo: save(contract)
    activate ContractRepo
    ContractRepo-->>ContractService: Hợp đồng đã lưu
    deactivate ContractRepo
    
    %% Tạo mã hợp đồng nếu chưa có
    ContractService->>ContractService: Tạo mã hợp đồng nếu chưa có
    
    ContractService->>ContractRepo: save(contract) (nếu cần cập nhật mã hợp đồng)
    activate ContractRepo
    ContractRepo-->>ContractService: Hợp đồng đã lưu
    deactivate ContractRepo
    
    ContractService-->>ContractController: Hợp đồng đã lưu
    deactivate ContractService
    
    ContractController-->>ContractList: ResponseEntity<CustomerContract>
    deactivate ContractController
    
    %% Cập nhật danh sách hợp đồng
    ContractList->>ContractController: getAllContracts()
    activate ContractController
    
    ContractController->>ContractService: getAllContracts()
    activate ContractService
    
    ContractService->>ContractRepo: findByIsDeletedFalse()
    activate ContractRepo
    ContractRepo-->>ContractService: Danh sách hợp đồng
    deactivate ContractRepo
    
    ContractService-->>ContractController: Danh sách hợp đồng
    deactivate ContractService
    
    ContractController-->>ContractList: ResponseEntity<List<CustomerContract>>
    deactivate ContractController
    
    ContractList->>ContractList: Sắp xếp hợp đồng theo thời gian mới nhất
    
    ContractList-->>Staff: Hiển thị thông báo thành công
    deactivate ContractList
```

## Giải thích chi tiết các bước trong sơ đồ

### 1. Mở danh sách hợp đồng
- Nhân viên chọn chức năng "Ký hợp đồng với khách thuê lao động"
- CustomerContractList gọi API getAllContracts() của CustomerContractController
- CustomerContractController gọi CustomerContractService để lấy danh sách hợp đồng
- CustomerContractService truy vấn dữ liệu từ CustomerContractRepository
- Danh sách hợp đồng được trả về và hiển thị, sắp xếp theo thời gian mới nhất

### 2. Tìm kiếm khách hàng
- Nhân viên click nút "Thêm hợp đồng" để mở CustomerSelectionDialog
- Nhân viên nhập tên khách hàng và tìm kiếm
- CustomerSelectionDialog gọi API searchCustomers() của CustomerController
- CustomerController gọi CustomerService để tìm kiếm khách hàng
- CustomerService truy vấn dữ liệu từ CustomerRepository
- Danh sách khách hàng phù hợp được trả về và hiển thị

### 3. Chọn khách hàng
- Nhân viên chọn khách hàng từ danh sách
- CustomerSelectionDialog gọi handleSelectCustomer() của CustomerContractList
- CustomerContractList lưu thông tin khách hàng đã chọn

### 4. Nhập thông tin hợp đồng
- ContractForm được mở để nhập thông tin hợp đồng
- ContractForm gọi API getAllJobCategories() của JobCategoryController để lấy danh sách loại công việc
- Nhân viên nhập các thông tin hợp đồng: loại công việc, thời gian thuê, số lượng nhân công, địa điểm làm việc, tổng giá trị, mô tả

### 5. Lưu hợp đồng
- Nhân viên xác nhận thông tin và click "Lưu hợp đồng"
- ContractForm gọi handleSave() của CustomerContractList
- CustomerContractList gọi API createContract() của CustomerContractController
- CustomerContractController gọi CustomerContractService để tạo hợp đồng mới
- CustomerContractService kiểm tra thông tin khách hàng và loại công việc thông qua CustomerClient và JobCategoryClient
- CustomerContractService thiết lập các giá trị mặc định và lưu hợp đồng vào CustomerContractRepository
- Hợp đồng mới được trả về và thêm vào danh sách
- Danh sách hợp đồng được cập nhật và hiển thị thông báo thành công

## Các thành phần tham gia

### Frontend
- **CustomerContractList**: Component hiển thị danh sách hợp đồng và xử lý các thao tác CRUD
- **CustomerSelectionDialog**: Dialog tìm kiếm và chọn khách hàng
- **ContractForm**: Form nhập thông tin hợp đồng

### Backend
- **CustomerContractController**: REST API controller xử lý các request liên quan đến hợp đồng
- **CustomerContractServiceImpl**: Triển khai logic nghiệp vụ cho hợp đồng
- **CustomerController**: REST API controller xử lý các request liên quan đến khách hàng
- **CustomerServiceImpl**: Triển khai logic nghiệp vụ cho khách hàng
- **JobCategoryController**: REST API controller xử lý các request liên quan đến loại công việc
- **JobCategoryServiceImpl**: Triển khai logic nghiệp vụ cho loại công việc
- **CustomerContractRepository**: Interface truy cập dữ liệu hợp đồng
- **CustomerRepository**: Interface truy cập dữ liệu khách hàng
- **JobCategoryRepository**: Interface truy cập dữ liệu loại công việc
- **CustomerClient**: Feign client gọi đến customer-service
- **JobCategoryClient**: Feign client gọi đến job-service
