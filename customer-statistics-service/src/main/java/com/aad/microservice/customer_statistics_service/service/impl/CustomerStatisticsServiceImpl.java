package com.aad.microservice.customer_statistics_service.service.impl;

import com.aad.microservice.customer_statistics_service.client.CustomerClient;
import com.aad.microservice.customer_statistics_service.client.CustomerContractClient;
import com.aad.microservice.customer_statistics_service.client.CustomerPaymentClient;
import com.aad.microservice.customer_statistics_service.model.Customer;
import com.aad.microservice.customer_statistics_service.model.CustomerContract;
import com.aad.microservice.customer_statistics_service.model.CustomerPayment;
import com.aad.microservice.customer_statistics_service.model.CustomerRevenue;
import com.aad.microservice.customer_statistics_service.model.TimeBasedRevenue;
import com.aad.microservice.customer_statistics_service.service.CustomerStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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


    private List<CustomerPayment> getAllPaymentsInDateRange(LocalDate startDate, LocalDate endDate) {
        try {
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
            List<CustomerPayment> filteredPayments = allPayments.stream()
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

            return filteredPayments;
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy danh sách hóa đơn: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lấy danh sách hóa đơn: " + e.getMessage());
        }
    }

    @Override
    public List<TimeBasedRevenue> getDailyRevenueStatistics(LocalDate startDate, LocalDate endDate) {
        try {
            // Validate input dates
            if (startDate == null || endDate == null) {
                throw new IllegalArgumentException("Ngày bắt đầu và ngày kết thúc không được để trống");
            }

            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Ngày bắt đầu không thể sau ngày kết thúc");
            }

            // Lấy danh sách hóa đơn trong khoảng thời gian
            List<CustomerPayment> payments = getAllPaymentsInDateRange(startDate, endDate);

            // Tạo map để lưu thông tin thống kê theo ngày
            Map<LocalDate, TimeBasedRevenue> dailyRevenueMap = new HashMap<>();

            // Tạo các ngày trong khoảng thời gian
            LocalDate currentDate = startDate;
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            while (!currentDate.isAfter(endDate)) {
                TimeBasedRevenue dailyRevenue = new TimeBasedRevenue();
                dailyRevenue.setDate(currentDate);
                dailyRevenue.setLabel(currentDate.format(dateFormatter));
                dailyRevenue.setTotalRevenue(0.0);
                dailyRevenue.setInvoiceCount(0);
                dailyRevenue.setPeriodType("daily");

                dailyRevenueMap.put(currentDate, dailyRevenue);
                currentDate = currentDate.plusDays(1);
            }

            // Tính tổng doanh thu và số lượng hóa đơn cho mỗi ngày
            for (CustomerPayment payment : payments) {
                LocalDateTime paymentDateTime = payment.getPaymentDate();
                if (paymentDateTime != null) {
                    LocalDate paymentDate = paymentDateTime.toLocalDate();
                    Double paymentAmount = payment.getPaymentAmount();

                    if (dailyRevenueMap.containsKey(paymentDate) && paymentAmount != null) {
                        TimeBasedRevenue dailyRevenue = dailyRevenueMap.get(paymentDate);
                        dailyRevenue.setTotalRevenue(dailyRevenue.getTotalRevenue() + paymentAmount);
                        dailyRevenue.setInvoiceCount(dailyRevenue.getInvoiceCount() + 1);
                    }
                }
            }

            // Chuyển map thành list và sắp xếp theo ngày
            List<TimeBasedRevenue> result = dailyRevenueMap.values().stream()
                    .sorted(Comparator.comparing(TimeBasedRevenue::getDate))
                    .collect(Collectors.toList());

            return result;
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy thống kê doanh thu theo ngày: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi tính toán thống kê doanh thu theo ngày: " + e.getMessage());
        }
    }

    @Override
    public List<TimeBasedRevenue> getWeeklyRevenueStatistics(LocalDate startDate, LocalDate endDate) {
        try {
            // Validate input dates
            if (startDate == null || endDate == null) {
                throw new IllegalArgumentException("Ngày bắt đầu và ngày kết thúc không được để trống");
            }

            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Ngày bắt đầu không thể sau ngày kết thúc");
            }

            // Lấy danh sách hóa đơn trong khoảng thời gian
            List<CustomerPayment> payments = getAllPaymentsInDateRange(startDate, endDate);

            // Điều chỉnh startDate về đầu tuần (thứ 2)
            LocalDate adjustedStartDate = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

            // Điều chỉnh endDate về cuối tuần (chủ nhật)
            LocalDate adjustedEndDate = endDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

            // Tạo map để lưu thông tin thống kê theo tuần
            Map<LocalDate, TimeBasedRevenue> weeklyRevenueMap = new HashMap<>();

            // Tạo các tuần trong khoảng thời gian
            LocalDate currentWeekStart = adjustedStartDate;
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            WeekFields weekFields = WeekFields.of(Locale.getDefault());

            while (!currentWeekStart.isAfter(adjustedEndDate)) {
                LocalDate weekEnd = currentWeekStart.plusDays(6); // Chủ nhật

                int weekNumber = currentWeekStart.get(weekFields.weekOfWeekBasedYear());
                int year = currentWeekStart.getYear();

                String label = String.format("Tuần %d (%s - %s)",
                        weekNumber,
                        currentWeekStart.format(dateFormatter),
                        weekEnd.format(dateFormatter));

                TimeBasedRevenue weeklyRevenue = new TimeBasedRevenue();
                weeklyRevenue.setDate(currentWeekStart);
                weeklyRevenue.setLabel(label);
                weeklyRevenue.setTotalRevenue(0.0);
                weeklyRevenue.setInvoiceCount(0);
                weeklyRevenue.setPeriodType("weekly");

                weeklyRevenueMap.put(currentWeekStart, weeklyRevenue);
                currentWeekStart = currentWeekStart.plusWeeks(1);
            }

            // Tính tổng doanh thu và số lượng hóa đơn cho mỗi tuần
            for (CustomerPayment payment : payments) {
                LocalDateTime paymentDateTime = payment.getPaymentDate();
                if (paymentDateTime != null) {
                    LocalDate paymentDate = paymentDateTime.toLocalDate();
                    LocalDate weekStart = paymentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                    Double paymentAmount = payment.getPaymentAmount();

                    if (weeklyRevenueMap.containsKey(weekStart) && paymentAmount != null) {
                        TimeBasedRevenue weeklyRevenue = weeklyRevenueMap.get(weekStart);
                        weeklyRevenue.setTotalRevenue(weeklyRevenue.getTotalRevenue() + paymentAmount);
                        weeklyRevenue.setInvoiceCount(weeklyRevenue.getInvoiceCount() + 1);
                    }
                }
            }

            // Chuyển map thành list và sắp xếp theo ngày bắt đầu tuần
            List<TimeBasedRevenue> result = weeklyRevenueMap.values().stream()
                    .sorted(Comparator.comparing(TimeBasedRevenue::getDate))
                    .collect(Collectors.toList());

            return result;
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy thống kê doanh thu theo tuần: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi tính toán thống kê doanh thu theo tuần: " + e.getMessage());
        }
    }

    @Override
    public List<TimeBasedRevenue> getMonthlyRevenueStatistics(LocalDate startDate, LocalDate endDate) {
        try {
            // Validate input dates
            if (startDate == null || endDate == null) {
                throw new IllegalArgumentException("Ngày bắt đầu và ngày kết thúc không được để trống");
            }

            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Ngày bắt đầu không thể sau ngày kết thúc");
            }

            // Lấy danh sách hóa đơn trong khoảng thời gian
            List<CustomerPayment> payments = getAllPaymentsInDateRange(startDate, endDate);

            // Điều chỉnh startDate về đầu tháng
            LocalDate adjustedStartDate = startDate.withDayOfMonth(1);

            // Điều chỉnh endDate về cuối tháng
            LocalDate adjustedEndDate = endDate.withDayOfMonth(endDate.lengthOfMonth());

            // Tạo map để lưu thông tin thống kê theo tháng
            Map<String, TimeBasedRevenue> monthlyRevenueMap = new HashMap<>();

            // Tạo các tháng trong khoảng thời gian
            LocalDate currentMonth = adjustedStartDate;
            DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MM/yyyy");
            DateTimeFormatter monthNameFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("vi"));

            while (!currentMonth.isAfter(adjustedEndDate)) {
                String monthKey = currentMonth.format(monthFormatter);
                String monthName = currentMonth.format(monthNameFormatter);

                // Capitalize first letter of month name
                monthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);

                TimeBasedRevenue monthlyRevenue = new TimeBasedRevenue();
                monthlyRevenue.setDate(currentMonth);
                monthlyRevenue.setLabel(monthName);
                monthlyRevenue.setTotalRevenue(0.0);
                monthlyRevenue.setInvoiceCount(0);
                monthlyRevenue.setPeriodType("monthly");

                monthlyRevenueMap.put(monthKey, monthlyRevenue);

                // Chuyển sang tháng tiếp theo
                if (currentMonth.getMonthValue() == 12) {
                    currentMonth = LocalDate.of(currentMonth.getYear() + 1, 1, 1);
                } else {
                    currentMonth = LocalDate.of(currentMonth.getYear(), currentMonth.getMonthValue() + 1, 1);
                }
            }

            // Tính tổng doanh thu và số lượng hóa đơn cho mỗi tháng
            for (CustomerPayment payment : payments) {
                LocalDateTime paymentDateTime = payment.getPaymentDate();
                if (paymentDateTime != null) {
                    LocalDate paymentDate = paymentDateTime.toLocalDate();
                    String monthKey = paymentDate.format(monthFormatter);
                    Double paymentAmount = payment.getPaymentAmount();

                    if (monthlyRevenueMap.containsKey(monthKey) && paymentAmount != null) {
                        TimeBasedRevenue monthlyRevenue = monthlyRevenueMap.get(monthKey);
                        monthlyRevenue.setTotalRevenue(monthlyRevenue.getTotalRevenue() + paymentAmount);
                        monthlyRevenue.setInvoiceCount(monthlyRevenue.getInvoiceCount() + 1);
                    }
                }
            }

            // Chuyển map thành list và sắp xếp theo ngày
            List<TimeBasedRevenue> result = monthlyRevenueMap.values().stream()
                    .sorted(Comparator.comparing(TimeBasedRevenue::getDate))
                    .collect(Collectors.toList());

            return result;
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy thống kê doanh thu theo tháng: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi tính toán thống kê doanh thu theo tháng: " + e.getMessage());
        }
    }

    @Override
    public List<TimeBasedRevenue> getYearlyRevenueStatistics(LocalDate startDate, LocalDate endDate) {
        try {
            // Validate input dates
            if (startDate == null || endDate == null) {
                throw new IllegalArgumentException("Ngày bắt đầu và ngày kết thúc không được để trống");
            }

            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Ngày bắt đầu không thể sau ngày kết thúc");
            }

            // Lấy danh sách hóa đơn trong khoảng thời gian
            List<CustomerPayment> payments = getAllPaymentsInDateRange(startDate, endDate);

            // Điều chỉnh startDate về đầu năm
            LocalDate adjustedStartDate = LocalDate.of(startDate.getYear(), 1, 1);

            // Điều chỉnh endDate về cuối năm
            LocalDate adjustedEndDate = LocalDate.of(endDate.getYear(), 12, 31);

            // Tạo map để lưu thông tin thống kê theo năm
            Map<Integer, TimeBasedRevenue> yearlyRevenueMap = new HashMap<>();

            // Tạo các năm trong khoảng thời gian
            int startYear = adjustedStartDate.getYear();
            int endYear = adjustedEndDate.getYear();

            for (int year = startYear; year <= endYear; year++) {
                LocalDate yearDate = LocalDate.of(year, 1, 1);

                TimeBasedRevenue yearlyRevenue = new TimeBasedRevenue();
                yearlyRevenue.setDate(yearDate);
                yearlyRevenue.setLabel("Năm " + year);
                yearlyRevenue.setTotalRevenue(0.0);
                yearlyRevenue.setInvoiceCount(0);
                yearlyRevenue.setPeriodType("yearly");

                yearlyRevenueMap.put(year, yearlyRevenue);
            }

            // Tính tổng doanh thu và số lượng hóa đơn cho mỗi năm
            for (CustomerPayment payment : payments) {
                LocalDateTime paymentDateTime = payment.getPaymentDate();
                if (paymentDateTime != null) {
                    int year = paymentDateTime.getYear();
                    Double paymentAmount = payment.getPaymentAmount();

                    if (yearlyRevenueMap.containsKey(year) && paymentAmount != null) {
                        TimeBasedRevenue yearlyRevenue = yearlyRevenueMap.get(year);
                        yearlyRevenue.setTotalRevenue(yearlyRevenue.getTotalRevenue() + paymentAmount);
                        yearlyRevenue.setInvoiceCount(yearlyRevenue.getInvoiceCount() + 1);
                    }
                }
            }

            // Chuyển map thành list và sắp xếp theo năm
            List<TimeBasedRevenue> result = yearlyRevenueMap.values().stream()
                    .sorted(Comparator.comparing(TimeBasedRevenue::getDate))
                    .collect(Collectors.toList());

            return result;
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy thống kê doanh thu theo năm: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi tính toán thống kê doanh thu theo năm: " + e.getMessage());
        }
    }
}
