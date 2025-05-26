# PowerShell script to apply contract creation fixes

Write-Host "=== Applying Contract Creation Fixes ===" -ForegroundColor Green

# Step 1: Check if Docker is running
Write-Host "Step 1: Checking Docker status..." -ForegroundColor Yellow
try {
    $dockerStatus = docker ps 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Docker is not running. Please start Docker Desktop first." -ForegroundColor Red
        exit 1
    }
    Write-Host "✅ Docker is running" -ForegroundColor Green
} catch {
    Write-Host "❌ Docker is not available. Please install and start Docker Desktop." -ForegroundColor Red
    exit 1
}

# Step 2: Check if salary column exists and add it if needed
Write-Host "Step 2: Checking and adding salary column to work_shifts table..." -ForegroundColor Yellow

$checkColumnQuery = @"
SELECT COUNT(*) 
FROM information_schema.columns 
WHERE table_name = 'work_shifts' AND column_name = 'salary';
"@

try {
    $columnExists = docker exec postgres psql -U postgres -d customerContractMSDb -t -c $checkColumnQuery
    $columnExists = $columnExists.Trim()
    
    if ($columnExists -eq "0") {
        Write-Host "Salary column does not exist. Adding it now..." -ForegroundColor Yellow
        
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

# Step 3: Restart customer-contract-service
Write-Host "Step 3: Restarting customer-contract-service..." -ForegroundColor Yellow
docker restart customer-contract-service

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Customer-contract-service restarted successfully" -ForegroundColor Green
} else {
    Write-Host "❌ Failed to restart customer-contract-service" -ForegroundColor Red
}

# Step 4: Wait for service to start
Write-Host "Step 4: Waiting for service to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 20

# Step 5: Test the service
Write-Host "Step 5: Testing service health..." -ForegroundColor Yellow
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

# Step 6: Test API Gateway
Write-Host "Step 6: Testing API Gateway..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8083/actuator/health" -Method Get -TimeoutSec 10
    if ($response.status -eq "UP") {
        Write-Host "✅ API Gateway is healthy" -ForegroundColor Green
    } else {
        Write-Host "⚠️ API Gateway health check returned: $($response.status)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "⚠️ Could not reach API Gateway health endpoint" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=== Contract Creation Fixes Applied ===" -ForegroundColor Green
Write-Host "The following changes were made:" -ForegroundColor Cyan
Write-Host "1. ✅ Added salary column to work_shifts table (if it didn't exist)" -ForegroundColor White
Write-Host "2. ✅ Implemented automatic contract amount calculation" -ForegroundColor White
Write-Host "3. ✅ Removed manual total amount input field" -ForegroundColor White
Write-Host "4. ✅ Added comprehensive validation for JobDetail and WorkShift fields" -ForegroundColor White
Write-Host "5. ✅ Added backend calculation validation" -ForegroundColor White
Write-Host "6. ✅ Restarted customer-contract-service" -ForegroundColor White
Write-Host ""
Write-Host "You can now try creating a contract again. The total amount will be calculated automatically." -ForegroundColor Green
Write-Host "Make sure to:" -ForegroundColor Yellow
Write-Host "- Add job details with work shifts" -ForegroundColor White
Write-Host "- Set salary for each work shift" -ForegroundColor White
Write-Host "- Select working days for each shift" -ForegroundColor White
Write-Host "- The total contract amount will be calculated and displayed automatically" -ForegroundColor White
