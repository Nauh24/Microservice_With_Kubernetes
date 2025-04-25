-- Cập nhật schema cơ sở dữ liệu để phản ánh thay đổi từ fullName sang fullname
-- Đầu tiên, thêm cột fullname mới
ALTER TABLE customers ADD COLUMN IF NOT EXISTS fullname VARCHAR(255);

-- Sao chép dữ liệu từ cột fullName sang cột fullname
UPDATE customers SET fullname = "fullName" WHERE fullname IS NULL;

-- Xóa cột fullName cũ (chỉ thực hiện sau khi đã sao chép dữ liệu thành công)
-- ALTER TABLE customers DROP COLUMN IF EXISTS "fullName";
