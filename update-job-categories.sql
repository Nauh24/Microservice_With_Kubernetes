-- Script để cập nhật danh mục công việc bằng tiếng Việt
-- Chạy script này trong database 'jobdb'

-- Xóa tất cả dữ liệu cũ trong bảng job_categories
DELETE FROM job_categories;

-- Reset sequence để ID bắt đầu từ 1
ALTER SEQUENCE job_categories_id_seq RESTART WITH 1;

-- Thêm các danh mục công việc bằng tiếng Việt
INSERT INTO job_categories (name, description, is_deleted, created_at, updated_at) VALUES
('Công nhân xây dựng', 'Công việc liên quan đến xây dựng, thi công các công trình dân dụng và công nghiệp', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Thợ điện', 'Lắp đặt, sửa chữa và bảo trì hệ thống điện trong các công trình xây dựng', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Thợ nước', 'Lắp đặt, sửa chữa hệ thống cấp thoát nước, đường ống và thiết bị vệ sinh', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Thợ hàn', 'Hàn các kết cấu thép, kim loại trong xây dựng và sản xuất', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Thợ sơn', 'Sơn tường, sơn kết cấu và hoàn thiện bề mặt các công trình xây dựng', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Công nhân vận chuyển', 'Vận chuyển vật liệu, hàng hóa và thiết bị tại công trình', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Thợ mộc', 'Làm đồ gỗ, ván khuôn, cốp pha và các sản phẩm gỗ cho xây dựng', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Thợ ốp lát', 'Ốp lát gạch, đá, ceramic cho sàn nhà và tường các công trình', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Công nhân dọn dẹp', 'Dọn dẹp vệ sinh công trình, khu vực làm việc và môi trường xung quanh', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Thợ cơ khí', 'Sửa chữa, bảo trì máy móc thiết bị xây dựng và công nghiệp', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Công nhân bảo vệ', 'Bảo vệ an ninh, trật tự tại các công trình xây dựng và khu vực làm việc', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Thợ làm vườn', 'Chăm sóc cây xanh, thiết kế và thi công cảnh quan', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Công nhân kho bãi', 'Quản lý, sắp xếp và bảo quản vật tư, thiết bị trong kho', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Thợ lái xe', 'Lái xe tải, xe máy xúc, xe cần cẩu và các phương tiện tại công trình', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Công nhân phụ việc', 'Hỗ trợ các công việc phụ và lao động phổ thông tại công trình', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Nhân viên văn phòng', 'Công việc hành chính, văn thư, kế toán tại công trình và văn phòng', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Kỹ thuật viên', 'Hỗ trợ kỹ thuật, giám sát chất lượng và an toàn công trình', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Thợ máy', 'Vận hành máy móc thiết bị xây dựng và sản xuất chuyên nghiệp', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Công nhân an toàn', 'Đảm bảo an toàn lao động và phòng chống cháy nổ tại công trình', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Thợ cắt gạch', 'Cắt, gia công gạch đá và vật liệu xây dựng theo yêu cầu kỹ thuật', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Thợ trát tường', 'Trát tường, làm phẳng bề mặt và hoàn thiện các công trình xây dựng', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Công nhân đổ bê tông', 'Trộn, vận chuyển và đổ bê tông cho các công trình xây dựng', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Thợ lợp mái', 'Lợp mái ngói, tôn và các vật liệu lợp mái cho công trình', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Công nhân nông nghiệp', 'Trồng trọt, chăn nuôi và các công việc liên quan đến nông nghiệp', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Thợ sửa chữa', 'Sửa chữa thiết bị, máy móc và các vật dụng trong gia đình, công sở', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Hiển thị kết quả
SELECT COUNT(*) as total_categories FROM job_categories;
SELECT id, name, description FROM job_categories ORDER BY id;
