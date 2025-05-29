# Khắc Phục Vấn Đề Kết Nối Frontend-Backend trên Docker

## Vấn Đề Ban Đầu
Frontend không thể kết nối được với backend khi các microservices chạy trên Docker containers.

## Nguyên Nhân
1. **CORS Configuration không đầy đủ** - API Gateway không cho phép tất cả origins
2. **Server Binding** - API Gateway chỉ bind localhost trong container
3. **Proxy Configuration thiếu** - Frontend không có proxy để route API calls
4. **CORS Configuration trùng lặp** - Có 2 file CORS config gây xung đột

## Giải Pháp Đã Thực Hiện

### 1. Cải Thiện CORS Configuration trong API Gateway

**File**: `api-gateway/src/main/java/microservice/api_gateway/CorsGlobalConfiguration.java`

**Thay đổi**:
```java
// Trước
config.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:3001", "*"));

// Sau
config.setAllowedOriginPatterns(Collections.singletonList("*"));
config.setAllowedHeaders(Arrays.asList("*"));
config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"));
config.setExposedHeaders(Arrays.asList("Content-Disposition", "Content-Type", "Content-Length"));
config.setMaxAge(3600L);
```

### 2. Xóa File CORS Configuration Trùng Lặp

**Đã xóa**: `api-gateway/src/main/java/com/aad/microservice/api_gateway/config/CorsConfig.java`

**Lý do**: Tránh xung đột giữa 2 cấu hình CORS khác nhau.

### 3. Cấu Hình Server Binding

**File**: `api-gateway/src/main/resources/application-docker.yml`

**Thêm**:
```yaml
server:
  port: 8080
  address: 0.0.0.0  # Bind to all interfaces để có thể truy cập từ host machine
```

### 4. Thêm Proxy Configuration cho Frontend

**File mới**: `microservice_fe/src/setupProxy.js`

```javascript
const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function(app) {
  app.use(
    '/api',
    createProxyMiddleware({
      target: process.env.REACT_APP_API_URL || 'http://localhost:8080',
      changeOrigin: true,
      secure: false,
      logLevel: 'debug'
    })
  );
};
```

**Package cài thêm**:
```bash
npm install http-proxy-middleware --save-dev
```

### 5. Cập Nhật API Client

**File**: `microservice_fe/src/services/api/apiClient.ts`

**Thay đổi**:
```javascript
// Trước
const getBaseUrl = () => {
  if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
    const apiGatewayUrl = process.env.REACT_APP_API_URL || 'http://localhost:8080';
    return apiGatewayUrl;
  }
  return '';
};

// Sau
const getBaseUrl = () => {
  // Sử dụng proxy setup, không cần base URL
  // Proxy sẽ chuyển tiếp tất cả /api requests tới API Gateway
  return '';
};
```

## Kết Quả

### ✅ Các Endpoint Đã Test Thành Công:

1. **API Gateway Health Check**:
   ```bash
   curl http://localhost:8080/actuator/health
   # Response: {"status":"UP"}
   ```

2. **Customer Service qua API Gateway**:
   ```bash
   curl http://localhost:8080/api/customer
   # Response: [{"id":1,"fullName":"Nguyễn Văn Huân",...}]
   ```

3. **Job Service qua API Gateway**:
   ```bash
   curl http://localhost:8080/api/job-category
   # Response: [{"id":21,"name":"Cong nhan xay dung",...}]
   ```

4. **Frontend**:
   - Khởi động thành công tại `http://localhost:3000`
   - Proxy hoạt động, chuyển tiếp `/api/*` tới `http://localhost:8080`

### ✅ Tất Cả Containers Đang Chạy:

```bash
docker-compose ps
```

| Service | Port | Status |
|---------|------|--------|
| api-gateway | 8080 | ✅ Up |
| customer-service | 8081 | ✅ Up |
| job-service | 8082 | ✅ Up |
| customer-contract-service | 8083 | ✅ Up |
| customer-payment-service | 8084 | ✅ Up |
| customer-statistics-service | 8085 | ✅ Up |

## Kiến Trúc Kết Nối Hiện Tại

```
Frontend (localhost:3000)
    ↓ [setupProxy.js routes /api/* to localhost:8080]
API Gateway (localhost:8080)
    ↓ [CORS enabled, routes to internal services]
Microservices (customer-service:8081, job-service:8082, ...)
    ↓ [connects via host.docker.internal]
PostgreSQL (localhost:5432)
```

## Hướng Dẫn Sử Dụng

### Khởi Động Hệ Thống:
```bash
# 1. Khởi động Docker containers
docker-compose up -d

# 2. Khởi động Frontend
cd microservice_fe
npm start

# 3. Truy cập ứng dụng
# Frontend: http://localhost:3000
# API Gateway: http://localhost:8080
```

### Kiểm Tra Kết Nối:
```bash
# Test API Gateway
curl http://localhost:8080/actuator/health

# Test routing
curl http://localhost:8080/api/customer
curl http://localhost:8080/api/job-category
```

## Lưu Ý Quan Trọng

1. **Proxy Configuration**: Frontend sử dụng proxy để tự động chuyển tiếp API calls
2. **CORS**: API Gateway đã được cấu hình để cho phép tất cả origins
3. **Server Binding**: API Gateway bind 0.0.0.0 để accessible từ host machine
4. **Database Connection**: Services kết nối PostgreSQL qua `host.docker.internal`
5. **Port Mapping**: Tất cả services đều expose ports để test trực tiếp nếu cần

**Kết luận**: Vấn đề kết nối frontend-backend đã được khắc phục hoàn toàn. Hệ thống hiện hoạt động ổn định với kiến trúc microservices trên Docker.
