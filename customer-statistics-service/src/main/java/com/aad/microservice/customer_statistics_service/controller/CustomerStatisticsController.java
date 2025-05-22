package com.aad.microservice.customer_statistics_service.controller;

import com.aad.microservice.customer_statistics_service.model.CustomerRevenue;
import com.aad.microservice.customer_statistics_service.model.CustomerPayment;
import com.aad.microservice.customer_statistics_service.service.CustomerStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer-statistics")
public class CustomerStatisticsController {

    @Autowired
    private CustomerStatisticsService customerStatisticsService;

    @GetMapping
    public ResponseEntity<String> root() {
        return ResponseEntity.ok("Customer Statistics Service API Root");
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Customer Statistics Service is working!");
    }

    @GetMapping("/revenue")
    public ResponseEntity<?> getCustomerRevenueStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            List<CustomerRevenue> statistics = customerStatisticsService.getCustomerRevenueStatistics(startDate, endDate);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy thống kê doanh thu: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Không thể tải dữ liệu thống kê khách hàng. Vui lòng thử lại sau."));
        }
    }

    @GetMapping("/customer/{customerId}/invoices")
    public ResponseEntity<?> getCustomerInvoices(
            @PathVariable Long customerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            List<CustomerPayment> invoices = customerStatisticsService.getCustomerInvoices(customerId, startDate, endDate);
            return ResponseEntity.ok(invoices);
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy danh sách hóa đơn: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Không thể tải dữ liệu hóa đơn. Vui lòng thử lại sau."));
        }
    }
}
