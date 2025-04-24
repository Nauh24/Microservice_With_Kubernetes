# Biểu đồ tuần tự hoạt động chi tiết cho module "Ký hợp đồng với khách thuê lao động"

## 1. Tổng quan

Biểu đồ tuần tự mô tả chi tiết các luồng hoạt động chính trong module "Ký hợp đồng với khách thuê lao động", bao gồm:
1. Tạo hợp đồng mới
2. Ký hợp đồng
3. Kích hoạt hợp đồng
4. Hoàn thành hợp đồng
5. Hủy hợp đồng

## 2. Biểu đồ tuần tự

### 2.1. Tạo hợp đồng mới

```mermaid
sequenceDiagram
    actor Client
    participant ContractController as CustomerContractController
    participant ContractService as CustomerContractServiceImpl
    participant ContractRepo as CustomerContractRepository
    participant CustomerClient
    participant JobCategoryClient
    participant CustomerService as customer-service
    participant JobService as job-service
    
    Client->>ContractController: POST /api/customer-contract (CustomerContract)
    activate ContractController
    ContractController->>ContractService: createContract(contract)
    activate ContractService
    
    Note over ContractService: Kiểm tra tính hợp lệ của dữ liệu
    
    ContractService->>CustomerClient: checkCustomerExists(customerId)
    activate CustomerClient
    CustomerClient->>CustomerService: GET /api/customer/{id}/check-customer-exists
    activate CustomerService
    CustomerService-->>CustomerClient: Boolean (true/false)
    deactivate CustomerService
    CustomerClient-->>ContractService: Boolean (true/false)
    deactivate CustomerClient
    
    alt Khách hàng không tồn tại
        ContractService-->>ContractController: throw AppException(CustomerNotFound_Exception)
        ContractController-->>Client: HTTP 404 (Không tìm thấy thông tin khách hàng)
    else Khách hàng tồn tại
        ContractService->>JobCategoryClient: checkJobCategoryExists(jobCategoryId)
        activate JobCategoryClient
        JobCategoryClient->>JobService: GET /api/job-category/{id}/check-job-category-exists
        activate JobService
        JobService-->>JobCategoryClient: Boolean (true/false)
        deactivate JobService
        JobCategoryClient-->>ContractService: Boolean (true/false)
        deactivate JobCategoryClient
        
        alt Loại công việc không tồn tại
            ContractService-->>ContractController: throw AppException(JobCategoryNotFound_Exception)
            ContractController-->>Client: HTTP 404 (Không tìm thấy thông tin loại công việc)
        else Loại công việc tồn tại
            Note over ContractService: Kiểm tra ngày bắt đầu và kết thúc
            
            alt Ngày không hợp lệ
                ContractService-->>ContractController: throw AppException(InvalidDate_Exception)
                ContractController-->>Client: HTTP 400 (Ngày không hợp lệ)
            else Ngày hợp lệ
                Note over ContractService: Thiết lập giá trị mặc định
                ContractService->>ContractService: contract.setCreatedAt(LocalDateTime.now())
                ContractService->>ContractService: contract.setUpdatedAt(LocalDateTime.now())
                ContractService->>ContractService: contract.setIsDeleted(false)
                ContractService->>ContractService: contract.setStatus(PENDING)
                
                ContractService->>ContractRepo: save(contract)
                activate ContractRepo
                ContractRepo-->>ContractService: savedContract
                deactivate ContractRepo
                
                Note over ContractService: Tạo mã hợp đồng
                ContractService->>ContractService: savedContract.setContractCode("CC" + savedContract.getId())
                
                ContractService->>ContractRepo: save(savedContract)
                activate ContractRepo
                ContractRepo-->>ContractService: finalContract
                deactivate ContractRepo
                
                ContractService-->>ContractController: finalContract
                ContractController-->>Client: HTTP 200 (CustomerContract)
            end
        end
    end
    
    deactivate ContractService
    deactivate ContractController
```

### 2.2. Ký hợp đồng

