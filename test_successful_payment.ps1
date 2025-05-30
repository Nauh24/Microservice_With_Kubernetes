# Test successful multiple contracts payment
Write-Host "Testing SUCCESSFUL Multiple Contracts Payment..." -ForegroundColor Green

# Sử dụng contracts có remaining amount > 0
$requestBody = @{
    customerId = 1
    totalAmount = 30000000
    paymentMethod = 0
    note = "Successful multiple contracts payment test"
    contractPayments = @(
        @{
            contractId = 32  # Remaining: 10,000,000
            allocatedAmount = 10000000
        },
        @{
            contractId = 41  # Remaining: 12,000,000
            allocatedAmount = 12000000
        },
        @{
            contractId = 42  # Remaining: 12,000,000
            allocatedAmount = 8000000
        }
    )
}

$jsonBody = $requestBody | ConvertTo-Json -Depth 3
Write-Host "Request Body:" -ForegroundColor Cyan
Write-Host $jsonBody

try {
    Write-Host "`nSending payment request..." -ForegroundColor Yellow
    $payment = Invoke-RestMethod -Uri "http://localhost:8084/api/customer-payment/multiple-contracts" -Method Post -Body $jsonBody -ContentType "application/json"
    
    Write-Host "`n🎉 SUCCESS! Payment created with ID: $($payment.id)" -ForegroundColor Green
    Write-Host "Payment Amount: $($payment.paymentAmount) VND" -ForegroundColor Green
    Write-Host "Payment Method: $($payment.paymentMethod)" -ForegroundColor Green
    Write-Host "Customer ID: $($payment.customerId)" -ForegroundColor Green
    Write-Host "Note: $($payment.note)" -ForegroundColor Green
    
    # Verify Contract Payments
    Write-Host "`nVerifying contract payments..." -ForegroundColor Yellow
    try {
        $contractPayments = Invoke-RestMethod -Uri "http://localhost:8084/api/customer-payment/payment/$($payment.id)/contract-payments" -Method Get
        Write-Host "Found $($contractPayments.Count) contract payments:" -ForegroundColor Green
        $contractPayments | ForEach-Object {
            Write-Host "  - Contract $($_.contractId): $($_.allocatedAmount) VND" -ForegroundColor White
        }
        
        # Verify total
        $totalAllocated = ($contractPayments | Measure-Object -Property allocatedAmount -Sum).Sum
        Write-Host "`nTotal allocated: $totalAllocated VND" -ForegroundColor Cyan
        Write-Host "Payment amount: $($payment.paymentAmount) VND" -ForegroundColor Cyan
        
        if ($totalAllocated -eq $payment.paymentAmount) {
            Write-Host "✅ Amounts match perfectly!" -ForegroundColor Green
        } else {
            Write-Host "❌ Amount mismatch!" -ForegroundColor Red
        }
        
    } catch {
        Write-Host "Failed to verify contract payments: $($_.Exception.Message)" -ForegroundColor Red
    }
    
} catch {
    Write-Host "`n❌ FAILED to create payment!" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
            Write-Host "Response Body: $responseBody" -ForegroundColor Red
        } catch {
            Write-Host "Could not read response body" -ForegroundColor Red
        }
    }
}

Write-Host "`n🎯 Test completed!" -ForegroundColor Cyan
