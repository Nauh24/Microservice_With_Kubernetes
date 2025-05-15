package com.aad.microservice.customer_statistics_service.service.impl;

import com.aad.microservice.customer_statistics_service.client.CustomerClient;
import com.aad.microservice.customer_statistics_service.client.CustomerContractClient;
import com.aad.microservice.customer_statistics_service.client.CustomerPaymentClient;
import com.aad.microservice.customer_statistics_service.model.Customer;
import com.aad.microservice.customer_statistics_service.model.CustomerContract;
import com.aad.microservice.customer_statistics_service.model.CustomerPayment;
import com.aad.microservice.customer_statistics_service.model.CustomerRevenue;
import com.aad.microservice.customer_statistics_service.service.CustomerStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CustomerStatisticsServiceImpl implements CustomerStatisticsService {

    @Autowired
    private CustomerClient customerClient;

    @Autowired
    private CustomerContractClient contractClient;

    @Autowired
    private CustomerPaymentClient paymentClient;

    @Override
    public List<CustomerRevenue> getCustomerRevenueStatistics(LocalDate startDate, LocalDate endDate) {
        // Lấy danh sách tất cả khách hàng
        List<Customer> customers = customerClient.getAllCustomers();

        // Lấy danh sách tất cả hợp đồng trong khoảng thời gian
        List<CustomerContract> contracts = contractClient.getContractsByDateRange(startDate, endDate);

        // Lấy danh sách tất cả hóa đơn
        List<CustomerPayment> allPayments = paymentClient.getAllPayments();

        // Lọc hóa đơn trong khoảng thời gian
        List<CustomerPayment> payments = allPayments.stream()
                .filter(payment -> {
                    LocalDateTime paymentDateTime = payment.getPaymentDate();
                    LocalDate paymentDate = paymentDateTime != null ? paymentDateTime.toLocalDate() : null;
                    return paymentDate != null &&
                           !paymentDate.isBefore(startDate) &&
                           !paymentDate.isAfter(endDate);
                })
                .collect(Collectors.toList());

        // Tạo map để lưu thông tin thống kê theo khách hàng
        Map<Long, CustomerRevenue> customerRevenueMap = new HashMap<>();

        // Khởi tạo thông tin cơ bản cho mỗi khách hàng
        for (Customer customer : customers) {
            CustomerRevenue revenue = new CustomerRevenue();
            revenue.setId(customer.getId());
            revenue.setFullName(customer.getFullName());
            revenue.setCompanyName(customer.getCompanyName());
            revenue.setPhoneNumber(customer.getPhoneNumber());
            revenue.setEmail(customer.getEmail());
            revenue.setAddress(customer.getAddress());
            revenue.setIsDeleted(customer.getIsDeleted());
            revenue.setCreatedAt(customer.getCreatedAt());
            revenue.setUpdatedAt(customer.getUpdatedAt());
            revenue.setContractCount(0);
            revenue.setTotalRevenue(0.0);

            customerRevenueMap.put(customer.getId(), revenue);
        }

        // Tính số lượng hợp đồng cho mỗi khách hàng
        for (CustomerContract contract : contracts) {
            Long customerId = contract.getCustomerId();
            if (customerRevenueMap.containsKey(customerId)) {
                CustomerRevenue revenue = customerRevenueMap.get(customerId);
                revenue.setContractCount(revenue.getContractCount() + 1);
            }
        }

        // Tính tổng doanh thu cho mỗi khách hàng
        for (CustomerPayment payment : payments) {
            Long customerId = payment.getCustomerId();
            Double paymentAmount = payment.getPaymentAmount();
            if (customerRevenueMap.containsKey(customerId) && paymentAmount != null) {
                CustomerRevenue revenue = customerRevenueMap.get(customerId);
                revenue.setTotalRevenue(revenue.getTotalRevenue() + paymentAmount);
            }
        }

        // Chuyển map thành list và lọc những khách hàng có doanh thu > 0
        return customerRevenueMap.values().stream()
                .filter(revenue -> revenue.getTotalRevenue() > 0)
                .sorted((r1, r2) -> r2.getTotalRevenue().compareTo(r1.getTotalRevenue())) // Sắp xếp giảm dần theo doanh thu
                .collect(Collectors.toList());
    }

    @Override
    public List<CustomerPayment> getCustomerInvoices(Long customerId, LocalDate startDate, LocalDate endDate) {
        // Lấy danh sách hóa đơn của khách hàng
        List<CustomerPayment> customerPayments = paymentClient.getPaymentsByCustomerId(customerId);

        // Lọc hóa đơn trong khoảng thời gian
        List<CustomerPayment> filteredPayments = customerPayments.stream()
                .filter(payment -> {
                    LocalDateTime paymentDateTime = payment.getPaymentDate();
                    LocalDate paymentDate = paymentDateTime != null ? paymentDateTime.toLocalDate() : null;
                    return paymentDate != null &&
                           !paymentDate.isBefore(startDate) &&
                           !paymentDate.isAfter(endDate);
                })
                .collect(Collectors.toList());

        // Lấy thông tin mã hợp đồng cho mỗi hóa đơn
        for (CustomerPayment payment : filteredPayments) {
            if (payment.getCustomerContractId() != null) {
                try {
                    CustomerContract contract = contractClient.getContractById(payment.getCustomerContractId());
                    if (contract != null) {
                        payment.setContractCode(contract.getContractCode());
                    }
                } catch (Exception e) {
                    // Xử lý trường hợp không tìm thấy hợp đồng
                    System.out.println("Không thể lấy thông tin hợp đồng ID: " + payment.getCustomerContractId() + " - " + e.getMessage());
                }
            }
        }

        return filteredPayments;
    }
}