```mermaid
sequenceDiagram
    actor Client
    participant ContractController as CustomerContractController
    participant ContractService as CustomerContractServiceImpl
    participant ContractRepo as CustomerContractRepository
    
    Client->>ContractController: PUT /api/customer-contract/{id}/sign (signedDate)
    activate ContractController
    ContractController->>ContractService: signContract(id, signedDate)
    activate ContractService
    
    ContractService->>ContractRepo: findByIdAndIsDeletedFalse(id)
    activate ContractRepo
    ContractRepo-->>ContractService: Optional<CustomerContract>
    deactivate ContractRepo
    
    alt Hợp đồng không tồn tại
        ContractService-->>ContractController: throw AppException(NotFound_Exception)
        ContractController-->>Client: HTTP 404 (Không tìm thấy thông tin hợp đồng)
    else Hợp đồng tồn tại
        Note over ContractService: Kiểm tra trạng thái hợp đồng
        
        alt Trạng thái không phải PENDING
            ContractService-->>ContractController: throw AppException(NotAllowUpdate_Exception)
            ContractController-->>Client: HTTP 400 (Chỉ có thể ký hợp đồng đang ở trạng thái chờ xử lý)
        else Trạng thái là PENDING
            Note over ContractService: Kiểm tra ngày ký
            
            alt Ngày ký trước ngày hiện tại
                ContractService-->>ContractController: throw AppException(InvalidDate_Exception)
                ContractController-->>Client: HTTP 400 (Ngày ký không được trước ngày hiện tại)
            else Ngày ký hợp lệ
                ContractService->>ContractService: contract.setSignedDate(signedDate)
                ContractService->>ContractService: contract.setUpdatedAt(LocalDateTime.now())
                
                ContractService->>ContractRepo: save(contract)
                activate ContractRepo
                ContractRepo-->>ContractService: updatedContract
                deactivate ContractRepo
                
                ContractService-->>ContractController: updatedContract
                ContractController-->>Client: HTTP 200 (CustomerContract)
            end
        end
    end
    
    deactivate ContractService
    deactivate ContractController
```

### 2.3. Kích hoạt hợp đồng

```mermaid
sequenceDiagram
    actor Client
    participant ContractController as CustomerContractController
    participant ContractService as CustomerContractServiceImpl
    participant ContractRepo as CustomerContractRepository
    participant Constants as ContractStatusConstants
    
    Client->>ContractController: PUT /api/customer-contract/{id}/status/1
    activate ContractController
    ContractController->>ContractService: updateContractStatus(id, ACTIVE)
    activate ContractService
    
    ContractService->>ContractRepo: findByIdAndIsDeletedFalse(id)
    activate ContractRepo
    ContractRepo-->>ContractService: Optional<CustomerContract>
    deactivate ContractRepo
    
    alt Hợp đồng không tồn tại
        ContractService-->>ContractController: throw AppException(NotFound_Exception)
        ContractController-->>Client: HTTP 404 (Không tìm thấy thông tin hợp đồng)
    else Hợp đồng tồn tại
        Note over ContractService: Kiểm tra logic chuyển trạng thái
        
        alt Trạng thái hiện tại không phải PENDING
            ContractService-->>ContractController: throw AppException(NotAllowUpdate_Exception)
            ContractController-->>Client: HTTP 400 (Chỉ có thể kích hoạt hợp đồng đang ở trạng thái chờ xử lý)
        else Trạng thái hiện tại là PENDING
            alt Hợp đồng chưa được ký
                ContractService-->>ContractController: throw AppException(NotAllowUpdate_Exception)
                ContractController-->>Client: HTTP 400 (Hợp đồng chưa được ký, không thể kích hoạt)
            else Hợp đồng đã được ký
                ContractService->>ContractService: contract.setStatus(ACTIVE)
                ContractService->>ContractService: contract.setUpdatedAt(LocalDateTime.now())
                
                ContractService->>ContractRepo: save(contract)
                activate ContractRepo
                ContractRepo-->>ContractService: updatedContract
                deactivate ContractRepo
                
                ContractService-->>ContractController: updatedContract
                ContractController-->>Client: HTTP 200 (CustomerContract)
            end
        end
    end
    
    deactivate ContractService
    deactivate ContractController
```

### 2.4. Hoàn thành hợp đồng

