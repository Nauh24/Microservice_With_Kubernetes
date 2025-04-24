package com.aad.microservice.customer_payment_service.util;

import com.aad.microservice.customer_payment_service.model.PaymentPeriod;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class PaymentPeriodUtil {
    
    /**
     * Lấy kỳ thanh toán hiện tại (tuần hiện tại)
     */
    public static PaymentPeriod getCurrentPaymentPeriod() {
        LocalDate today = LocalDate.now();
        
        // Lấy thông tin tuần, tháng, năm hiện tại
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int weekNumber = today.get(weekFields.weekOfMonth());
        int monthNumber = today.getMonthValue();
        int year = today.getYear();
        
        // Tính ngày bắt đầu tuần (thứ 2)
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        
        // Tính ngày kết thúc tuần (chủ nhật)
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        
        return PaymentPeriod.builder()
                .weekNumber(weekNumber)
                .monthNumber(monthNumber)
                .year(year)
                .startDate(startOfWeek)
                .endDate(endOfWeek)
                .build();
    }
    
    /**
     * Lấy kỳ thanh toán theo ngày cụ thể
     */
    public static PaymentPeriod getPaymentPeriodByDate(LocalDate date) {
        // Lấy thông tin tuần, tháng, năm của ngày cụ thể
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int weekNumber = date.get(weekFields.weekOfMonth());
        int monthNumber = date.getMonthValue();
        int year = date.getYear();
        
        // Tính ngày bắt đầu tuần (thứ 2)
        LocalDate startOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        
        // Tính ngày kết thúc tuần (chủ nhật)
        LocalDate endOfWeek = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        
        return PaymentPeriod.builder()
                .weekNumber(weekNumber)
                .monthNumber(monthNumber)
                .year(year)
                .startDate(startOfWeek)
                .endDate(endOfWeek)
                .build();
    }
}
