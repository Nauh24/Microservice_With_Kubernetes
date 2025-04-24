# Hướng dẫn khắc phục lỗi xung đột bean trong job-service

## Vấn đề

Khi chạy job-service, bạn gặp lỗi sau:

```
org.springframework.beans.factory.BeanDefinitionStoreException: Failed to parse configuration class [com.aad.microservice.job_service.JobServiceApplication]
...
Caused by: org.springframework.context.annotation.ConflictingBeanDefinitionException: Annotation-specified bean name 'jobCategoryServiceImpl' for bean class [com.aad.microservice.job_service.service.JobCategoryServiceImpl] conflicts with existing, non-compatible bean definition of same name and class [com.aad.microservice.job_service.service.impl.JobCategoryServiceImpl]
```

Lỗi này xảy ra do có hai class cùng tên `JobCategoryServiceImpl` ở hai package khác nhau:
1. `com.aad.microservice.job_service.service.JobCategoryServiceImpl`
2. `com.aad.microservice.job_service.service.impl.JobCategoryServiceImpl`

Cả hai đều được đánh dấu là `@Service` và cùng triển khai interface `JobCategoryService`, dẫn đến xung đột khi Spring Boot cố gắng tạo bean.

## Cách khắc phục

Để khắc phục vấn đề này, bạn cần xóa một trong hai class. Trong trường hợp này, chúng ta sẽ xóa class `JobCategoryServiceImpl` trong package `service` và giữ lại class trong package `service.impl` (đây là cách tổ chức code chuẩn hơn).

### Sử dụng script tự động

Chạy script `fix-job-service.bat` để tự động khắc phục vấn đề:

```bash
fix-job-service.bat
```

Script này sẽ:
1. Xóa file `JobCategoryServiceImpl.java` trong package `service`
2. Xóa các file đã biên dịch trong thư mục `target`
3. Build lại job-service

### Khắc phục thủ công

Nếu bạn muốn khắc phục thủ công, hãy thực hiện các bước sau:

1. Xóa file `job-service\src\main\java\com\aad\microservice\job_service\service\JobCategoryServiceImpl.java`

2. Xóa các file đã biên dịch:
   ```bash
   cd job-service
   mvn clean
   ```

3. Build lại job-service:
   ```bash
   cd job-service
   mvn package -DskipTests
   ```

4. Chạy lại job-service

## Nguyên nhân gốc rễ

Vấn đề này có thể xảy ra khi:
1. Copy/paste code từ nơi khác mà không điều chỉnh package
2. Tạo class mới nhưng quên xóa class cũ
3. Tái cấu trúc code nhưng không xóa các file cũ

## Phòng ngừa trong tương lai

Để tránh vấn đề tương tự trong tương lai:
1. Tuân thủ cấu trúc package nhất quán
2. Sử dụng IDE để di chuyển/đổi tên class thay vì copy/paste
3. Xóa các file cũ khi tái cấu trúc code
4. Thực hiện clean build thường xuyên để phát hiện vấn đề sớm
