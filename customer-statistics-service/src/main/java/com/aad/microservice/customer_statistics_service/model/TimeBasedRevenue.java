package com.aad.microservice.customer_statistics_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Model để lưu trữ thông tin doanh thu theo thời gian
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeBasedRevenue {
    // Thời gian (ngày, tuần, tháng, năm)
    private LocalDate date;
    
    // Nhãn hiển thị
    private String label;
    
    // Tổng doanh thu trong khoảng thời gian
    private Double totalRevenue;
    
    // Số lượng hóa đơn trong khoảng thời gian
    private Integer invoiceCount;
    
    private String periodType;
}
