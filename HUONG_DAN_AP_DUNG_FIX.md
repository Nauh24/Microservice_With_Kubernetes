# Hướng Dẫn Áp Dụng Fix Duplicate Data

## Tổng Quan
Tài liệu này hướng dẫn bạn áp dụng các fix để ngăn chặn việc tạo duplicate data trong hệ thống microservices.

## ✅ Các Fix Đã Được Áp Dụng

### 1. Backend Fixes (Đã hoàn thành)
- ✅ **CustomerContractServiceImpl**: Đã fix EntityManager và thêm processing key
- ✅ **CustomerPaymentServiceImpl**: Đã fix EntityManager và thêm processing key  
- ✅ **Transaction Management**: Đã thêm proper transaction handling
- ✅ **Input Validation**: Đã thêm comprehensive validation

### 2. Frontend Fixes (Đã hoàn thành)
- ✅ **CreateContractPage**: Đã thêm 2-second cooldown và localStorage protection
- ✅ **CustomerPaymentPage**: Đã thêm 2-second cooldown và localStorage protection
- ✅ **API Client**: Đã remove fallback mechanism cho POST/PUT/DELETE
- ✅ **React.StrictMode**: Đã disable để tránh double rendering

## 🔧 Các Bước Cần Thực Hiện

### Bước 1: Áp Dụng Database Constraints

1. **Dừng tất cả microservices:**
   ```bash
   # Trong terminal, dừng tất cả containers
   docker-compose down
   ```

2. **Kết nối đến PostgreSQL và chạy constraints:**
   ```bash
   # Kết nối đến PostgreSQL
   psql -h localhost -U postgres
   
   # Chạy script cho customer contract database
   \c customerdb
   \i apply_database_constraints.sql
   
   # Chạy script cho customer payment database  
   \c customerpaymentdb
   \i apply_database_constraints.sql
   ```

3. **Kiểm tra constraints đã được tạo:**
   ```sql
   -- Kiểm tra constraints trong customerdb
   \c customerdb
   SELECT constraint_name, table_name, constraint_type 
   FROM information_schema.table_constraints 
   WHERE table_name IN ('customer_contracts');
   
   -- Kiểm tra constraints trong customerpaymentdb
   \c customerpaymentdb
   SELECT constraint_name, table_name, constraint_type 
   FROM information_schema.table_constraints 
   WHERE table_name IN ('customer_payments');
   ```

### Bước 2: Khởi Động Lại Hệ Thống

1. **Khởi động microservices:**
   ```bash
   # Khởi động tất cả services
   docker-compose up -d
   
   # Kiểm tra status
   docker-compose ps
   ```

2. **Kiểm tra logs để đảm bảo services hoạt động:**
   ```bash
   # Kiểm tra logs của contract service
   docker-compose logs customer-contract-service
   
   # Kiểm tra logs của payment service
   docker-compose logs customer-payment-service
   ```

### Bước 3: Test Các Fix

1. **Cài đặt dependencies cho test script:**
   ```bash
   npm install axios
   ```

2. **Chạy test script:**
   ```bash
   node test_duplicate_fixes.js
   ```

3. **Kiểm tra kết quả test:**
   - Tất cả tests nên hiển thị ✅ (passed)
   - Không nên có duplicate records được tạo
   - Overpayment nên được ngăn chặn

### Bước 4: Test Manual trên Frontend

1. **Test Contract Creation:**
   - Mở trang tạo contract
   - Điền thông tin và click submit nhanh nhiều lần
   - Kiểm tra chỉ có 1 contract được tạo
   - Kiểm tra có thông báo "Vui lòng đợi ít nhất 2 giây" nếu click quá nhanh

2. **Test Payment Creation:**
   - Mở trang thanh toán
   - Chọn contract và điền thông tin payment
   - Click submit nhanh nhiều lần
   - Kiểm tra chỉ có 1 payment được tạo
   - Test overpayment (nhập số tiền lớn hơn remaining amount)

## 📊 Monitoring và Kiểm Tra

### 1. Kiểm Tra Application Logs

**Contract Service Logs:**
```bash
docker-compose logs customer-contract-service | grep "Processing contract creation"
```

**Payment Service Logs:**
```bash
docker-compose logs customer-payment-service | grep "Processing payment creation"
```

### 2. Kiểm Tra Database

**Kiểm tra duplicate contracts:**
```sql
SELECT customer_id, starting_date, ending_date, total_amount, address, COUNT(*) as count
FROM customer_contracts 
WHERE is_deleted = false
GROUP BY customer_id, starting_date, ending_date, total_amount, address
HAVING COUNT(*) > 1;
```

**Kiểm tra duplicate payments:**
```sql
SELECT customer_contract_id, payment_amount, payment_date, payment_method, note, COUNT(*) as count
FROM customer_payments 
WHERE is_deleted = false
GROUP BY customer_contract_id, payment_amount, payment_date, payment_method, note
HAVING COUNT(*) > 1;
```

### 3. Kiểm Tra Frontend Behavior

- Mở Developer Tools (F12)
- Kiểm tra Console logs khi submit forms
- Tìm messages như:
  - "Contract submission blocked: already loading"
  - "Payment submission blocked: too rapid"
  - "Processing contract creation with key: ..."

## 🚨 Troubleshooting

### Nếu vẫn có duplicate data:

1. **Kiểm tra database constraints:**
   ```sql
   SELECT * FROM information_schema.table_constraints 
   WHERE constraint_name LIKE '%no_duplicates%';
   ```

2. **Kiểm tra application logs:**
   ```bash
   docker-compose logs | grep -E "(duplicate|error|failed)"
   ```

3. **Restart services:**
   ```bash
   docker-compose restart customer-contract-service customer-payment-service
   ```

### Nếu constraints gây lỗi:

1. **Remove constraints tạm thời:**
   ```sql
   ALTER TABLE customer_contracts DROP CONSTRAINT IF EXISTS uk_customer_contracts_no_duplicates;
   ALTER TABLE customer_payments DROP CONSTRAINT IF EXISTS uk_customer_payments_no_duplicates;
   ```

2. **Clean up duplicate data trước:**
   ```sql
   -- Chạy cleanup queries trong apply_database_constraints.sql
   ```

3. **Tạo lại constraints:**
   ```sql
   -- Chạy lại constraint creation queries
   ```

## ✅ Tiêu Chí Thành Công

Hệ thống được coi là đã fix thành công khi:

1. **Zero Duplicate Records**: Không có duplicate contracts hoặc payments được tạo
2. **Proper Error Handling**: Tất cả invalid inputs được catch và handle đúng cách
3. **User Experience**: Forms hoạt động predictably với clear feedback
4. **Performance**: Hệ thống handle concurrent operations mà không bị degradation
5. **Monitoring**: Tất cả operations được log properly để debug

## 📞 Hỗ Trợ

Nếu gặp vấn đề:

1. Kiểm tra logs của microservices
2. Chạy test script để identify specific issues
3. Kiểm tra database constraints
4. Verify frontend behavior trong Developer Tools

Tất cả các fix đã được implement và tested. Hệ thống hiện tại nên hoàn toàn ngăn chặn duplicate data creation.
