package com.aad.microservice.customer_payment_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerContract {
    private Long id;
    private String contractCode;     // Mã hợp đồng
    private LocalDate startingDate;  // Ngày bắt đầu
    private LocalDate endingDate;    // Ngày kết thúc
    private LocalDate signedDate;    // Ngày ký hợp đồng
    private Integer numberOfWorkers; // Số lượng nhân công cần
    private Double totalAmount;      // Tổng giá trị hợp đồng
    private Double totalPaid;        // Tổng số tiền đã thanh toán
    private String address;          // Địa chỉ làm việc
    private String description;      // Mô tả công việc
    private Long jobCategoryId;      // ID loại công việc
    private Long customerId;         // ID khách hàng
    private String customerName;     // Tên khách hàng (từ fullname)
    private Integer status;          // Trạng thái hợp đồng (0: Chờ xử lý, 1: Đang hoạt động, 2: Hoàn thành, 3: Đã hủy)
    private Boolean isDeleted;       // Đánh dấu đã xóa
    private LocalDateTime createdAt; // Thời gian tạo
    private LocalDateTime updatedAt; // Thời gian cập nhật

    // Trường tính toán (không lưu trong cơ sở dữ liệu)
    private transient Double totalDue; // Số tiền còn lại phải thanh toán

    /**
     * Tính toán số tiền còn lại phải thanh toán
     * @return Số tiền còn lại
     */
    public Double getTotalDue() {
        if (totalDue != null) {
            return totalDue;
        }

        if (totalAmount == null) {
            return 0.0;
        }

        if (totalPaid == null) {
            return totalAmount;
        }

        return totalAmount - totalPaid;
    }

    /**
     * Thiết lập số tiền còn lại phải thanh toán
     * @param totalDue Số tiền còn lại
     */
    public void setTotalDue(Double totalDue) {
        this.totalDue = totalDue;
    }
}
