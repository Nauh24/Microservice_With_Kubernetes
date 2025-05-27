-- Database Setup Script for Microservices
-- This script clears existing data and sets up Vietnamese job categories
-- Note: Run this script as postgres user

-- First, ensure databases exist (run these commands separately if needed)
-- CREATE DATABASE customerdb;
-- CREATE DATABASE jobdb;
-- CREATE DATABASE customercontractdb;
-- CREATE DATABASE customerpaymentdb;

-- Connect to jobdb to clear and setup job categories
\c jobdb;

-- Clear existing job/work data
TRUNCATE TABLE IF EXISTS work_shifts CASCADE;
TRUNCATE TABLE IF EXISTS job_details CASCADE;
TRUNCATE TABLE IF EXISTS jobs CASCADE;

-- Clear and reset job categories
TRUNCATE TABLE IF EXISTS job_categories CASCADE;

-- Insert Vietnamese job categories
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
('Công nhân phụ việc', 'Hỗ trợ các công việc phụ tại công trình', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Nhân viên văn phòng', 'Công việc hành chính, văn thư tại công trình', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Kỹ thuật viên', 'Hỗ trợ kỹ thuật, giám sát chất lượng công trình', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Thợ máy', 'Vận hành máy móc thiết bị xây dựng', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Công nhân an toàn', 'Đảm bảo an toàn lao động tại công trình', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Thợ cắt gạch', 'Cắt, gia công gạch đá cho công trình', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

-- Connect to customercontractdb to clear contract data
\c customercontractdb;

-- Clear existing contract data
TRUNCATE TABLE IF EXISTS work_shifts CASCADE;
TRUNCATE TABLE IF EXISTS job_details CASCADE;
TRUNCATE TABLE IF EXISTS customer_contracts CASCADE;

-- Connect to customerpaymentdb to clear payment data
\c customerpaymentdb;

-- Clear existing payment data
TRUNCATE TABLE IF EXISTS customer_payments CASCADE;

-- Connect to customerdb to clear customer data (optional - keep existing customers)
\c customerdb;

-- Optionally clear customer data (uncomment if needed)
-- TRUNCATE TABLE IF EXISTS customers CASCADE;

-- Display completion message
SELECT 'Database setup completed successfully!' as status;
