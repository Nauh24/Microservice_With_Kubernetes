# Sơ đồ tuần tự cho Module Thống kê Khách hàng theo Doanh thu

## Tổng quan

Sơ đồ tuần tự này mô tả luồng hoạt động của module "Thống kê khách hàng theo doanh thu", bao gồm các bước từ khi quản lý chọn chức năng thống kê đến khi hiển thị kết quả và xem chi tiết hóa đơn của khách hàng.

## Sơ đồ tuần tự

```mermaid
sequenceDiagram
    actor Manager as Quản lý
    
    participant Frontend
    participant ApiGateway
    participant StatsController as CustomerStatisticsController
    participant StatsService as CustomerStatisticsServiceImpl
    participant CustomerClient
    participant PaymentClient
    participant CustomerController
    participant PaymentController

    %% 1. Quản lý chọn chức năng thống kê
    Manager->>Frontend: Chọn chức năng thống kê khách hàng theo doanh thu
    Frontend->>Frontend: Hiển thị giao diện chọn khoảng thời gian

    %% 2. Quản lý chọn khoảng thời gian
    Manager->>Frontend: Chọn khoảng thời gian (startDate, endDate)
    Manager->>Frontend: Bấm nút thống kê

    %% 3. Frontend gửi request đến API Gateway
    Frontend->>ApiGateway: GET /api/customer-statistics/revenue?startDate={startDate}&endDate={endDate}
    
    %% 4. API Gateway định tuyến request đến CustomerStatisticsController
    ApiGateway->>StatsController: getCustomerRevenueStatistics(startDate, endDate)
    
    %% 5. CustomerStatisticsController gọi CustomerStatisticsService
    StatsController->>StatsService: getCustomerRevenueStatistics(startDate, endDate)
    
    %% 6. CustomerStatisticsServiceImpl lấy dữ liệu từ các service khác
    %% 6.1. Lấy danh sách khách hàng
    StatsService->>CustomerClient: getAllCustomers()
    CustomerClient->>CustomerController: GET /api/customer
    CustomerController-->>CustomerClient: List<Customer>
    CustomerClient-->>StatsService: List<Customer>
    
    %% 6.2. Lấy danh sách thanh toán trong khoảng thời gian
    StatsService->>PaymentClient: getPaymentsByDateRange(startDate, endDate)
    PaymentClient->>PaymentController: GET /api/customer-payment?startDate={startDate}&endDate={endDate}
    PaymentController-->>PaymentClient: List<CustomerPayment>
    PaymentClient-->>StatsService: List<CustomerPayment>
    
    %% 7. CustomerStatisticsServiceImpl tính toán doanh thu cho mỗi khách hàng
    StatsService->>StatsService: Tính toán doanh thu cho mỗi khách hàng
    
    %% 8. CustomerStatisticsServiceImpl sắp xếp kết quả theo doanh thu từ cao đến thấp
    StatsService->>StatsService: Sắp xếp kết quả theo doanh thu từ cao đến thấp
    
    %% 9. Trả về kết quả
    StatsService-->>StatsController: List<CustomerRevenue>
    StatsController-->>ApiGateway: ResponseEntity<List<CustomerRevenue>>
    ApiGateway-->>Frontend: List<CustomerRevenue>
    
    %% 10. Frontend hiển thị kết quả
    Frontend->>Frontend: Hiển thị danh sách khách hàng với doanh thu
    Frontend-->>Manager: Hiển thị kết quả thống kê
    
    %% 11. Quản lý click vào một khách hàng để xem chi tiết hóa đơn
    Manager->>Frontend: Click vào một khách hàng
    
    %% 12. Frontend gửi request lấy danh sách hóa đơn của khách hàng
    Frontend->>ApiGateway: GET /api/customer-statistics/customer/{customerId}/invoices?startDate={startDate}&endDate={endDate}
    
    %% 13. API Gateway định tuyến request đến CustomerStatisticsController
    ApiGateway->>StatsController: getCustomerInvoices(customerId, startDate, endDate)
    
    %% 14. CustomerStatisticsController gọi CustomerStatisticsService
    StatsController->>StatsService: getCustomerInvoices(customerId, startDate, endDate)
    
    %% 15. CustomerStatisticsServiceImpl lấy danh sách hóa đơn của khách hàng
    StatsService->>PaymentClient: getPaymentsByCustomerId(customerId)
    PaymentClient->>PaymentController: GET /api/customer-payment/customer/{customerId}
    PaymentController-->>PaymentClient: List<CustomerPayment>
    PaymentClient-->>StatsService: List<CustomerPayment>
    
    %% 16. CustomerStatisticsServiceImpl lọc hóa đơn trong khoảng thời gian
    StatsService->>StatsService: Lọc hóa đơn trong khoảng thời gian
    
    %% 17. Trả về kết quả
    StatsService-->>StatsController: List<CustomerPayment>
    StatsController-->>ApiGateway: ResponseEntity<List<CustomerPayment>>
    ApiGateway-->>Frontend: List<CustomerPayment>
    
    %% 18. Frontend hiển thị danh sách hóa đơn
    Frontend->>Frontend: Hiển thị danh sách hóa đơn của khách hàng
    Frontend-->>Manager: Hiển thị danh sách hóa đơn
```

## Giải thích chi tiết luồng hoạt động

### 1. Hiển thị giao diện thống kê
- Quản lý chọn chức năng thống kê khách hàng theo doanh thu
- Frontend hiển thị giao diện chọn khoảng thời gian

### 2. Chọn khoảng thời gian và thực hiện thống kê
- Quản lý chọn khoảng thời gian (ngày bắt đầu, ngày kết thúc)
- Quản lý bấm nút thống kê
- Frontend gửi request đến API Gateway
- API Gateway định tuyến request đến CustomerStatisticsController
- CustomerStatisticsController gọi phương thức getCustomerRevenueStatistics() của CustomerStatisticsService

### 3. Lấy dữ liệu và tính toán thống kê
- CustomerStatisticsServiceImpl lấy danh sách khách hàng từ CustomerClient
- CustomerStatisticsServiceImpl lấy danh sách thanh toán trong khoảng thời gian từ PaymentClient
- CustomerStatisticsServiceImpl tính toán tổng doanh thu cho mỗi khách hàng
- CustomerStatisticsServiceImpl sắp xếp kết quả theo tổng doanh thu từ cao đến thấp

### 4. Hiển thị kết quả thống kê
- Kết quả được trả về qua các lớp trung gian, thông qua API Gateway đến Frontend
- Frontend hiển thị danh sách khách hàng với thông tin doanh thu

### 5. Xem chi tiết hóa đơn của khách hàng
- Quản lý click vào một khách hàng để xem chi tiết hóa đơn
- Frontend gửi request đến API Gateway để lấy danh sách hóa đơn của khách hàng đó
- CustomerStatisticsController gọi phương thức getCustomerInvoices() của CustomerStatisticsService
- CustomerStatisticsServiceImpl lấy danh sách thanh toán của khách hàng từ PaymentClient
- CustomerStatisticsServiceImpl lọc hóa đơn trong khoảng thời gian đã chọn
- Kết quả được trả về và hiển thị trên Frontend
