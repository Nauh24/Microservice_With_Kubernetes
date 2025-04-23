package com.aad.microservice.customer_contract_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_contracts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerContract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String contractCode;     // Mã hợp đồng

    // Thông tin thời gian
    private LocalDate startingDate;  // Ngày bắt đầu
    private LocalDate endingDate;    // Ngày kết thúc
    private LocalDate signedDate;    // Ngày ký hợp đồng

    // Thông tin công việc
    private Integer numberOfWorkers;  // Số lượng nhân công cần
    private Double totalAmount;      // Tổng giá trị hợp đồng
    private String address;          // Địa chỉ làm việc
    private String description;      // Mô tả công việc

    // Quan hệ với các đối tượng khác
    private Long jobCategoryId;      // ID loại công việc
    private Long customerId;         // ID khách hàng

    // Trạng thái và thông tin hệ thống
    private Integer status;          // Trạng thái hợp đồng (0: Nháp, 1: Chờ ký, 2: Đang hoạt động, 3: Hoàn thành, 4: Hủy)
    private Boolean isDeleted;       // Đánh dấu đã xóa
    private LocalDateTime createdAt; // Thời gian tạo
    private LocalDateTime updatedAt; // Thời gian cập nhật
}
