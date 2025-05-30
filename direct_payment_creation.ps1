# Script tao thanh toan truc tiep cho nhieu hop dong
# Su dung du lieu co dinh, khong can kiem tra truoc

Write-Host "=== TAO THANH TOAN TRUC TIEP ===" -ForegroundColor Cyan
Write-Host ""

# Cau hinh API
$API_GATEWAY_URL = "http://localhost:8080"
$PAYMENT_ENDPOINT = "$API_GATEWAY_URL/api/customer-payment/multiple-contracts"

$headers = @{
    "Content-Type" = "application/json"
    "Accept" = "application/json"
}

# Du lieu thanh toan mau - Thay doi theo du lieu thuc te
$paymentData = @{
    customerId = 1
    totalAmount = 15000000
    paymentMethod = 1
    note = "Thanh toan gop cho nhieu hop dong - $(Get-Date -Format 'dd/MM/yyyy HH:mm')"
    contractPayments = @(
        @{
            contractId = 1
            allocatedAmount = 5000000
        },
        @{
            contractId = 2
            allocatedAmount = 7000000
        },
        @{
            contractId = 3
            allocatedAmount = 3000000
        }
    )
}

Write-Host "Thong tin thanh toan se duoc tao:" -ForegroundColor Yellow
Write-Host "- Khach hang ID: $($paymentData.customerId)" -ForegroundColor White
Write-Host "- Tong so tien: $($paymentData.totalAmount) VND" -ForegroundColor White
Write-Host "- Phuong thuc: Chuyen khoan" -ForegroundColor White
Write-Host "- So hop dong: $($paymentData.contractPayments.Count)" -ForegroundColor White

Write-Host ""
Write-Host "Chi tiet thanh toan theo hop dong:" -ForegroundColor Yellow
foreach ($cp in $paymentData.contractPayments) {
    Write-Host "  - Hop dong $($cp.contractId): $($cp.allocatedAmount) VND" -ForegroundColor White
}

Write-Host ""
$confirm = Read-Host "Ban co muon tiep tuc tao thanh toan nay? (y/n)"
if ($confirm -ne 'y' -and $confirm -ne 'Y') {
    Write-Host "Da huy tao thanh toan." -ForegroundColor Yellow
    exit 0
}

Write-Host ""
Write-Host "Dang gui request tao thanh toan..." -ForegroundColor Yellow

$jsonBody = $paymentData | ConvertTo-Json -Depth 3
Write-Host ""
Write-Host "Request JSON:" -ForegroundColor Cyan
Write-Host $jsonBody -ForegroundColor Gray

Write-Host ""
Write-Host "Gui request den: $PAYMENT_ENDPOINT" -ForegroundColor Yellow

try {
    $response = Invoke-RestMethod -Uri $PAYMENT_ENDPOINT -Method POST -Body $jsonBody -Headers $headers -TimeoutSec 30
    
    Write-Host ""
    Write-Host "THANH TOAN DA DUOC TAO THANH CONG!" -ForegroundColor Green
    Write-Host "=================================" -ForegroundColor Green
    Write-Host "Payment ID: $($response.id)" -ForegroundColor Yellow
    Write-Host "Tong so tien: $($response.paymentAmount) VND" -ForegroundColor Yellow
    Write-Host "Phuong thuc: $(if($response.paymentMethod -eq 1){'Chuyen khoan'}else{'Khac'})" -ForegroundColor Yellow
    Write-Host "Ghi chu: $($response.note)" -ForegroundColor Yellow
    Write-Host "Ngay thanh toan: $($response.paymentDate)" -ForegroundColor Yellow
    Write-Host "Khach hang ID: $($response.customerId)" -ForegroundColor Yellow
    
    if ($response.contractPayments) {
        Write-Host ""
        Write-Host "Chi tiet thanh toan theo hop dong:" -ForegroundColor Cyan
        foreach ($cp in $response.contractPayments) {
            Write-Host "  - Hop dong $($cp.contractId): $($cp.allocatedAmount) VND" -ForegroundColor White
        }
    }
    
    Write-Host ""
    Write-Host "Thanh toan da duoc luu vao database thanh cong!" -ForegroundColor Green
    
} catch {
    Write-Host ""
    Write-Host "LOI KHI TAO THANH TOAN!" -ForegroundColor Red
    Write-Host "======================" -ForegroundColor Red
    Write-Host "Loi: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode
        Write-Host "HTTP Status Code: $statusCode" -ForegroundColor Red
        
        try {
            $errorStream = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($errorStream)
            $errorBody = $reader.ReadToEnd()
            Write-Host "Chi tiet loi: $errorBody" -ForegroundColor Red
        } catch {
            Write-Host "Khong the doc chi tiet loi tu response" -ForegroundColor Red
        }
    }
    
    Write-Host ""
    Write-Host "Cac nguyen nhan co the:" -ForegroundColor Yellow
    Write-Host "1. API Gateway khong chay (port 8080)" -ForegroundColor White
    Write-Host "2. Customer Payment Service khong chay (port 8084)" -ForegroundColor White
    Write-Host "3. Khach hang hoac hop dong khong ton tai trong database" -ForegroundColor White
    Write-Host "4. So tien thanh toan vuot qua so tien con lai cua hop dong" -ForegroundColor White
    Write-Host "5. Database connection loi" -ForegroundColor White
    
    Write-Host ""
    Write-Host "Huong dan khac phuc:" -ForegroundColor Yellow
    Write-Host "1. Khoi dong Docker Desktop" -ForegroundColor White
    Write-Host "2. Chay: docker-compose up -d" -ForegroundColor White
    Write-Host "3. Doi 1-2 phut de cac service khoi dong" -ForegroundColor White
    Write-Host "4. Kiem tra lai du lieu trong database" -ForegroundColor White
}

Write-Host ""
Write-Host "=== HOAN THANH ===" -ForegroundColor Cyan

Write-Host ""
Write-Host "HUONG DAN SUA DOI DU LIEU:" -ForegroundColor Cyan
Write-Host "De su dung voi du lieu thuc te, hay sua doi cac gia tri sau:" -ForegroundColor Yellow
Write-Host "- customerId: ID cua khach hang co trong database" -ForegroundColor White
Write-Host "- contractPayments.contractId: ID cua cac hop dong thuoc ve khach hang do" -ForegroundColor White
Write-Host "- allocatedAmount: So tien thanh toan cho tung hop dong" -ForegroundColor White
Write-Host "- totalAmount: Tong so tien (phai bang tong allocatedAmount)" -ForegroundColor White
