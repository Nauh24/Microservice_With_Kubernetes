package com.aad.microservice.customer_statistics_service.controller;

import com.aad.microservice.customer_statistics_service.model.CustomerRevenue;
import com.aad.microservice.customer_statistics_service.model.CustomerPayment;
import com.aad.microservice.customer_statistics_service.service.CustomerStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer-statistics")
@CrossOrigin(origins = "*") // Allow requests from any origin
public class CustomerStatisticsController {

    @Autowired
    private CustomerStatisticsService customerStatisticsService;

    @GetMapping
    public ResponseEntity<Map<String, String>> root() {
        Map<String, String> response = new HashMap<>();
        response.put("service", "Customer Statistics Service");
        response.put("status", "running");
        response.put("version", "1.0");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("message", "Customer Statistics Service is working!");
        health.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(health);
    }

    @GetMapping("/revenue")
    public ResponseEntity<?> getCustomerRevenueStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            // Validate input parameters
            if (startDate == null || endDate == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Ngày bắt đầu và ngày kết thúc không được để trống");
                return ResponseEntity.badRequest().body(error);
            }

            if (startDate.isAfter(endDate)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Ngày bắt đầu không thể sau ngày kết thúc");
                return ResponseEntity.badRequest().body(error);
            }

            System.out.println("Đang lấy thống kê doanh thu từ " + startDate + " đến " + endDate);
            List<CustomerRevenue> statistics = customerStatisticsService.getCustomerRevenueStatistics(startDate, endDate);
            System.out.println("Đã lấy " + statistics.size() + " kết quả thống kê");
            return ResponseEntity.ok(statistics);
        } catch (IllegalArgumentException e) {
            System.err.println("Lỗi tham số đầu vào: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy thống kê doanh thu: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Không thể tải dữ liệu thống kê khách hàng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/customer/{customerId}/invoices")
    public ResponseEntity<?> getCustomerInvoices(
            @PathVariable Long customerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            // Validate input parameters
            if (customerId == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "ID khách hàng không được để trống");
                return ResponseEntity.badRequest().body(error);
            }

            if (startDate == null || endDate == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Ngày bắt đầu và ngày kết thúc không được để trống");
                return ResponseEntity.badRequest().body(error);
            }

            if (startDate.isAfter(endDate)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Ngày bắt đầu không thể sau ngày kết thúc");
                return ResponseEntity.badRequest().body(error);
            }

            System.out.println("Đang lấy danh sách hóa đơn của khách hàng ID: " + customerId +
                              " từ " + startDate + " đến " + endDate);
            List<CustomerPayment> invoices = customerStatisticsService.getCustomerInvoices(customerId, startDate, endDate);
            System.out.println("Đã lấy " + invoices.size() + " hóa đơn");
            return ResponseEntity.ok(invoices);
        } catch (IllegalArgumentException e) {
            System.err.println("Lỗi tham số đầu vào: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy danh sách hóa đơn: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Không thể tải dữ liệu hóa đơn: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
