# Script để cập nhật danh mục công việc bằng tiếng Việt
Write-Host "=== CẬP NHẬT DANH MỤC CÔNG VIỆC BẰNG TIẾNG VIỆT ===" -ForegroundColor Green
Write-Host ""

# Kiểm tra kết nối PostgreSQL
Write-Host "Kiểm tra kết nối PostgreSQL..." -ForegroundColor Yellow
try {
    $connection = Test-NetConnection -ComputerName localhost -Port 5432 -WarningAction SilentlyContinue
    if ($connection.TcpTestSucceeded) {
        Write-Host "✓ PostgreSQL đang chạy trên localhost:5432" -ForegroundColor Green
    } else {
        Write-Host "✗ PostgreSQL không chạy trên localhost:5432" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "✗ Lỗi khi kiểm tra PostgreSQL" -ForegroundColor Red
    exit 1
}

# Sử dụng Docker để chạy psql
Write-Host ""
Write-Host "Đang cập nhật danh mục công việc..." -ForegroundColor Yellow

# Sao chép file SQL vào container job-service
docker cp update-job-categories.sql job-service:/tmp/update-job-categories.sql

# Chạy script SQL từ trong container
$env:PGPASSWORD = "1234"
$result = docker exec job-service psql -h host.docker.internal -U postgres -d jobdb -f /tmp/update-job-categories.sql

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Cập nhật danh mục công việc thành công!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Kết quả:" -ForegroundColor Cyan
    Write-Host $result
} else {
    Write-Host "✗ Lỗi khi cập nhật danh mục công việc" -ForegroundColor Red
    Write-Host $result
}

Write-Host ""
Write-Host "=== HOÀN THÀNH CẬP NHẬT ===" -ForegroundColor Green
