# Hướng dẫn chạy đồng thời nhiều vi dịch vụ

Dự án này sử dụng Docker Compose để chạy đồng thời tất cả các vi dịch vụ trong hệ thống quản lý nhân công.

## Yêu cầu

- Docker và Docker Compose
- Java 17 hoặc cao hơn
- Maven

## Các dịch vụ

1. worker-service: cổng 8080
2. job-schedule-service: cổng 8081
3. register-service: cổng 8082
4. api-gateway: cổng 8083
5. worker-contract-service: cổng 8084
6. customer-service: cổng 8085
7. job-service: cổng 8086
8. customer-contract-service: cổng 8087
9. customer-payment-service: cổng 8088
10. PostgreSQL: cổng 5432

## Cách sử dụng

### Build và chạy tất cả các dịch vụ

#### Windows

```bash
build-and-run.bat
```

#### Linux/Mac

```bash
chmod +x build-and-run.sh
./build-and-run.sh
```

### Chỉ chạy các dịch vụ (không build lại)

#### Windows

```bash
run-services.bat
```

#### Linux/Mac

```bash
chmod +x run-services.sh
./run-services.sh
```

### Dừng tất cả các dịch vụ

#### Windows

```bash
stop-services.bat
```

#### Linux/Mac

```bash
chmod +x stop-services.sh
./stop-services.sh
```

## Truy cập API Gateway

Sau khi tất cả các dịch vụ đã được khởi động, bạn có thể truy cập API Gateway tại:

```
http://localhost:8083
```

## Cấu trúc thư mục

- `docker-compose.yml`: Cấu hình Docker Compose để chạy tất cả các dịch vụ
- `init-multiple-databases.sh`: Script để khởi tạo nhiều cơ sở dữ liệu trong PostgreSQL
- `build-and-run.bat/sh`: Script để build và chạy tất cả các dịch vụ
- `run-services.bat/sh`: Script để chỉ chạy các dịch vụ (không build lại)
- `stop-services.bat/sh`: Script để dừng tất cả các dịch vụ
- `api-gateway-docker.yml`: Cấu hình API Gateway cho môi trường Docker

## Lưu ý

- Đảm bảo rằng không có dịch vụ nào đang chạy trên các cổng được sử dụng (8080-8088, 5432)
- Nếu bạn thay đổi mã nguồn, bạn cần chạy lại script `build-and-run` để build lại các dịch vụ
- Dữ liệu PostgreSQL được lưu trữ trong volume Docker, vì vậy nó sẽ được giữ lại giữa các lần khởi động
