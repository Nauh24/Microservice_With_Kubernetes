# Script to Monitor and Detect Data Duplication Issues
# This script continuously monitors for duplicate data creation

param(
    [int]$IntervalSeconds = 30,
    [switch]$Continuous,
    [switch]$ShowDetails
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "    DATA DUPLICATION MONITOR" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Function to check for duplicate contracts
function Check-DuplicateContracts {
    Write-Host "Checking for duplicate contracts..." -ForegroundColor Yellow
    
    $env:PGPASSWORD = "1234"
    
    try {
        $duplicates = psql -h localhost -U postgres -d customercontractdb -t -c "
            SELECT 
                customer_id, 
                starting_date, 
                ending_date, 
                total_amount, 
                address,
                COUNT(*) as count
            FROM customer_contracts 
            WHERE is_deleted = false
            GROUP BY customer_id, starting_date, ending_date, total_amount, address
            HAVING COUNT(*) > 1;"
        
        if ($duplicates -and $duplicates.Trim() -ne "") {
            Write-Host "❌ DUPLICATE CONTRACTS FOUND:" -ForegroundColor Red
            Write-Host $duplicates -ForegroundColor Red
            return $false
        } else {
            Write-Host "✓ No duplicate contracts found" -ForegroundColor Green
            return $true
        }
    } catch {
        Write-Host "❌ Error checking duplicate contracts: $_" -ForegroundColor Red
        return $false
    }
}

# Function to check for duplicate payments
function Check-DuplicatePayments {
    Write-Host "Checking for duplicate payments..." -ForegroundColor Yellow
    
    $env:PGPASSWORD = "1234"
    
    try {
        $duplicates = psql -h localhost -U postgres -d customerpaymentdb -t -c "
            SELECT 
                customer_contract_id, 
                payment_amount, 
                DATE(payment_date) as payment_date_only,
                payment_method,
                note,
                COUNT(*) as count
            FROM customer_payments 
            WHERE is_deleted = false
            GROUP BY customer_contract_id, payment_amount, DATE(payment_date), payment_method, note
            HAVING COUNT(*) > 1;"
        
        if ($duplicates -and $duplicates.Trim() -ne "") {
            Write-Host "❌ DUPLICATE PAYMENTS FOUND:" -ForegroundColor Red
            Write-Host $duplicates -ForegroundColor Red
            return $false
        } else {
            Write-Host "✓ No duplicate payments found" -ForegroundColor Green
            return $true
        }
    } catch {
        Write-Host "❌ Error checking duplicate payments: $_" -ForegroundColor Red
        return $false
    }
}

# Function to check database constraints
function Check-DatabaseConstraints {
    Write-Host "Checking database constraints..." -ForegroundColor Yellow
    
    $env:PGPASSWORD = "1234"
    
    # Check contract constraints
    try {
        $contractConstraints = psql -h localhost -U postgres -d customercontractdb -t -c "
            SELECT constraint_name, constraint_type 
            FROM information_schema.table_constraints 
            WHERE table_name = 'customer_contracts' 
            AND constraint_type IN ('UNIQUE', 'CHECK');"
        
        if ($contractConstraints -and $contractConstraints.Contains("uk_customer_contracts_no_duplicates")) {
            Write-Host "✓ Contract unique constraints are in place" -ForegroundColor Green
        } else {
            Write-Host "❌ Contract unique constraints are missing" -ForegroundColor Red
        }
    } catch {
        Write-Host "❌ Error checking contract constraints: $_" -ForegroundColor Red
    }
    
    # Check payment constraints
    try {
        $paymentConstraints = psql -h localhost -U postgres -d customerpaymentdb -t -c "
            SELECT constraint_name, constraint_type 
            FROM information_schema.table_constraints 
            WHERE table_name = 'customer_payments' 
            AND constraint_type IN ('UNIQUE', 'CHECK');"
        
        if ($paymentConstraints -and $paymentConstraints.Contains("uk_customer_payments_no_duplicates")) {
            Write-Host "✓ Payment unique constraints are in place" -ForegroundColor Green
        } else {
            Write-Host "❌ Payment unique constraints are missing" -ForegroundColor Red
        }
    } catch {
        Write-Host "❌ Error checking payment constraints: $_" -ForegroundColor Red
    }
}

# Function to check service health
function Check-ServiceHealth {
    Write-Host "Checking service health..." -ForegroundColor Yellow
    
    $services = @(
        @{Name="Customer Contract Service"; Port=8083; Path="/health"},
        @{Name="Customer Payment Service"; Port=8084; Path="/health"}
    )
    
    foreach ($service in $services) {
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:$($service.Port)$($service.Path)" -TimeoutSec 5 -ErrorAction SilentlyContinue
            if ($response.StatusCode -eq 200) {
                Write-Host "✓ $($service.Name) is healthy" -ForegroundColor Green
            } else {
                Write-Host "⚠ $($service.Name) returned status $($response.StatusCode)" -ForegroundColor Yellow
            }
        } catch {
            Write-Host "❌ $($service.Name) is not responding" -ForegroundColor Red
        }
    }
}

