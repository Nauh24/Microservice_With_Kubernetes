# API Usage Examples - Customer Payment Service

## 1. Tạo thanh toán cho nhiều hợp đồng

### Endpoint: `POST /api/customer-payment/multiple-contracts`

### Request Body:
```json
{
  "paymentDate": "2024-01-15T10:30:00",
  "paymentMethod": 1,
  "totalAmount": 15000000,
  "note": "Thanh toán gộp cho 3 hợp đồng tháng 1/2024",
  "customerId": 1,
  "contractPayments": [
    {
      "contractId": 101,
      "allocatedAmount": 5000000
    },
    {
      "contractId": 102,
      "allocatedAmount": 7000000
    },
    {
      "contractId": 103,
      "allocatedAmount": 3000000
    }
  ]
}
```

### Response:
```json
{
  "id": 25,
  "paymentDate": "2024-01-15T10:30:00",
  "paymentMethod": 1,
  "paymentAmount": 15000000,
  "note": "Thanh toán gộp cho 3 hợp đồng tháng 1/2024",
  "customerId": 1,
  "isDeleted": false,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00",
  "contractPayments": [
    {
      "id": 1,
      "contractId": 101,
      "allocatedAmount": 5000000,
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-15T10:30:00"
    },
    {
      "id": 2,
      "contractId": 102,
      "allocatedAmount": 7000000,
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-15T10:30:00"
    },
    {
      "id": 3,
      "contractId": 103,
      "allocatedAmount": 3000000,
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-15T10:30:00"
    }
  ]
}
```

## 2. Lấy danh sách thanh toán theo Payment ID

### Endpoint: `GET /api/customer-payment/payment/{paymentId}/contract-payments`

### Response:
```json
[
  {
    "id": 1,
    "contractId": 101,
    "allocatedAmount": 5000000,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  },
  {
    "id": 2,
    "contractId": 102,
    "allocatedAmount": 7000000,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
]
```

## 3. Lấy danh sách thanh toán theo Contract ID

### Endpoint: `GET /api/customer-payment/contract/{contractId}/contract-payments`

### Response:
```json
[
  {
    "id": 1,
    "contractId": 101,
    "allocatedAmount": 5000000,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  },
  {
    "id": 4,
    "contractId": 101,
    "allocatedAmount": 3000000,
    "createdAt": "2024-01-20T14:15:00",
    "updatedAt": "2024-01-20T14:15:00"
  }
]
```

## 4. Validation Rules

### Khi tạo thanh toán nhiều hợp đồng:
1. **Tổng số tiền phân bổ phải bằng tổng số tiền thanh toán**
2. **Mỗi hợp đồng phải tồn tại và đang ở trạng thái ACTIVE hoặc PENDING**
3. **Số tiền phân bổ cho mỗi hợp đồng không được vượt quá số tiền còn lại**
4. **Khách hàng phải tồn tại trong hệ thống**

### Error Examples:
```json
{
  "error": "InvalidAmount_Exception",
  "message": "Tổng số tiền phân bổ (14000000 VNĐ) phải bằng tổng số tiền thanh toán (15000000 VNĐ)"
}
```

```json
{
  "error": "InvalidAmount_Exception", 
  "message": "Số tiền thanh toán cho hợp đồng ID 101 (8000000 VNĐ) không được vượt quá số tiền còn lại (5000000 VNĐ)"
}
```
