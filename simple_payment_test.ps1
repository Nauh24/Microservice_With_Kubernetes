# Script don gian de kiem tra va tao thanh toan cho nhieu hop dong

Write-Host "=== KIEM TRA VA TAO THANH TOAN ===" -ForegroundColor Cyan
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
    Write-Host "Khong the ket noi den API Gateway" -ForegroundColor Red
    Write-Host "Vui long dam bao API Gateway dang chay tren port 8080" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "2. Lay danh sach khach hang..." -ForegroundColor Yellow
try {
    $customers = Invoke-RestMethod -Uri "$API_GATEWAY_URL/api/customer" -Method GET -Headers $headers
    Write-Host "Tim thay $($customers.Count) khach hang" -ForegroundColor Green
    
    if ($customers.Count -gt 0) {
        Write-Host ""
        Write-Host "DANH SACH KHACH HANG:" -ForegroundColor Cyan
        for ($i = 0; $i -lt [Math]::Min(5, $customers.Count); $i++) {
            $customer = $customers[$i]
            Write-Host "ID: $($customer.id) | Ten: $($customer.fullname) | SDT: $($customer.phoneNumber)" -ForegroundColor White
        }
    }
} catch {
    Write-Host "Khong the lay danh sach khach hang: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "3. Lay danh sach hop dong..." -ForegroundColor Yellow
try {
    $contracts = Invoke-RestMethod -Uri "$API_GATEWAY_URL/api/customer-contract" -Method GET -Headers $headers
    Write-Host "Tim thay $($contracts.Count) hop dong" -ForegroundColor Green
    
    if ($contracts.Count -gt 0) {
        Write-Host ""
        Write-Host "DANH SACH HOP DONG:" -ForegroundColor Cyan
        for ($i = 0; $i -lt [Math]::Min(5, $contracts.Count); $i++) {
            $contract = $contracts[$i]
            $totalPaid = if ($contract.totalPaid) { $contract.totalPaid } else { 0 }
            $remaining = $contract.totalAmount - $totalPaid
            Write-Host "ID: $($contract.id) | Khach hang: $($contract.customerId) | Tong tien: $($contract.totalAmount) | Con lai: $remaining" -ForegroundColor White
        }
    }
} catch {
    Write-Host "Khong the lay danh sach hop dong: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

if ($customers.Count -eq 0 -or $contracts.Count -eq 0) {
    Write-Host ""
    Write-Host "Khong co du lieu de tao thanh toan" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "4. Tao thanh toan mau..." -ForegroundColor Yellow

# Tim khach hang dau tien co hop dong
$firstCustomer = $customers[0]
$customerContracts = $contracts | Where-Object { $_.customerId -eq $firstCustomer.id }

if ($customerContracts.Count -eq 0) {
    # Neu khach hang dau tien khong co hop dong, lay hop dong dau tien
    $firstContract = $contracts[0]
    $customerId = $firstContract.customerId
    $customerContracts = @($firstContract)
} else {
    $customerId = $firstCustomer.id
}

# Lay toi da 3 hop dong dau tien
$selectedContracts = $customerContracts | Select-Object -First 3

Write-Host "Khach hang ID: $customerId" -ForegroundColor Cyan
Write-Host "So hop dong: $($selectedContracts.Count)" -ForegroundColor Cyan

# Tao danh sach thanh toan
$contractPayments = @()
$totalAmount = 0

foreach ($contract in $selectedContracts) {
    $totalPaid = if ($contract.totalPaid) { $contract.totalPaid } else { 0 }
    $remaining = $contract.totalAmount - $totalPaid
    
    if ($remaining -gt 0) {
        # Thanh toan 50% so tien con lai hoac toi thieu 1 trieu
        $paymentAmount = [Math]::Max(1000000, [Math]::Floor($remaining * 0.5))
        $paymentAmount = [Math]::Min($paymentAmount, $remaining)
        
        $contractPayments += @{
            contractId = $contract.id
            allocatedAmount = $paymentAmount
        }
        $totalAmount += $paymentAmount
        
        Write-Host "Hop dong $($contract.id): Thanh toan $paymentAmount VND" -ForegroundColor White
    }
}

if ($contractPayments.Count -eq 0) {
    Write-Host "Khong co hop dong nao can thanh toan" -ForegroundColor Yellow
    exit 1
}

# Tao request
$paymentRequest = @{
    customerId = $customerId
    totalAmount = $totalAmount
    paymentMethod = 1
    note = "Thanh toan tu dong cho $($contractPayments.Count) hop dong - $(Get-Date -Format 'dd/MM/yyyy HH:mm')"
    contractPayments = $contractPayments
}

$jsonBody = $paymentRequest | ConvertTo-Json -Depth 3

Write-Host ""
Write-Host "5. Gui request tao thanh toan..." -ForegroundColor Yellow
Write-Host "Tong so tien: $totalAmount VND" -ForegroundColor Cyan

try {
    $response = Invoke-RestMethod -Uri "$API_GATEWAY_URL/api/customer-payment/multiple-contracts" -Method POST -Body $jsonBody -Headers $headers
    
    Write-Host ""
    Write-Host "THANH TOAN DA DUOC TAO THANH CONG!" -ForegroundColor Green
    Write-Host "Payment ID: $($response.id)" -ForegroundColor Yellow
    Write-Host "Tong so tien: $($response.paymentAmount) VND" -ForegroundColor Yellow
    Write-Host "Phuong thuc: Chuyen khoan" -ForegroundColor Yellow
    Write-Host "Ngay thanh toan: $($response.paymentDate)" -ForegroundColor Yellow
    
    if ($response.contractPayments) {
        Write-Host ""
        Write-Host "Chi tiet thanh toan:" -ForegroundColor Cyan
        foreach ($cp in $response.contractPayments) {
            Write-Host "Hop dong $($cp.contractId): $($cp.allocatedAmount) VND" -ForegroundColor White
        }
    }
    
} catch {
    Write-Host ""
    Write-Host "LOI KHI TAO THANH TOAN:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode
        Write-Host "Status Code: $statusCode" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "=== HOAN THANH ===" -ForegroundColor Cyan
