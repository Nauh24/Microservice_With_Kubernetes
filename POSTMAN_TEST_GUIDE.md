# Hướng dẫn Test Thanh toán Nhiều Hợp đồng với Postman

## 🚀 Service đã sẵn sàng!
- ✅ customer-payment-service đang chạy trên **http://localhost:8084**
- ✅ Bảng `contract_payments` đã được tạo tự động
- ✅ Tất cả lỗi backend đã được sửa

## 📋 Các bước test với Postman

### 1. **Health Check**
```
GET http://localhost:8084/actuator/health
```
**Expected Response:**
```json
{
  "status": "UP"
}
```

### 2. **Lấy danh sách hợp đồng active của khách hàng**
```
GET http://localhost:8084/api/payments/customer/1/active-contracts
```
**Mục đích:** Xem các hợp đồng có thể thanh toán và số tiền còn lại

### 3. **Test Thanh toán Nhiều Hợp đồng (CHÍNH)**
```
POST http://localhost:8084/api/payments/multiple-contracts
Content-Type: application/json

{
  "customerId": 1,
  "totalAmount": 50000000,
  "paymentMethod": 0,
  "note": "Thanh toán nhiều hợp đồng test",
  "contractPayments": [
    {
      "contractId": 7,
      "allocatedAmount": 15000000
    },
    {
      "contractId": 9,
      "allocatedAmount": 24000000
    },
    {
      "contractId": 10,
      "allocatedAmount": 11000000
    }
  ]
}
```

**⚠️ Lưu ý quan trọng:**
- `totalAmount` PHẢI BẰNG tổng của tất cả `allocatedAmount`
- `contractId` phải tồn tại trong database
- `allocatedAmount` không được vượt quá số tiền còn lại của hợp đồng
- `customerId` phải tồn tại

### 4. **Verify kết quả**
Sau khi tạo payment thành công, lấy `paymentId` từ response và chạy:
```
GET http://localhost:8084/api/payments/payment/{paymentId}/contract-payments
```

## 🧪 Test Cases

### ✅ **Success Case**
```json
{
  "customerId": 1,
  "totalAmount": 39000000,
  "paymentMethod": 0,
  "note": "Test thành công",
  "contractPayments": [
    {
      "contractId": 7,
      "allocatedAmount": 15000000
    },
    {
      "contractId": 9,
      "allocatedAmount": 24000000
    }
  ]
}
```

### ❌ **Error Cases để test**

#### 1. Tổng tiền không khớp
```json
{
  "customerId": 1,
  "totalAmount": 40000000,
  "paymentMethod": 0,
  "contractPayments": [
    {
      "contractId": 7,
      "allocatedAmount": 15000000
    },
    {
      "contractId": 9,
      "allocatedAmount": 24000000
    }
  ]
}
```
**Expected Error:** "Tổng số tiền phân bổ phải bằng tổng số tiền thanh toán"

#### 2. Contract ID không tồn tại
```json
{
  "customerId": 1,
  "totalAmount": 15000000,
  "paymentMethod": 0,
  "contractPayments": [
    {
      "contractId": 999,
      "allocatedAmount": 15000000
    }
  ]
}
```
**Expected Error:** "Không tìm thấy hợp đồng ID: 999"

#### 3. Số tiền vượt quá remaining amount
```json
{
  "customerId": 1,
  "totalAmount": 100000000,
  "paymentMethod": 0,
  "contractPayments": [
    {
      "contractId": 7,
      "allocatedAmount": 100000000
    }
  ]
}
```
**Expected Error:** "Số tiền thanh toán không được vượt quá số tiền còn lại"

## 📊 Kiểm tra Logs

### Backend Logs (Terminal đang chạy service)
Bạn sẽ thấy logs như:
```
🚀 Controller: Received multiple contracts payment request:
Customer ID: 1
Total Amount: 50000000.0
Payment Method: 0
Contract Payments Count: 3
✅ Controller: Payment created successfully with ID: 123
```

### Frontend Logs (Browser Console)
Nếu test từ frontend, sẽ thấy:
```
🚀 Payment service: Creating payment with multiple contracts...
✅ Payment service: Payment with multiple contracts created successfully
```

## 🔧 Troubleshooting

### Nếu gặp lỗi 500:
1. Kiểm tra backend logs để xem lỗi cụ thể
2. Đảm bảo database connection OK
3. Kiểm tra dữ liệu customers và contracts có tồn tại

### Nếu gặp lỗi 400:
1. Kiểm tra format JSON đúng chưa
2. Kiểm tra tổng tiền có khớp không
3. Kiểm tra contract IDs có tồn tại không

### Nếu không connect được:
1. Đảm bảo service đang chạy trên port 8084
2. Kiểm tra firewall/antivirus
3. Thử với `127.0.0.1:8084` thay vì `localhost:8084`

## 🎯 Kết quả mong đợi

**Successful Response:**
```json
{
  "id": 123,
  "paymentDate": "2025-05-30T11:00:00",
  "paymentMethod": 0,
  "paymentAmount": 50000000,
  "note": "Thanh toán nhiều hợp đồng test",
  "customerId": 1,
  "contractPayments": [
    {
      "id": 1,
      "contractId": 7,
      "allocatedAmount": 15000000
    },
    {
      "id": 2,
      "contractId": 9,
      "allocatedAmount": 24000000
    },
    {
      "id": 3,
      "contractId": 10,
      "allocatedAmount": 11000000
    }
  ]
}
```

## 📝 Payment Methods
- `0`: Tiền mặt
- `1`: Chuyển khoản
- `2`: Thẻ tín dụng
- `3`: Khác

---

**🎉 Chúc bạn test thành công!** 

Nếu có lỗi gì, hãy check backend logs và cho tôi biết để hỗ trợ thêm.
