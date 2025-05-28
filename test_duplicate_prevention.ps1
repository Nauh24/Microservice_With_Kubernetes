# Script to Test Duplicate Prevention
# This script tests both contract and payment duplicate prevention

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "    DUPLICATE PREVENTION TEST" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Function to test contract duplicate prevention
function Test-ContractDuplicatePrevention {
    Write-Host "Testing Contract Duplicate Prevention..." -ForegroundColor Yellow
    Write-Host "========================================" -ForegroundColor Yellow

    $testContract = @{
        customerId = 1
        startingDate = "2024-01-15"
        endingDate = "2024-02-15"
        totalAmount = 10000000
        address = "Test Address Duplicate Prevention"
        description = "Test Contract for Duplicate Prevention"
        jobDetails = @()
    } | ConvertTo-Json -Depth 3

    try {
        # First contract creation (should succeed)
        Write-Host "Creating first contract..." -ForegroundColor White
        $response1 = Invoke-RestMethod -Uri "http://localhost:8083/api/contracts" -Method POST -Body $testContract -ContentType "application/json" -TimeoutSec 10
        Write-Host "✓ First contract creation succeeded (ID: $($response1.id))" -ForegroundColor Green

        # Wait a moment
        Start-Sleep -Seconds 2

        # Second identical contract creation (should fail)
        Write-Host "Attempting to create duplicate contract..." -ForegroundColor White
        try {
            $response2 = Invoke-RestMethod -Uri "http://localhost:8083/api/contracts" -Method POST -Body $testContract -ContentType "application/json" -TimeoutSec 10
            Write-Host "❌ Duplicate contract creation should have failed but succeeded (ID: $($response2.id))" -ForegroundColor Red
            return $false
        } catch {
            Write-Host "✓ Duplicate contract creation properly prevented" -ForegroundColor Green
            Write-Host "   Error message: $($_.Exception.Message)" -ForegroundColor Gray
            return $true
        }
    } catch {
        Write-Host "❌ Contract creation test failed: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# Function to test payment duplicate prevention
function Test-PaymentDuplicatePrevention {
    Write-Host ""
    Write-Host "Testing Payment Duplicate Prevention..." -ForegroundColor Yellow
    Write-Host "=======================================" -ForegroundColor Yellow

    $testPayment = @{
        customerContractId = 1
        customerId = 1
        paymentAmount = 1000000
        paymentMethod = 0
        note = "Test Payment Duplicate Prevention"
    } | ConvertTo-Json -Depth 3

    try {
        # First payment creation (should succeed)
        Write-Host "Creating first payment..." -ForegroundColor White
        $response1 = Invoke-RestMethod -Uri "http://localhost:8084/api/payments" -Method POST -Body $testPayment -ContentType "application/json" -TimeoutSec 10
        Write-Host "✓ First payment creation succeeded (ID: $($response1.id))" -ForegroundColor Green

        # Wait a moment
        Start-Sleep -Seconds 2

        # Second identical payment creation (should fail)
        Write-Host "Attempting to create duplicate payment..." -ForegroundColor White
        try {
            $response2 = Invoke-RestMethod -Uri "http://localhost:8084/api/payments" -Method POST -Body $testPayment -ContentType "application/json" -TimeoutSec 10
            Write-Host "❌ Duplicate payment creation should have failed but succeeded (ID: $($response2.id))" -ForegroundColor Red
            return $false
        } catch {
            Write-Host "✓ Duplicate payment creation properly prevented" -ForegroundColor Green
            Write-Host "   Error message: $($_.Exception.Message)" -ForegroundColor Gray
            return $true
        }
    } catch {
        Write-Host "❌ Payment creation test failed: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# Function to check service health
function Test-ServiceHealth {
    Write-Host ""
    Write-Host "Checking Service Health..." -ForegroundColor Yellow
    Write-Host "==========================" -ForegroundColor Yellow

    $services = @(
        @{Name="Customer Contract Service"; Port=8083; Path="/actuator/health"},
        @{Name="Customer Payment Service"; Port=8084; Path="/actuator/health"},
        @{Name="API Gateway"; Port=8080; Path="/actuator/health"}
    )

    $allHealthy = $true

    foreach ($service in $services) {
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:$($service.Port)$($service.Path)" -TimeoutSec 5 -ErrorAction SilentlyContinue
            if ($response.StatusCode -eq 200) {
                Write-Host "✓ $($service.Name) is healthy" -ForegroundColor Green
            } else {
                Write-Host "⚠ $($service.Name) returned status $($response.StatusCode)" -ForegroundColor Yellow
                $allHealthy = $false
            }
        } catch {
            Write-Host "❌ $($service.Name) is not responding: $($_.Exception.Message)" -ForegroundColor Red
            $allHealthy = $false
        }
    }

    return $allHealthy
}

# Function to check database duplicates
function Check-DatabaseDuplicates {
    Write-Host ""
    Write-Host "Checking Database for Existing Duplicates..." -ForegroundColor Yellow
    Write-Host "=============================================" -ForegroundColor Yellow

    try {
        # Check contract duplicates
        Write-Host "Checking contract duplicates..." -ForegroundColor White
        $contractDuplicates = docker run --rm --network microservice_with_kubernetes_microservice-network -e PGPASSWORD=1234 postgres:13 psql -h host.docker.internal -U postgres -d customercontractdb -t -c "SELECT COUNT(*) FROM (SELECT customer_id, starting_date, ending_date, total_amount, address, COUNT(*) as cnt FROM customer_contracts WHERE is_deleted = false GROUP BY customer_id, starting_date, ending_date, total_amount, address HAVING COUNT(*) > 1) duplicates;"

        if ($contractDuplicates -and $contractDuplicates.Trim() -eq "0") {
            Write-Host "✓ No contract duplicates found" -ForegroundColor Green
        } else {
            Write-Host "❌ Contract duplicates found: $($contractDuplicates.Trim())" -ForegroundColor Red
        }

        # Check payment duplicates
        Write-Host "Checking payment duplicates..." -ForegroundColor White
        $paymentDuplicates = docker run --rm --network microservice_with_kubernetes_microservice-network -e PGPASSWORD=1234 postgres:13 psql -h host.docker.internal -U postgres -d customerpaymentdb -t -c "SELECT COUNT(*) FROM (SELECT customer_contract_id, payment_amount, DATE(payment_date), payment_method, note, COUNT(*) as cnt FROM customer_payments WHERE is_deleted = false GROUP BY customer_contract_id, payment_amount, DATE(payment_date), payment_method, note HAVING COUNT(*) > 1) duplicates;"

        if ($paymentDuplicates -and $paymentDuplicates.Trim() -eq "0") {
            Write-Host "✓ No payment duplicates found" -ForegroundColor Green
        } else {
            Write-Host "❌ Payment duplicates found: $($paymentDuplicates.Trim())" -ForegroundColor Red
        }

    } catch {
        Write-Host "❌ Error checking database duplicates: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Main execution
Write-Host "Starting duplicate prevention tests..." -ForegroundColor White
Write-Host ""

# Check service health first
$servicesHealthy = Test-ServiceHealth

if (-not $servicesHealthy) {
    Write-Host ""
    Write-Host "⚠ Some services are not healthy. Tests may fail." -ForegroundColor Yellow
    Write-Host "Continuing with tests anyway..." -ForegroundColor Yellow
}

# Check existing duplicates
Check-DatabaseDuplicates

# Test contract duplicate prevention
$contractTestPassed = Test-ContractDuplicatePrevention

# Test payment duplicate prevention
$paymentTestPassed = Test-PaymentDuplicatePrevention

# Summary
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "           TEST SUMMARY" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

if ($servicesHealthy) {
    Write-Host "✓ Service Health: PASSED" -ForegroundColor Green
} else {
    Write-Host "❌ Service Health: FAILED" -ForegroundColor Red
}

if ($contractTestPassed) {
    Write-Host "✓ Contract Duplicate Prevention: PASSED" -ForegroundColor Green
} else {
    Write-Host "❌ Contract Duplicate Prevention: FAILED" -ForegroundColor Red
}

if ($paymentTestPassed) {
    Write-Host "✓ Payment Duplicate Prevention: PASSED" -ForegroundColor Green
} else {
    Write-Host "❌ Payment Duplicate Prevention: FAILED" -ForegroundColor Red
}

Write-Host ""
if ($servicesHealthy -and $contractTestPassed -and $paymentTestPassed) {
    Write-Host "🎉 ALL TESTS PASSED! Duplicate prevention is working correctly." -ForegroundColor Green
} else {
    Write-Host "⚠ Some tests failed. Please check the logs above for details." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "You can now test manually by:" -ForegroundColor Cyan
Write-Host "1. Opening http://localhost:3000 in your browser" -ForegroundColor Cyan
Write-Host "2. Creating a contract" -ForegroundColor Cyan
Write-Host "3. Trying to create the same contract again" -ForegroundColor Cyan
Write-Host "4. The second attempt should be prevented" -ForegroundColor Cyan
