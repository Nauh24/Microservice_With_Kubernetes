# Hướng dẫn triển khai Docker cho Microservices

Dự án này sử dụng Docker Compose để chạy đồng thời tất cả các microservice trong hệ thống quản lý nhân công.

## Yêu cầu

- Docker và Docker Compose
- Java 17
- Maven
- Node.js và npm

## Các microservice

1. **customer-service**: Quản lý thông tin khách hàng (cổng 8085)
2. **job-service**: Quản lý thông tin công việc và loại công việc (cổng 8086)
3. **customer-contract-service**: Quản lý hợp đồng khách hàng (cổng 8087)
4. **customer-payment-service**: Quản lý thanh toán của khách hàng (cổng 8088)
5. **customer-statistics-service**: Thống kê doanh thu khách hàng (cổng 8089)
6. **api-gateway**: API Gateway cho tất cả các service (cổng 8083)
7. **frontend**: Giao diện người dùng React (cổng 3000)

## Cách triển khai

### Windows

```bash
run-docker.bat
```

### Linux/Mac

```bash
chmod +x run-docker.sh
./run-docker.sh
```

## Cấu trúc Docker

### Dockerfile

Mỗi microservice đều có một Dockerfile riêng với cấu trúc cơ bản như sau:

```dockerfile
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=docker", "-Dspring.devtools.restart.enabled=true", "-Dspring.devtools.add-properties=true", "/app/app.jar"]
```

### docker-compose-minimal.yml

File docker-compose-minimal.yml định nghĩa tất cả các service cần thiết:

- **postgres**: Cơ sở dữ liệu PostgreSQL
- **customer-service**: Service quản lý khách hàng
- **job-service**: Service quản lý công việc
- **customer-contract-service**: Service quản lý hợp đồng
- **customer-payment-service**: Service quản lý thanh toán
- **customer-statistics-service**: Service thống kê
- **api-gateway**: API Gateway
- **frontend**: Giao diện người dùng React

### Cơ sở dữ liệu

Dự án sử dụng PostgreSQL với nhiều cơ sở dữ liệu cho từng service:

- customerMSDb
- jobMSDb
- customerContractMSDb
- customerPaymentMSDb

Script `init-multiple-databases.sh` được sử dụng để tự động tạo các cơ sở dữ liệu này khi container PostgreSQL khởi động.

## Truy cập các service

- **API Gateway**: http://localhost:8083
- **Frontend**: http://localhost:3000
- **customer-service**: http://localhost:8085
- **job-service**: http://localhost:8086
- **customer-contract-service**: http://localhost:8087
- **customer-payment-service**: http://localhost:8088
- **customer-statistics-service**: http://localhost:8089

## Gỡ lỗi

### Kiểm tra logs

```bash
# Xem logs của tất cả các container
docker-compose -f docker-compose-minimal.yml logs

# Xem logs của một container cụ thể
docker-compose -f docker-compose-minimal.yml logs customer-service
```

### Khởi động lại một service

```bash
docker-compose -f docker-compose-minimal.yml restart customer-service
```

### Dừng tất cả các service

```bash
docker-compose -f docker-compose-minimal.yml down
```

### Xóa volumes và khởi động lại

```bash
docker-compose -f docker-compose-minimal.yml down -v
./run-docker.bat  # hoặc ./run-docker.sh trên Linux/Mac
```

## Lưu ý

- Đảm bảo rằng không có dịch vụ nào đang chạy trên các cổng được sử dụng (8083, 8085-8089, 3000, 5432)
- Nếu bạn thay đổi mã nguồn, bạn cần chạy lại script `run-docker.bat` hoặc `run-docker.sh` để build lại các service
- Dữ liệu PostgreSQL được lưu trữ trong volume Docker, vì vậy nó sẽ được giữ lại giữa các lần khởi động
