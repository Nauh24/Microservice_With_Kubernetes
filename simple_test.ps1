# Simple Test for Duplicate Prevention

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "    DUPLICATE PREVENTION TEST" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Test service health
Write-Host "Checking Service Health..." -ForegroundColor Yellow
Write-Host "==========================" -ForegroundColor Yellow

$services = @(
    @{Name="Customer Contract Service"; Port=8083},
    @{Name="Customer Payment Service"; Port=8084},
    @{Name="API Gateway"; Port=8080}
)

foreach ($service in $services) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:$($service.Port)/actuator/health" -TimeoutSec 5 -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            Write-Host "✓ $($service.Name) is healthy" -ForegroundColor Green
        } else {
            Write-Host "⚠ $($service.Name) returned status $($response.StatusCode)" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "❌ $($service.Name) is not responding" -ForegroundColor Red
    }
}

Write-Host ""

# Test contract creation
Write-Host "Testing Contract Creation..." -ForegroundColor Yellow
Write-Host "============================" -ForegroundColor Yellow

$testContract = @{
    customerId = 1
    startingDate = "2024-01-15"
    endingDate = "2024-02-15"
    totalAmount = 10000000
    address = "Test Address Simple"
    description = "Test Contract Simple"
    jobDetails = @()
} | ConvertTo-Json -Depth 3

try {
    Write-Host "Creating first contract..." -ForegroundColor White
    $response1 = Invoke-RestMethod -Uri "http://localhost:8083/api/contracts" -Method POST -Body $testContract -ContentType "application/json" -TimeoutSec 10
    Write-Host "✓ First contract creation succeeded (ID: $($response1.id))" -ForegroundColor Green
    
    Start-Sleep -Seconds 2
    
    Write-Host "Attempting to create duplicate contract..." -ForegroundColor White
    try {
        $response2 = Invoke-RestMethod -Uri "http://localhost:8083/api/contracts" -Method POST -Body $testContract -ContentType "application/json" -TimeoutSec 10
        Write-Host "❌ Duplicate contract creation should have failed but succeeded (ID: $($response2.id))" -ForegroundColor Red
    } catch {
        Write-Host "✓ Duplicate contract creation properly prevented" -ForegroundColor Green
        Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ Contract creation test failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test payment creation
Write-Host "Testing Payment Creation..." -ForegroundColor Yellow
Write-Host "===========================" -ForegroundColor Yellow

$testPayment = @{
    customerContractId = 1
    customerId = 1
    paymentAmount = 1000000
    paymentMethod = 0
    note = "Test Payment Simple"
} | ConvertTo-Json -Depth 3

try {
    Write-Host "Creating first payment..." -ForegroundColor White
    $response1 = Invoke-RestMethod -Uri "http://localhost:8084/api/payments" -Method POST -Body $testPayment -ContentType "application/json" -TimeoutSec 10
    Write-Host "✓ First payment creation succeeded (ID: $($response1.id))" -ForegroundColor Green
    
    Start-Sleep -Seconds 2
    
    Write-Host "Attempting to create duplicate payment..." -ForegroundColor White
    try {
        $response2 = Invoke-RestMethod -Uri "http://localhost:8084/api/payments" -Method POST -Body $testPayment -ContentType "application/json" -TimeoutSec 10
        Write-Host "❌ Duplicate payment creation should have failed but succeeded (ID: $($response2.id))" -ForegroundColor Red
    } catch {
        Write-Host "✓ Duplicate payment creation properly prevented" -ForegroundColor Green
        Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ Payment creation test failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Check database duplicates
Write-Host "Checking Database Duplicates..." -ForegroundColor Yellow
Write-Host "===============================" -ForegroundColor Yellow

try {
    Write-Host "Checking contract duplicates..." -ForegroundColor White
    $contractCheck = docker run --rm --network microservice_with_kubernetes_microservice-network -e PGPASSWORD=1234 postgres:13 psql -h host.docker.internal -U postgres -d customercontractdb -t -c "SELECT COUNT(*) FROM customer_contracts WHERE is_deleted = false;"
    Write-Host "Total active contracts: $($contractCheck.Trim())" -ForegroundColor White
    
    Write-Host "Checking payment duplicates..." -ForegroundColor White
    $paymentCheck = docker run --rm --network microservice_with_kubernetes_microservice-network -e PGPASSWORD=1234 postgres:13 psql -h host.docker.internal -U postgres -d customerpaymentdb -t -c "SELECT COUNT(*) FROM customer_payments WHERE is_deleted = false;"
    Write-Host "Total active payments: $($paymentCheck.Trim())" -ForegroundColor White
} catch {
    Write-Host "❌ Error checking database: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "           TEST COMPLETED" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Manual Testing:" -ForegroundColor Cyan
Write-Host "1. Open http://localhost:3000 in your browser" -ForegroundColor Cyan
Write-Host "2. Try creating contracts and payments" -ForegroundColor Cyan
Write-Host "3. Attempt to create duplicates" -ForegroundColor Cyan
Write-Host "4. Check that duplicates are prevented" -ForegroundColor Cyan
