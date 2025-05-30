# Script tạo thanh toán mẫu cho nhiều hợp đồng với dữ liệu cố định
# Sử dụng API endpoint: POST /api/customer-payment/multiple-contracts

Write-Host "=== TẠO THANH TOÁN MẪU CHO NHIỀU HỢP ĐỒNG ===" -ForegroundColor Cyan
Write-Host ""

# Cấu hình
$API_GATEWAY_URL = "http://localhost:8080"
$PAYMENT_ENDPOINT = "$API_GATEWAY_URL/api/customer-payment/multiple-contracts"

$headers = @{
    "Content-Type" = "application/json"
    "Accept" = "application/json"
}

# Dữ liệu thanh toán mẫu - Thay đổi các ID này theo dữ liệu thực tế trong database
$samplePaymentRequest = @{
    customerId = 1  # ID khách hàng
    totalAmount = 15000000  # Tổng số tiền thanh toán (15 triệu VNĐ)
    paymentMethod = 1  # 0: Tiền mặt, 1: Chuyển khoản, 2: Thẻ tín dụng
    note = "Thanh toán gộp cho 3 hợp đồng - $(Get-Date -Format 'dd/MM/yyyy HH:mm')"
    contractPayments = @(
        @{
            contractId = 1  # ID hợp đồng 1
            allocatedAmount = 5000000  # 5 triệu VNĐ
        },
        @{
            contractId = 2  # ID hợp đồng 2  
            allocatedAmount = 7000000  # 7 triệu VNĐ
        },
        @{
            contractId = 3  # ID hợp đồng 3
            allocatedAmount = 3000000  # 3 triệu VNĐ
        }
    )
}

Write-Host "Thông tin thanh toán sẽ được tạo:" -ForegroundColor Yellow
Write-Host "- Khách hàng ID: $($samplePaymentRequest.customerId)" -ForegroundColor White
Write-Host "- Tổng số tiền: $($samplePaymentRequest.totalAmount) VNĐ" -ForegroundColor White
Write-Host "- Phương thức: $(if($samplePaymentRequest.paymentMethod -eq 0){'Tiền mặt'}elseif($samplePaymentRequest.paymentMethod -eq 1){'Chuyển khoản'}else{'Khác'})" -ForegroundColor White
Write-Host "- Số hợp đồng: $($samplePaymentRequest.contractPayments.Count)" -ForegroundColor White

Write-Host ""
Write-Host "Chi tiết thanh toán theo hợp đồng:" -ForegroundColor Yellow
$samplePaymentRequest.contractPayments | ForEach-Object {
    Write-Host "  - Hợp đồng $($_.contractId): $($_.allocatedAmount) VNĐ" -ForegroundColor White
}

Write-Host ""
$confirm = Read-Host "Bạn có muốn tiếp tục tạo thanh toán này? (y/n)"
if ($confirm -ne 'y' -and $confirm -ne 'Y') {
    Write-Host "Đã hủy tạo thanh toán." -ForegroundColor Yellow
    exit 0
}