```mermaid
sequenceDiagram
    actor Client
    participant ContractController as CustomerContractController
    participant ContractService as CustomerContractServiceImpl
    participant ContractRepo as CustomerContractRepository
    participant Constants as ContractStatusConstants
    
    Client->>ContractController: PUT /api/customer-contract/{id}/status/2
    activate ContractController
    ContractController->>ContractService: updateContractStatus(id, COMPLETED)
    activate ContractService
    
    ContractService->>ContractRepo: findByIdAndIsDeletedFalse(id)
    activate ContractRepo
    ContractRepo-->>ContractService: Optional<CustomerContract>
    deactivate ContractRepo
    
    alt Hợp đồng không tồn tại
        ContractService-->>ContractController: throw AppException(NotFound_Exception)
        ContractController-->>Client: HTTP 404 (Không tìm thấy thông tin hợp đồng)
    else Hợp đồng tồn tại
        Note over ContractService: Kiểm tra logic chuyển trạng thái
        
        alt Trạng thái hiện tại không phải ACTIVE
            ContractService-->>ContractController: throw AppException(NotAllowUpdate_Exception)
            ContractController-->>Client: HTTP 400 (Chỉ có thể hoàn thành hợp đồng đang hoạt động)
        else Trạng thái hiện tại là ACTIVE
            ContractService->>ContractService: contract.setStatus(COMPLETED)
            ContractService->>ContractService: contract.setUpdatedAt(LocalDateTime.now())
            
            ContractService->>ContractRepo: save(contract)
            activate ContractRepo
            ContractRepo-->>ContractService: updatedContract
            deactivate ContractRepo
            
            ContractService-->>ContractController: updatedContract
            ContractController-->>Client: HTTP 200 (CustomerContract)
        end
    end
    
    deactivate ContractService
    deactivate ContractController
```

### 2.5. Hủy hợp đồng

```mermaid
sequenceDiagram
    actor Client
    participant ContractController as CustomerContractController
    participant ContractService as CustomerContractServiceImpl
    participant ContractRepo as CustomerContractRepository
    participant Constants as ContractStatusConstants
    
    Client->>ContractController: PUT /api/customer-contract/{id}/status/3
    activate ContractController
    ContractController->>ContractService: updateContractStatus(id, CANCELLED)
    activate ContractService
    
    ContractService->>ContractRepo: findByIdAndIsDeletedFalse(id)
    activate ContractRepo
    ContractRepo-->>ContractService: Optional<CustomerContract>
    deactivate ContractRepo
    
    alt Hợp đồng không tồn tại
        ContractService-->>ContractController: throw AppException(NotFound_Exception)
        ContractController-->>Client: HTTP 404 (Không tìm thấy thông tin hợp đồng)
    else Hợp đồng tồn tại
        Note over ContractService: Kiểm tra logic chuyển trạng thái
        
        alt Trạng thái hiện tại không phải PENDING hoặc ACTIVE
            ContractService-->>ContractController: throw AppException(NotAllowUpdate_Exception)
            ContractController-->>Client: HTTP 400 (Chỉ có thể hủy hợp đồng đang chờ xử lý hoặc đang hoạt động)
        else Trạng thái hiện tại là PENDING hoặc ACTIVE
            ContractService->>ContractService: contract.setStatus(CANCELLED)
            ContractService->>ContractService: contract.setUpdatedAt(LocalDateTime.now())
            
            ContractService->>ContractRepo: save(contract)
            activate ContractRepo
            ContractRepo-->>ContractService: updatedContract
            deactivate ContractRepo
            
            ContractService-->>ContractController: updatedContract
            ContractController-->>Client: HTTP 200 (CustomerContract)
        end
    end
    
    deactivate ContractService
    deactivate ContractController
```

## 3. Mô tả chi tiết các luồng hoạt động

### 3.1. Tạo hợp đồng mới

1. **Khởi tạo**:
   - Client gửi request POST đến `/api/customer-contract` với thông tin hợp đồng
   - CustomerContractController nhận request và gọi phương thức `createContract()` từ CustomerContractServiceImpl

2. **Kiểm tra tính hợp lệ**:
   - Kiểm tra customerId tồn tại thông qua CustomerClient
   - Kiểm tra jobCategoryId tồn tại thông qua JobCategoryClient
   - Kiểm tra ngày bắt đầu và kết thúc

