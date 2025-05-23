package com.aad.microservice.customer_statistics_service.service;

import com.aad.microservice.customer_statistics_service.model.CustomerRevenue;
import com.aad.microservice.customer_statistics_service.model.CustomerPayment;
import com.aad.microservice.customer_statistics_service.model.TimeBasedRevenue;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CustomerStatisticsService {

    List<CustomerRevenue> getCustomerRevenueStatistics(LocalDate startDate, LocalDate endDate);

    List<CustomerPayment> getCustomerInvoices(Long customerId, LocalDate startDate, LocalDate endDate);

    List<TimeBasedRevenue> getDailyRevenueStatistics(LocalDate startDate, LocalDate endDate);

    List<TimeBasedRevenue> getWeeklyRevenueStatistics(LocalDate startDate, LocalDate endDate);

    List<TimeBasedRevenue> getMonthlyRevenueStatistics(LocalDate startDate, LocalDate endDate);

    List<TimeBasedRevenue> getYearlyRevenueStatistics(LocalDate startDate, LocalDate endDate);
}
