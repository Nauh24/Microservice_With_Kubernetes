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
        try {
            // Validate input dates
            if (startDate == null || endDate == null) {
                throw new IllegalArgumentException("Ngày bắt đầu và ngày kết thúc không được để trống");
            }

            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Ngày bắt đầu không thể sau ngày kết thúc");
            }

            // Lấy danh sách tất cả khách hàng
            List<Customer> customers;
            try {
                customers = customerClient.getAllCustomers();
                System.out.println("Đã lấy " + customers.size() + " khách hàng từ customer-service");
            } catch (Exception e) {
                System.err.println("Lỗi khi lấy danh sách khách hàng: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Không thể kết nối đến customer-service: " + e.getMessage());
            }

            // Lấy danh sách tất cả hợp đồng trong khoảng thời gian
            List<CustomerContract> contracts;
            try {
                contracts = contractClient.getContractsByDateRange(startDate, endDate);
                System.out.println("Đã lấy " + contracts.size() + " hợp đồng từ customer-contract-service");
            } catch (Exception e) {
                System.err.println("Lỗi khi lấy danh sách hợp đồng: " + e.getMessage());
                e.printStackTrace();
                contracts = new ArrayList<>();
                System.out.println("Tiếp tục với danh sách hợp đồng trống");
            }

            // Lấy danh sách tất cả hóa đơn
            List<CustomerPayment> allPayments;
            try {
                allPayments = paymentClient.getAllPayments();
                System.out.println("Đã lấy " + allPayments.size() + " hóa đơn từ customer-payment-service");
            } catch (Exception e) {
                System.err.println("Lỗi khi lấy danh sách hóa đơn: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Không thể kết nối đến customer-payment-service: " + e.getMessage());
            }

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

            System.out.println("Đã lọc " + payments.size() + " hóa đơn trong khoảng thời gian từ " +
                              startDate + " đến " + endDate);

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
                if (customerId != null && customerRevenueMap.containsKey(customerId)) {
                    CustomerRevenue revenue = customerRevenueMap.get(customerId);
                    revenue.setContractCount(revenue.getContractCount() + 1);
                }
            }

            // Tính tổng doanh thu cho mỗi khách hàng
            for (CustomerPayment payment : payments) {
                Long customerId = payment.getCustomerId();
                Double paymentAmount = payment.getPaymentAmount();
                if (customerId != null && customerRevenueMap.containsKey(customerId) && paymentAmount != null) {
                    CustomerRevenue revenue = customerRevenueMap.get(customerId);
                    revenue.setTotalRevenue(revenue.getTotalRevenue() + paymentAmount);
                }
            }

            // Chuyển map thành list và lọc những khách hàng có doanh thu > 0
            List<CustomerRevenue> result = customerRevenueMap.values().stream()
                    .filter(revenue -> revenue.getTotalRevenue() > 0)
                    .sorted((r1, r2) -> r2.getTotalRevenue().compareTo(r1.getTotalRevenue())) // Sắp xếp giảm dần theo doanh thu
                    .collect(Collectors.toList());

            System.out.println("Trả về " + result.size() + " khách hàng có doanh thu > 0");
            return result;
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy thống kê doanh thu: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi tính toán thống kê doanh thu: " + e.getMessage());
        }
    }

    @Override
    public List<CustomerPayment> getCustomerInvoices(Long customerId, LocalDate startDate, LocalDate endDate) {
        try {
            // Validate input parameters
            if (customerId == null) {
                throw new IllegalArgumentException("ID khách hàng không được để trống");
            }

            if (startDate == null || endDate == null) {
                throw new IllegalArgumentException("Ngày bắt đầu và ngày kết thúc không được để trống");
            }

            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Ngày bắt đầu không thể sau ngày kết thúc");
            }

            // Lấy danh sách hóa đơn của khách hàng
            List<CustomerPayment> customerPayments;
            try {
                customerPayments = paymentClient.getPaymentsByCustomerId(customerId);
                System.out.println("Đã lấy " + customerPayments.size() + " hóa đơn của khách hàng ID: " + customerId);
            } catch (Exception e) {
                System.err.println("Lỗi khi lấy danh sách hóa đơn của khách hàng: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Không thể kết nối đến customer-payment-service: " + e.getMessage());
            }

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

            System.out.println("Đã lọc " + filteredPayments.size() + " hóa đơn trong khoảng thời gian từ " +
                              startDate + " đến " + endDate);

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
                        System.err.println("Không thể lấy thông tin hợp đồng ID: " + payment.getCustomerContractId() + " - " + e.getMessage());
                        // Không throw exception ở đây, chỉ log lỗi và tiếp tục
                        payment.setContractCode("Không xác định");
                    }
                }
            }

            // Sắp xếp hóa đơn theo ngày thanh toán giảm dần (mới nhất lên đầu)
            filteredPayments.sort((p1, p2) -> {
                if (p1.getPaymentDate() == null && p2.getPaymentDate() == null) return 0;
                if (p1.getPaymentDate() == null) return 1;
                if (p2.getPaymentDate() == null) return -1;
                return p2.getPaymentDate().compareTo(p1.getPaymentDate());
            });

            return filteredPayments;
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy danh sách hóa đơn: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lấy danh sách hóa đơn: " + e.getMessage());
        }
    }
}