3. **Xử lý dữ liệu**:
   - Thiết lập các giá trị mặc định: createdAt, updatedAt, isDeleted
   - Thiết lập trạng thái mặc định là PENDING
   - Lưu hợp đồng vào cơ sở dữ liệu
   - Tạo mã hợp đồng và lưu lại

4. **Kết quả**:
   - Trả về hợp đồng đã tạo cho Client

### 3.2. Ký hợp đồng

1. **Khởi tạo**:
   - Client gửi request PUT đến `/api/customer-contract/{id}/sign` với ngày ký
   - CustomerContractController nhận request và gọi phương thức `signContract()` từ CustomerContractServiceImpl

2. **Kiểm tra tính hợp lệ**:
   - Kiểm tra hợp đồng tồn tại
   - Kiểm tra trạng thái hợp đồng (phải là PENDING)
   - Kiểm tra ngày ký (không được trước ngày hiện tại)

3. **Xử lý dữ liệu**:
   - Cập nhật ngày ký và thời gian cập nhật
   - Lưu hợp đồng vào cơ sở dữ liệu

4. **Kết quả**:
   - Trả về hợp đồng đã cập nhật cho Client

### 3.3. Kích hoạt hợp đồng

1. **Khởi tạo**:
   - Client gửi request PUT đến `/api/customer-contract/{id}/status/1`
   - CustomerContractController nhận request và gọi phương thức `updateContractStatus()` từ CustomerContractServiceImpl

2. **Kiểm tra tính hợp lệ**:
   - Kiểm tra hợp đồng tồn tại
   - Kiểm tra trạng thái hiện tại (phải là PENDING)
   - Kiểm tra hợp đồng đã được ký chưa

3. **Xử lý dữ liệu**:
   - Cập nhật trạng thái thành ACTIVE và thời gian cập nhật
   - Lưu hợp đồng vào cơ sở dữ liệu

4. **Kết quả**:
   - Trả về hợp đồng đã cập nhật cho Client

### 3.4. Hoàn thành hợp đồng

1. **Khởi tạo**:
   - Client gửi request PUT đến `/api/customer-contract/{id}/status/2`
   - CustomerContractController nhận request và gọi phương thức `updateContractStatus()` từ CustomerContractServiceImpl

2. **Kiểm tra tính hợp lệ**:
   - Kiểm tra hợp đồng tồn tại
   - Kiểm tra trạng thái hiện tại (phải là ACTIVE)

3. **Xử lý dữ liệu**:
   - Cập nhật trạng thái thành COMPLETED và thời gian cập nhật
   - Lưu hợp đồng vào cơ sở dữ liệu

4. **Kết quả**:
   - Trả về hợp đồng đã cập nhật cho Client

### 3.5. Hủy hợp đồng

1. **Khởi tạo**:
   - Client gửi request PUT đến `/api/customer-contract/{id}/status/3`
   - CustomerContractController nhận request và gọi phương thức `updateContractStatus()` từ CustomerContractServiceImpl

2. **Kiểm tra tính hợp lệ**:
   - Kiểm tra hợp đồng tồn tại
   - Kiểm tra trạng thái hiện tại (phải là PENDING hoặc ACTIVE)

3. **Xử lý dữ liệu**:
   - Cập nhật trạng thái thành CANCELLED và thời gian cập nhật
   - Lưu hợp đồng vào cơ sở dữ liệu

4. **Kết quả**:
   - Trả về hợp đồng đã cập nhật cho Client

## 4. Kết luận

Biểu đồ tuần tự chi tiết cho module "Ký hợp đồng với khách thuê lao động" đã mô tả đầy đủ các luồng hoạt động chính, bao gồm tạo hợp đồng mới, ký hợp đồng, kích hoạt hợp đồng, hoàn thành hợp đồng và hủy hợp đồng. Mỗi luồng hoạt động đều được mô tả chi tiết từ khi Client gửi request đến khi nhận được response, bao gồm các bước kiểm tra tính hợp lệ, xử lý dữ liệu và lưu trữ.

Biểu đồ tuần tự này giúp hiểu rõ hơn về cách các đối tượng tương tác với nhau theo thời gian, cũng như các điều kiện và ràng buộc trong quá trình xử lý nghiệp vụ. Điều này rất hữu ích cho việc phát triển, kiểm thử và bảo trì hệ thống.