Write-Host ""
Write-Host "1. Kiểm tra kết nối API Gateway..." -ForegroundColor Yellow
try {
    $healthCheck = Invoke-RestMethod -Uri "$API_GATEWAY_URL/actuator/health" -Method GET -Headers $headers -TimeoutSec 10
    Write-Host "✅ API Gateway đang hoạt động" -ForegroundColor Green
} catch {
    Write-Host "❌ Không thể kết nối đến API Gateway tại $API_GATEWAY_URL" -ForegroundColor Red
    Write-Host "Vui lòng đảm bảo API Gateway đang chạy trên port 8080" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "2. Gửi request tạo thanh toán..." -ForegroundColor Yellow

$jsonBody = $samplePaymentRequest | ConvertTo-Json -Depth 3
Write-Host "Request Body:" -ForegroundColor Cyan
Write-Host $jsonBody -ForegroundColor Gray

try {
    $response = Invoke-RestMethod -Uri $PAYMENT_ENDPOINT -Method POST -Body $jsonBody -Headers $headers
    
    Write-Host ""
    Write-Host "✅ THANH TOÁN ĐÃ ĐƯỢC TẠO THÀNH CÔNG!" -ForegroundColor Green
    Write-Host "Payment ID: $($response.id)" -ForegroundColor Yellow
    Write-Host "Tổng số tiền: $($response.paymentAmount) VNĐ" -ForegroundColor Yellow
    Write-Host "Phương thức: $(if($response.paymentMethod -eq 0){'Tiền mặt'}elseif($response.paymentMethod -eq 1){'Chuyển khoản'}else{'Khác'})" -ForegroundColor Yellow
    Write-Host "Ghi chú: $($response.note)" -ForegroundColor Yellow
    Write-Host "Ngày thanh toán: $($response.paymentDate)" -ForegroundColor Yellow
    Write-Host "Khách hàng ID: $($response.customerId)" -ForegroundColor Yellow
    
    if ($response.contractPayments) {
        Write-Host ""
        Write-Host "Chi tiết thanh toán theo hợp đồng:" -ForegroundColor Cyan
        $response.contractPayments | ForEach-Object {
            Write-Host "  - Hợp đồng $($_.contractId): $($_.allocatedAmount) VNĐ" -ForegroundColor White
        }
    }
    
    Write-Host ""
    Write-Host "3. Kiểm tra thanh toán đã tạo..." -ForegroundColor Yellow
    try {
        $createdPayment = Invoke-RestMethod -Uri "$API_GATEWAY_URL/api/customer-payment/$($response.id)" -Method GET -Headers $headers
        Write-Host "✅ Xác nhận thanh toán đã được lưu trong database" -ForegroundColor Green
    } catch {
        Write-Host "⚠️ Không thể xác nhận thanh toán trong database" -ForegroundColor Yellow
    }
    
} catch {
    Write-Host ""
    Write-Host "❌ LỖI KHI TẠO THANH TOÁN:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode
        Write-Host "Status Code: $statusCode" -ForegroundColor Red
        
        try {
            $errorResponse = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($errorResponse)
            $errorBody = $reader.ReadToEnd()
            Write-Host "Error Response: $errorBody" -ForegroundColor Red
        } catch {
            Write-Host "Không thể đọc chi tiết lỗi từ response" -ForegroundColor Red
        }
    }
    
    Write-Host ""
    Write-Host "Gợi ý khắc phục:" -ForegroundColor Yellow
    Write-Host "1. Kiểm tra các ID khách hàng và hợp đồng có tồn tại trong database" -ForegroundColor White
    Write-Host "2. Đảm bảo các microservice đang chạy (customer-payment-service, customer-contract-service)" -ForegroundColor White
    Write-Host "3. Kiểm tra số tiền thanh toán không vượt quá số tiền còn lại của hợp đồng" -ForegroundColor White
    Write-Host "4. Kiểm tra database connection và table structure" -ForegroundColor White
}

Write-Host ""
Write-Host "=== HOÀN THÀNH ===" -ForegroundColor Cyan

# Hiển thị hướng dẫn sử dụng
Write-Host ""
Write-Host "HƯỚNG DẪN SỬA ĐỔI DỮ LIỆU:" -ForegroundColor Cyan
Write-Host "Để sử dụng với dữ liệu thực tế, hãy sửa đổi các giá trị sau trong script:" -ForegroundColor Yellow
Write-Host "- customerId: ID của khách hàng có trong database" -ForegroundColor White
Write-Host "- contractPayments.contractId: ID của các hợp đồng thuộc về khách hàng đó" -ForegroundColor White
Write-Host "- allocatedAmount: Số tiền thanh toán cho từng hợp đồng" -ForegroundColor White
Write-Host "- totalAmount: Tổng số tiền (phải bằng tổng allocatedAmount)" -ForegroundColor White
