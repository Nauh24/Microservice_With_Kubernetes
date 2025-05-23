package com.aad.microservice.customer_statistics_service.service;

import com.aad.microservice.customer_statistics_service.model.CustomerRevenue;
import com.aad.microservice.customer_statistics_service.model.CustomerPayment;
import com.aad.microservice.customer_statistics_service.model.TimeBasedRevenue;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CustomerStatisticsService {

    /**
     * Lấy thống kê doanh thu theo khách hàng trong khoảng thời gian
     *
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return Danh sách thống kê doanh thu theo khách hàng
     */
    List<CustomerRevenue> getCustomerRevenueStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * Lấy danh sách hóa đơn của khách hàng trong khoảng thời gian
     *
     * @param customerId ID của khách hàng
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return Danh sách hóa đơn của khách hàng
     */
    List<CustomerPayment> getCustomerInvoices(Long customerId, LocalDate startDate, LocalDate endDate);

    /**
     * Lấy thống kê doanh thu theo ngày trong khoảng thời gian
     *
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return Danh sách thống kê doanh thu theo ngày
     */
    List<TimeBasedRevenue> getDailyRevenueStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * Lấy thống kê doanh thu theo tuần trong khoảng thời gian
     *
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return Danh sách thống kê doanh thu theo tuần
     */
    List<TimeBasedRevenue> getWeeklyRevenueStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * Lấy thống kê doanh thu theo tháng trong khoảng thời gian
     *
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return Danh sách thống kê doanh thu theo tháng
     */
    List<TimeBasedRevenue> getMonthlyRevenueStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * Lấy thống kê doanh thu theo năm trong khoảng thời gian
     *
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return Danh sách thống kê doanh thu theo năm
     */
    List<TimeBasedRevenue> getYearlyRevenueStatistics(LocalDate startDate, LocalDate endDate);
}