# Function to show detailed statistics
function Show-DetailedStatistics {
    if (-not $ShowDetails) { return }
    
    Write-Host ""
    Write-Host "DETAILED STATISTICS" -ForegroundColor Cyan
    Write-Host "===================" -ForegroundColor Cyan
    
    $env:PGPASSWORD = "1234"
    
    # Contract statistics
    try {
        Write-Host "Contract Statistics:" -ForegroundColor Yellow
        $contractStats = psql -h localhost -U postgres -d customercontractdb -t -c "
            SELECT 
                COUNT(*) as total_contracts,
                COUNT(CASE WHEN is_deleted = false THEN 1 END) as active_contracts,
                COUNT(CASE WHEN is_deleted = true THEN 1 END) as deleted_contracts
            FROM customer_contracts;"
        Write-Host $contractStats -ForegroundColor White
    } catch {
        Write-Host "❌ Error getting contract statistics: $_" -ForegroundColor Red
    }
    
    # Payment statistics
    try {
        Write-Host "Payment Statistics:" -ForegroundColor Yellow
        $paymentStats = psql -h localhost -U postgres -d customerpaymentdb -t -c "
            SELECT 
                COUNT(*) as total_payments,
                COUNT(CASE WHEN is_deleted = false THEN 1 END) as active_payments,
                COUNT(CASE WHEN is_deleted = true THEN 1 END) as deleted_payments,
                SUM(CASE WHEN is_deleted = false THEN payment_amount ELSE 0 END) as total_amount
            FROM customer_payments;"
        Write-Host $paymentStats -ForegroundColor White
    } catch {
        Write-Host "❌ Error getting payment statistics: $_" -ForegroundColor Red
    }
}

# Function to run a single check
function Run-SingleCheck {
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Write-Host "[$timestamp] Running duplicate check..." -ForegroundColor White
    
    $contractsOk = Check-DuplicateContracts
    $paymentsOk = Check-DuplicatePayments
    Check-DatabaseConstraints
    Check-ServiceHealth
    Show-DetailedStatistics
    
    Write-Host ""
    
    if ($contractsOk -and $paymentsOk) {
        Write-Host "✓ All checks passed - No duplicates detected" -ForegroundColor Green
        return $true
    } else {
        Write-Host "❌ Issues detected - Please investigate" -ForegroundColor Red
        return $false
    }
}

# Main execution
if ($Continuous) {
    Write-Host "Starting continuous monitoring (interval: $IntervalSeconds seconds)" -ForegroundColor White
    Write-Host "Press Ctrl+C to stop monitoring" -ForegroundColor White
    Write-Host ""
    
    $checkCount = 0
    $issueCount = 0
    
    while ($true) {
        $checkCount++
        Write-Host "========== CHECK #$checkCount ==========" -ForegroundColor Cyan
        
        $success = Run-SingleCheck
        if (-not $success) {
            $issueCount++
        }
        
        Write-Host "Issues detected so far: $issueCount out of $checkCount checks" -ForegroundColor $(if ($issueCount -eq 0) { "Green" } else { "Yellow" })
        Write-Host ""
        
        Start-Sleep -Seconds $IntervalSeconds
    }
} else {
    # Single check
    Run-SingleCheck
}

Write-Host ""
Write-Host "Monitoring completed." -ForegroundColor Cyan
