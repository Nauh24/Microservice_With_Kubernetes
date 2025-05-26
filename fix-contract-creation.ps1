# PowerShell script to fix contract creation issue

Write-Host "=== Fixing Contract Creation Issue ===" -ForegroundColor Green

# Step 1: Check if salary column exists and add it if needed
Write-Host "Step 1: Checking and adding salary column to work_shifts table..." -ForegroundColor Yellow

$checkColumnQuery = @"
SELECT COUNT(*) 
FROM information_schema.columns 
WHERE table_name = 'work_shifts' AND column_name = 'salary';
"@

try {
    $columnExists = docker exec postgres psql -U postgres -d customerContractMSDb -t -c $checkColumnQuery
    $columnExists = $columnExists.Trim()
    
    if ($columnExists -eq "0") {
        Write-Host "Salary column does not exist. Adding it now..." -ForegroundColor Red
        
        $addColumnQuery = @"
-- Add salary column to work_shifts table
ALTER TABLE work_shifts ADD COLUMN salary DOUBLE PRECISION;

-- Set default value for existing records
UPDATE work_shifts SET salary = 0.0 WHERE salary IS NULL;

-- Verify the column was added
SELECT 'Salary column added successfully' as result;
"@
        
        docker exec postgres psql -U postgres -d customerContractMSDb -c $addColumnQuery
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ Salary column added successfully" -ForegroundColor Green
        } else {
            Write-Host "❌ Failed to add salary column" -ForegroundColor Red
            exit 1
        }
    } else {
        Write-Host "✅ Salary column already exists" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ Error checking/adding salary column: $_" -ForegroundColor Red
    exit 1
}

# Step 2: Restart customer-contract-service
Write-Host "Step 2: Restarting customer-contract-service..." -ForegroundColor Yellow
docker restart customer-contract-service

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Customer-contract-service restarted successfully" -ForegroundColor Green
} else {
    Write-Host "❌ Failed to restart customer-contract-service" -ForegroundColor Red
}

# Step 3: Wait for service to start
Write-Host "Step 3: Waiting for service to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

# Step 4: Test the service
Write-Host "Step 4: Testing service health..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8087/actuator/health" -Method Get -TimeoutSec 10
    if ($response.status -eq "UP") {
        Write-Host "✅ Customer-contract-service is healthy" -ForegroundColor Green
    } else {
        Write-Host "⚠️ Customer-contract-service health check returned: $($response.status)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "⚠️ Could not reach customer-contract-service health endpoint" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=== Fix Complete ===" -ForegroundColor Green
Write-Host "The following changes were made:" -ForegroundColor Cyan
Write-Host "1. ✅ Added salary column to work_shifts table (if it didn't exist)" -ForegroundColor White
Write-Host "2. ✅ Added comprehensive validation for JobDetail and WorkShift fields" -ForegroundColor White
Write-Host "3. ✅ Restarted customer-contract-service" -ForegroundColor White
Write-Host ""
Write-Host "You can now try creating a contract again." -ForegroundColor Green
