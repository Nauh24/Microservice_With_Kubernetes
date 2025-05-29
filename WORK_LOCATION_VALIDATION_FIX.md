# Sửa Lỗi Yêu Cầu Nhập Địa Điểm Làm Việc

## Mô Tả Vấn Đề

Hệ thống đang yêu cầu bắt buộc phải nhập "địa điểm làm việc" (workLocation) trong chi tiết công việc khi tạo hợp đồng. Điều này gây khó khăn cho người dùng khi chưa xác định được địa điểm cụ thể.

## Nguyên Nhân

Lỗi này xuất phát từ hai nơi:

1. **Frontend**: Trường "Địa điểm làm việc" trong `JobDetailForm.tsx` có thuộc tính `required`
2. **Backend**: Validation trong `CustomerContractServiceImpl.java` yêu cầu workLocation không được để trống

## Giải Pháp Đã Áp Dụng

### 1. Sửa Frontend

**File**: `microservice_fe/src/components/contract/JobDetailForm.tsx`

**Thay đổi**:
- Loại bỏ thuộc tính `required` khỏi TextField
- Cập nhật placeholder để thông báo trường này không bắt buộc

```typescript
// Trước
<TextField
  required
  placeholder="Địa chỉ cụ thể nơi thực hiện công việc"
/>

// Sau  
<TextField
  placeholder="Địa chỉ cụ thể nơi thực hiện công việc (không bắt buộc)"
/>
```

### 2. Sửa Backend

**File**: `customer-contract-service/.../CustomerContractServiceImpl.java`

**Thay đổi**:
- Thay vì throw exception khi workLocation trống, hệ thống sẽ tự động gán giá trị mặc định
- Cập nhật logic auto-derive address để xử lý trường hợp workLocation có giá trị mặc định

```java
// Trước
if (jobDetail.getWorkLocation() == null || jobDetail.getWorkLocation().trim().isEmpty()) {
    throw new AppException(ErrorCode.InvalidInput_Exception, "Địa điểm làm việc không được để trống");
}

// Sau
if (jobDetail.getWorkLocation() == null || jobDetail.getWorkLocation().trim().isEmpty()) {
    jobDetail.setWorkLocation("Địa điểm sẽ được thông báo sau");
}
```

### 3. Cập Nhật Logic Auto-Derive Address

Cập nhật logic tự động tạo địa chỉ hợp đồng để xử lý trường hợp workLocation có giá trị mặc định:

```java
// Auto-derive address from job details if not provided
if (contract.getAddress() == null || contract.getAddress().trim().isEmpty()) {
    if (contract.getJobDetails() != null && !contract.getJobDetails().isEmpty()) {
        String firstWorkLocation = contract.getJobDetails().get(0).getWorkLocation();
        if (firstWorkLocation != null && !firstWorkLocation.trim().isEmpty() && 
            !firstWorkLocation.equals("Địa điểm sẽ được thông báo sau")) {
            contract.setAddress(firstWorkLocation);
        } else {
            contract.setAddress("Địa chỉ sẽ được thông báo sau");
        }
    }
}
```

## Files Đã Sửa

1. `microservice_fe/src/components/contract/JobDetailForm.tsx` - Loại bỏ validation required
2. `customer-contract-service/.../CustomerContractServiceImpl.java` - Thay đổi validation logic
3. `test_contract_without_address.ps1` - Cập nhật script test

## Cách Áp Dụng Sửa Lỗi

### Bước 1: Restart Customer Contract Service
```bash
# Nếu sử dụng Docker
docker-compose restart customer-contract-service

# Nếu chạy local
# Dừng và khởi động lại Spring Boot application
```

### Bước 2: Restart Frontend (nếu cần)
```bash
# Trong thư mục microservice_fe
npm start
# hoặc
yarn start
```

### Bước 3: Test Sửa Lỗi
```powershell
.\test_contract_without_address.ps1
```

## Kết Quả Mong Đợi

Sau khi áp dụng sửa lỗi:

1. ✅ Có thể tạo hợp đồng mà không cần nhập địa điểm làm việc
2. ✅ Trường địa điểm làm việc sẽ tự động được gán giá trị "Địa điểm sẽ được thông báo sau"
3. ✅ Địa chỉ hợp đồng sẽ tự động được gán giá trị "Địa chỉ sẽ được thông báo sau" nếu không có địa điểm cụ thể
4. ✅ Người dùng vẫn có thể nhập địa điểm cụ thể nếu muốn
5. ✅ Logic tính toán và validation khác vẫn hoạt động bình thường

## Lưu Ý

- Trường địa điểm làm việc vẫn hiển thị trong form nhưng không bắt buộc
- Hệ thống sẽ tự động gán giá trị mặc định khi trường này để trống
- Người dùng có thể cập nhật địa điểm sau khi tạo hợp đồng
- Sửa lỗi này không ảnh hưởng đến các hợp đồng đã tồn tại

## Kiểm Tra

Để kiểm tra sửa lỗi đã hoạt động:

1. Mở trang tạo hợp đồng mới
2. Chọn khách hàng
3. Thêm chi tiết công việc nhưng để trống trường "Địa điểm làm việc"
4. Thêm ca làm việc với đầy đủ thông tin
5. Nhấn "Tạo hợp đồng"
6. Hợp đồng sẽ được tạo thành công với địa điểm mặc định

## Hỗ Trợ

Nếu vẫn gặp vấn đề sau khi áp dụng sửa lỗi:

1. Kiểm tra log của customer-contract-service
2. Đảm bảo service đã được restart
3. Kiểm tra kết nối database
4. Chạy script test để xác định vấn đề cụ thể
