# Script đơn giản để cập nhật danh mục công việc
Write-Host "=== CẬP NHẬT DANH MỤC CÔNG VIỆC ===" -ForegroundColor Green
Write-Host ""

Write-Host "Đang sao chép file SQL vào container..." -ForegroundColor Yellow
docker cp update-job-categories.sql job-service:/tmp/update-job-categories.sql

Write-Host "Đang chạy script SQL..." -ForegroundColor Yellow
docker exec -e PGPASSWORD=1234 job-service psql -h host.docker.internal -U postgres -d jobdb -f /tmp/update-job-categories.sql

Write-Host ""
Write-Host "=== HOÀN THÀNH ===" -ForegroundColor Green
