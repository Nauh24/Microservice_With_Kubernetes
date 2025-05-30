# Script kiem tra du lieu trong database de tao thanh toan
# Lay thong tin khach hang va hop dong tu API

Write-Host "=== KIEM TRA DU LIEU DATABASE ===" -ForegroundColor Cyan
Write-Host ""

# Cau hinh
$API_GATEWAY_URL = "http://localhost:8080"

$headers = @{
    "Content-Type" = "application/json"
    "Accept" = "application/json"
}

Write-Host "1. Kiem tra ket noi API Gateway..." -ForegroundColor Yellow
try {
    $healthCheck = Invoke-RestMethod -Uri "$API_GATEWAY_URL/actuator/health" -Method GET -Headers $headers -TimeoutSec 10
    Write-Host "API Gateway dang hoat dong" -ForegroundColor Green
} catch {
    Write-Host "Khong the ket noi den API Gateway tai $API_GATEWAY_URL" -ForegroundColor Red
    Write-Host "Vui long dam bao API Gateway dang chay tren port 8080" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "2. Lấy danh sách khách hàng..." -ForegroundColor Yellow
try {
    $customers = Invoke-RestMethod -Uri "$API_GATEWAY_URL/api/customer" -Method GET -Headers $headers
    Write-Host "✅ Tìm thấy $($customers.Count) khách hàng" -ForegroundColor Green

    if ($customers.Count -gt 0) {
        Write-Host ""
        Write-Host "DANH SÁCH KHÁCH HÀNG:" -ForegroundColor Cyan
        Write-Host "ID`tTên`t`t`tCông ty`t`t`tSĐT" -ForegroundColor White
        Write-Host "---`t---`t`t`t---`t`t`t---" -ForegroundColor Gray

        $customers | ForEach-Object {
            $name = if ($_.fullname.Length -gt 15) { $_.fullname.Substring(0, 15) + "..." } else { $_.fullname }
            $company = if ($_.companyName -and $_.companyName.Length -gt 15) { $_.companyName.Substring(0, 15) + "..." } else { if ($_.companyName) { $_.companyName } else { "N/A" } }
            Write-Host "$($_.id)`t$name`t`t$company`t`t$($_.phoneNumber)" -ForegroundColor White
        }
    } else {
        Write-Host "⚠️ Không có khách hàng nào trong database" -ForegroundColor Yellow
    }
} catch {
    Write-Host "❌ Không thể lấy danh sách khách hàng: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "3. Lấy danh sách hợp đồng..." -ForegroundColor Yellow
try {
    $contracts = Invoke-RestMethod -Uri "$API_GATEWAY_URL/api/customer-contract" -Method GET -Headers $headers
    Write-Host "✅ Tìm thấy $($contracts.Count) hợp đồng" -ForegroundColor Green

    if ($contracts.Count -gt 0) {
        Write-Host ""
        Write-Host "DANH SÁCH HỢP ĐỒNG:" -ForegroundColor Cyan
        Write-Host "ID`tKhách hàng ID`tTổng tiền`t`tĐã trả`t`tCòn lại`t`tMô tả" -ForegroundColor White
        Write-Host "---`t---`t`t---`t`t---`t`t---`t`t---" -ForegroundColor Gray

        $contracts | ForEach-Object {
            $totalPaid = if ($_.totalPaid) { $_.totalPaid } else { 0 }
            $remaining = $_.totalAmount - $totalPaid
            $description = if ($_.description -and $_.description.Length -gt 20) { $_.description.Substring(0, 20) + "..." } else { if ($_.description) { $_.description } else { "N/A" } }

            $color = if ($remaining -le 0) { "Green" } elseif ($totalPaid -gt 0) { "Yellow" } else { "White" }
            Write-Host "$($_.id)`t$($_.customerId)`t`t$($_.totalAmount)`t`t$totalPaid`t`t$remaining`t`t$description" -ForegroundColor $color
        }

        Write-Host ""
        Write-Host "Chú thích màu:" -ForegroundColor Cyan
        Write-Host "- Xanh lá: Đã thanh toán đủ" -ForegroundColor Green
        Write-Host "- Vàng: Đã thanh toán một phần" -ForegroundColor Yellow
        Write-Host "- Trắng: Chưa thanh toán" -ForegroundColor White
    } else {
        Write-Host "⚠️ Không có hợp đồng nào trong database" -ForegroundColor Yellow
    }
} catch {
    Write-Host "❌ Không thể lấy danh sách hợp đồng: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "4. Phân tích dữ liệu để tạo thanh toán..." -ForegroundColor Yellow

if ($customers.Count -gt 0 -and $contracts.Count -gt 0) {
    # Nhóm hợp đồng theo khách hàng
    $contractsByCustomer = $contracts | Group-Object -Property customerId

    Write-Host ""
    Write-Host "PHÂN TÍCH THEO KHÁCH HÀNG:" -ForegroundColor Cyan

    foreach ($group in $contractsByCustomer) {
        $customerId = [int]$group.Name
        $customer = $customers | Where-Object { $_.id -eq $customerId }
        $customerName = if ($customer) { $customer.fullname } else { "Không tìm thấy" }

        Write-Host ""
        Write-Host "Khách hàng: $customerName (ID: $customerId)" -ForegroundColor Yellow

        $unpaidContracts = @()
        $partiallyPaidContracts = @()
        $totalUnpaidAmount = 0

        foreach ($contract in $group.Group) {
            $totalPaid = if ($contract.totalPaid) { $contract.totalPaid } else { 0 }
            $remaining = $contract.totalAmount - $totalPaid

            if ($remaining -gt 0) {
                if ($totalPaid -eq 0) {
                    $unpaidContracts += $contract
                } else {
                    $partiallyPaidContracts += $contract
                }
                $totalUnpaidAmount += $remaining
            }

            Write-Host "  - Hợp đồng $($contract.id): $($contract.totalAmount) VNĐ (Đã trả: $totalPaid, Còn lại: $remaining)" -ForegroundColor White
        }

        if ($unpaidContracts.Count -gt 0 -or $partiallyPaidContracts.Count -gt 0) {
            Write-Host "  📊 Tổng kết:" -ForegroundColor Cyan
            Write-Host "    - Hợp đồng chưa thanh toán: $($unpaidContracts.Count)" -ForegroundColor White
            Write-Host "    - Hợp đồng thanh toán một phần: $($partiallyPaidContracts.Count)" -ForegroundColor White
            Write-Host "    - Tổng số tiền còn lại: $totalUnpaidAmount VNĐ" -ForegroundColor White

            # Tạo gợi ý thanh toán
            if (($unpaidContracts.Count + $partiallyPaidContracts.Count) -gt 1) {
                Write-Host "  💡 Có thể tạo thanh toán gộp cho khách hàng này" -ForegroundColor Green

                # Tạo script mẫu
                Write-Host ""
                Write-Host "  📝 Script mẫu cho khách hàng này:" -ForegroundColor Cyan
                Write-Host "  customerId = $customerId" -ForegroundColor Gray

                $sampleContracts = ($unpaidContracts + $partiallyPaidContracts) | Select-Object -First 3
                $sampleTotal = 0

                Write-Host "  contractPayments = @(" -ForegroundColor Gray
                foreach ($contract in $sampleContracts) {
                    $contractTotalPaid = if ($contract.totalPaid) { $contract.totalPaid } else { 0 }
                    $remaining = $contract.totalAmount - $contractTotalPaid
                    $suggestedAmount = [Math]::Min($remaining, [Math]::Floor($remaining * 0.5))
                    $sampleTotal += $suggestedAmount

                    Write-Host "    @`{ contractId = $($contract.id); allocatedAmount = $suggestedAmount `}," -ForegroundColor Gray
                }
                Write-Host "  )" -ForegroundColor Gray
                Write-Host "  totalAmount = $sampleTotal" -ForegroundColor Gray
            }
        } else {
            Write-Host "  ✅ Tất cả hợp đồng đã được thanh toán đủ" -ForegroundColor Green
        }
    }
} else {
    Write-Host "⚠️ Không đủ dữ liệu để phân tích" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "5. Lấy danh sách thanh toán hiện có..." -ForegroundColor Yellow
try {
    $payments = Invoke-RestMethod -Uri "$API_GATEWAY_URL/api/customer-payment" -Method GET -Headers $headers
    Write-Host "✅ Tìm thấy $($payments.Count) thanh toán" -ForegroundColor Green

    if ($payments.Count -gt 0) {
        Write-Host ""
        Write-Host "THANH TOÁN GẦN NHẤT:" -ForegroundColor Cyan
        $recentPayments = $payments | Sort-Object { [DateTime]$_.paymentDate } -Descending | Select-Object -First 5

        $recentPayments | ForEach-Object {
            $paymentDate = [DateTime]$_.paymentDate
            $method = switch ($_.paymentMethod) {
                0 { "Tiền mặt" }
                1 { "Chuyển khoản" }
                2 { "Thẻ tín dụng" }
                default { "Khác" }
            }
            Write-Host "ID: $($_.id) | Ngày: $($paymentDate.ToString('dd/MM/yyyy')) | Số tiền: $($_.paymentAmount) | Phương thức: $method | Khách hàng: $($_.customerId)" -ForegroundColor White
        }
    }
} catch {
    Write-Host "❌ Không thể lấy danh sách thanh toán: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== HOÀN THÀNH KIỂM TRA ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "HƯỚNG DẪN TIẾP THEO:" -ForegroundColor Yellow
Write-Host "1. Sử dụng script 'create_sample_payment.ps1' với dữ liệu mẫu" -ForegroundColor White
Write-Host "2. Hoặc sử dụng script 'create_multiple_contract_payment.ps1' để tự động tạo thanh toán" -ForegroundColor White
Write-Host "3. Sua doi cac ID trong script theo du lieu thuc te o tren" -ForegroundColor White
