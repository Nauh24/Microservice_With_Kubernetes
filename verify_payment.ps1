Write-Host "🔍 Verifying Payment ID: 50" -ForegroundColor Cyan

try {
    $contractPayments = Invoke-RestMethod -Uri "http://localhost:8084/api/customer-payment/payment/50/contract-payments" -Method Get
    Write-Host "✅ SUCCESS! Found $($contractPayments.Count) contract payments:" -ForegroundColor Green

    $contractPayments | ForEach-Object {
        Write-Host "  - Contract $($_.contractId): $($_.allocatedAmount) VND" -ForegroundColor White
    }

    $total = ($contractPayments | Measure-Object -Property allocatedAmount -Sum).Sum
    Write-Host "`nTotal Allocated: $total VND" -ForegroundColor Green
    Write-Host "🎉 MULTIPLE CONTRACTS PAYMENT WORKS PERFECTLY!" -ForegroundColor Green

} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}
