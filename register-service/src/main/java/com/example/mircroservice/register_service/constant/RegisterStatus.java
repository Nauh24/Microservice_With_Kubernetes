package com.example.mircroservice.register_service.constant;

public enum RegisterStatus
{
    AwaitingApproved, // Chờ quản lý duyệt 
    Approved, // Quản lý xác nhận yêu cầu đăng ký
    Canceled, // Quản lý hủy yêu cầu đăng ký
}