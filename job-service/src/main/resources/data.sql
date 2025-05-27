-- Insert sample job categories into jobdb database
-- This file will be executed automatically by Spring Boot on startup

INSERT INTO job_categories (name, description, is_deleted, created_at, updated_at) VALUES
('Công nhân xây dựng', 'Công việc liên quan đến xây dựng, thi công các công trình', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Thợ điện', 'Lắp đặt, sửa chữa hệ thống điện trong các công trình', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Thợ nước', 'Lắp đặt, sửa chữa hệ thống cấp thoát nước', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Thợ hàn', 'Hàn các kết cấu thép, kim loại trong xây dựng', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Thợ sơn', 'Sơn tường, sơn kết cấu các công trình xây dựng', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Công nhân vận chuyển', 'Vận chuyển vật liệu, hàng hóa tại công trình', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Thợ mộc', 'Làm đồ gỗ, ván khuôn, cốp pha cho xây dựng', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Thợ ốp lát', 'Ốp lát gạch, đá cho các công trình', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Công nhân dọn dẹp', 'Dọn dẹp vệ sinh công trình, khu vực làm việc', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Thợ cơ khí', 'Sửa chữa, bảo trì máy móc thiết bị xây dựng', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Công nhân bảo vệ', 'Bảo vệ an ninh tại các công trình xây dựng', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Thợ làm vườn', 'Chăm sóc cây xanh, thiết kế cảnh quan', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Công nhân kho bãi', 'Quản lý, sắp xếp vật tư trong kho', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Thợ lái xe', 'Lái xe tải, xe máy xúc tại công trình', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Công nhân phụ việc', 'Hỗ trợ các công việc phụ tại công trình', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;
