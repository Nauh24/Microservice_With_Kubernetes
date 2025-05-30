# Script tạo thanh toán cho nhiều hợp đồng của một khách hàng
# Sử dụng API endpoint: POST /api/customer-payment/multiple-contracts

Write-Host "=== TẠO THANH TOÁN CHO NHIỀU HỢP ĐỒNG ===" -ForegroundColor Cyan
Write-Host ""

# Cấu hình API Gateway URL
$API_GATEWAY_URL = "http://localhost:8080"
$PAYMENT_ENDPOINT = "$API_GATEWAY_URL/api/customer-payment/multiple-contracts"

# Headers cho request
$headers = @{
    "Content-Type" = "application/json"
    "Accept" = "application/json"
}

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
Write-Host "2. Lấy danh sách khách hàng..." -ForegroundColor Yellow
try {
    $customers = Invoke-RestMethod -Uri "$API_GATEWAY_URL/api/customer" -Method GET -Headers $headers
    Write-Host "✅ Tìm thấy $($customers.Count) khách hàng" -ForegroundColor Green

    # Hiển thị danh sách khách hàng
    Write-Host ""
    Write-Host "DANH SÁCH KHÁCH HÀNG:" -ForegroundColor Cyan
    $customers | ForEach-Object {
        Write-Host "ID: $($_.id) | Tên: $($_.fullname) | Công ty: $($_.companyName) | SĐT: $($_.phoneNumber)" -ForegroundColor White
    }
} catch {
    Write-Host "❌ Không thể lấy danh sách khách hàng: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "3. Lấy danh sách hợp đồng..." -ForegroundColor Yellow
try {
    $contracts = Invoke-RestMethod -Uri "$API_GATEWAY_URL/api/customer-contract" -Method GET -Headers $headers
    Write-Host "✅ Tìm thấy $($contracts.Count) hợp đồng" -ForegroundColor Green

    # Hiển thị hợp đồng theo khách hàng
    Write-Host ""
    Write-Host "DANH SÁCH HỢP ĐỒNG THEO KHÁCH HÀNG:" -ForegroundColor Cyan
    $contractsByCustomer = $contracts | Group-Object -Property customerId

    foreach ($group in $contractsByCustomer) {
        $customerId = $group.Name
        $customerName = ($customers | Where-Object { $_.id -eq $customerId }).fullname
        Write-Host ""
        Write-Host "Khách hàng ID $customerId - $customerName:" -ForegroundColor Yellow

        $group.Group | ForEach-Object {
            $totalPaid = if ($_.totalPaid -ne $null) { $_.totalPaid } else { 0 }
            $totalDue = $_.totalAmount - $totalPaid
            Write-Host "  - Hợp đồng ID: $($_.id) | Tổng tiền: $($_.totalAmount) | Đã trả: $totalPaid | Còn lại: $totalDue | Mô tả: $($_.description)" -ForegroundColor White
        }
    }
} catch {
    Write-Host "❌ Không thể lấy danh sách hợp đồng: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "4. Tạo thanh toán mẫu cho nhiều hợp đồng..." -ForegroundColor Yellow

# Tìm khách hàng có nhiều hợp đồng
$customerWithMultipleContracts = $contractsByCustomer | Where-Object { $_.Group.Count -gt 1 } | Select-Object -First 1

if (-not $customerWithMultipleContracts) {
    Write-Host "❌ Không tìm thấy khách hàng nào có nhiều hợp đồng" -ForegroundColor Red
    Write-Host "Tạo thanh toán cho hợp đồng đầu tiên..." -ForegroundColor Yellow

    # Lấy hợp đồng đầu tiên
    $firstContract = $contracts | Select-Object -First 1
    if (-not $firstContract) {
        Write-Host "❌ Không có hợp đồng nào trong hệ thống" -ForegroundColor Red
        exit 1
    }

    $selectedCustomerId = $firstContract.customerId
    $selectedContracts = @($firstContract)
} else {
    $selectedCustomerId = [int]$customerWithMultipleContracts.Name
    $selectedContracts = $customerWithMultipleContracts.Group | Select-Object -First 3  # Lấy tối đa 3 hợp đồng
}

$customerName = ($customers | Where-Object { $_.id -eq $selectedCustomerId }).fullname

Write-Host ""
Write-Host "Tạo thanh toán cho khách hàng: $customerName (ID: $selectedCustomerId)" -ForegroundColor Cyan
Write-Host "Số hợp đồng được thanh toán: $($selectedContracts.Count)" -ForegroundColor Cyan

# Tạo danh sách thanh toán cho từng hợp đồng
$contractPayments = @()
$totalPaymentAmount = 0

foreach ($contract in $selectedContracts) {
    $contractTotalPaid = if ($contract.totalPaid -ne $null) { $contract.totalPaid } else { 0 }
    $remainingAmount = $contract.totalAmount - $contractTotalPaid
    $paymentAmount = [Math]::Min($remainingAmount, $remainingAmount * 0.5)  # Thanh toán 50% số tiền còn lại

    if ($paymentAmount -gt 0) {
        $contractPayments += @{
            contractId = $contract.id
            allocatedAmount = $paymentAmount
        }
        $totalPaymentAmount += $paymentAmount

        Write-Host "  - Hợp đồng $($contract.id): Thanh toán $paymentAmount VNĐ" -ForegroundColor White
    }
}

if ($contractPayments.Count -eq 0) {
    Write-Host "❌ Không có hợp đồng nào cần thanh toán" -ForegroundColor Red
    exit 1
}

# Tạo request body
$paymentRequest = @{
    customerId = $selectedCustomerId
    totalAmount = $totalPaymentAmount
    paymentMethod = 1  # Chuyển khoản
    note = "Thanh toán tự động cho $($contractPayments.Count) hợp đồng - $(Get-Date -Format 'dd/MM/yyyy HH:mm')"
    contractPayments = $contractPayments
}

$jsonBody = $paymentRequest | ConvertTo-Json -Depth 3

Write-Host ""
Write-Host "5. Gửi request tạo thanh toán..." -ForegroundColor Yellow
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

    if ($response.contractPayments) {
        Write-Host ""
        Write-Host "Chi tiết thanh toán theo hợp đồng:" -ForegroundColor Cyan
        $response.contractPayments | ForEach-Object {
            Write-Host "  - Hợp đồng $($_.contractId): $($_.allocatedAmount) VNĐ" -ForegroundColor White
        }
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
}

Write-Host ""
Write-Host "=== HOÀN THÀNH ===" -ForegroundColor Cyan
