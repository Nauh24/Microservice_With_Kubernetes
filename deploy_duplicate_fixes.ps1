# Comprehensive Script to Deploy Data Duplication Fixes
# This script applies database constraints, rebuilds services, and tests the fixes

param(
    [switch]$SkipDatabase,
    [switch]$SkipBuild,
    [switch]$SkipTest,
    [switch]$Force
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  DATA DUPLICATION FIXES DEPLOYMENT" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Function to check if PostgreSQL is running
function Test-PostgreSQL {
    try {
        $env:PGPASSWORD = "1234"
        $result = psql -h localhost -U postgres -d postgres -c "SELECT 1;" 2>$null
        return $true
    } catch {
        return $false
    }
}

# Function to apply database constraints
function Apply-DatabaseConstraints {
    Write-Host "STEP 1: APPLYING DATABASE CONSTRAINTS" -ForegroundColor Yellow
    Write-Host "======================================" -ForegroundColor Yellow

    if (-not (Test-PostgreSQL)) {
        Write-Host "❌ PostgreSQL is not running or not accessible" -ForegroundColor Red
        Write-Host "Please start PostgreSQL and ensure it's accessible on localhost:5432" -ForegroundColor Red
        return $false
    }

    Write-Host "✓ PostgreSQL is accessible" -ForegroundColor Green

    # Set PostgreSQL password
    $env:PGPASSWORD = "1234"

    # Apply constraints to customer contract database
    Write-Host "Applying constraints to customercontractdb..." -ForegroundColor Yellow
    try {
        psql -h localhost -U postgres -d customercontractdb -f "fix_duplicate_data_issues.sql"
        Write-Host "✓ Customer contract database constraints applied" -ForegroundColor Green
    } catch {
        Write-Host "❌ Failed to apply customer contract database constraints" -ForegroundColor Red
        Write-Host "Error: $_" -ForegroundColor Red
        return $false
    }

    # Apply constraints to customer payment database
    Write-Host "Applying constraints to customerpaymentdb..." -ForegroundColor Yellow
    try {
        psql -h localhost -U postgres -d customerpaymentdb -f "fix_duplicate_data_issues.sql"
        Write-Host "✓ Customer payment database constraints applied" -ForegroundColor Green
    } catch {
        Write-Host "❌ Failed to apply customer payment database constraints" -ForegroundColor Red
        Write-Host "Error: $_" -ForegroundColor Red
        return $false
    }

    Write-Host ""
    return $true
}

# Function to build and deploy services
function Build-AndDeployServices {
    Write-Host "STEP 2: BUILDING AND DEPLOYING SERVICES" -ForegroundColor Yellow
    Write-Host "========================================" -ForegroundColor Yellow

    # Stop existing containers
    Write-Host "Stopping existing containers..." -ForegroundColor Yellow
    docker-compose down

    # Build and start services
    Write-Host "Building and starting services..." -ForegroundColor Yellow
    try {
        docker-compose up -d --build
        Write-Host "✓ Services built and started successfully" -ForegroundColor Green
    } catch {
        Write-Host "❌ Failed to build and start services" -ForegroundColor Red
        Write-Host "Error: $_" -ForegroundColor Red
        return $false
    }

    # Wait for services to be ready
    Write-Host "Waiting for services to be ready..." -ForegroundColor Yellow
    Start-Sleep -Seconds 30

    # Check service health
    $services = @(
        @{Name="API Gateway"; Port=8080},
        @{Name="Customer Service"; Port=8081},
        @{Name="Job Service"; Port=8082},
        @{Name="Customer Contract Service"; Port=8083},
        @{Name="Customer Payment Service"; Port=8084},
        @{Name="Customer Statistics Service"; Port=8085},
        @{Name="Frontend"; Port=3000}
    )

    foreach ($service in $services) {
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:$($service.Port)/health" -TimeoutSec 5 -ErrorAction SilentlyContinue
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
    return $true
}

# Function to run duplicate prevention tests
function Test-DuplicatePrevention {
    Write-Host "STEP 3: TESTING DUPLICATE PREVENTION" -ForegroundColor Yellow
    Write-Host "=====================================" -ForegroundColor Yellow

    # Test contract creation
    Write-Host "Testing contract creation duplicate prevention..." -ForegroundColor Yellow

    $testContract = @{
        customerId = 1
        startingDate = "2024-01-15"
        endingDate = "2024-02-15"
        totalAmount = 10000000
        address = "Test Address"
        description = "Test Contract"
        jobDetails = @()
    } | ConvertTo-Json -Depth 3

    try {
        # First contract creation (should succeed)
        $response1 = Invoke-RestMethod -Uri "http://localhost:8080/api/customer-contract" -Method POST -Body $testContract -ContentType "application/json"
        Write-Host "✓ First contract creation succeeded (ID: $($response1.id))" -ForegroundColor Green

        # Second identical contract creation (should fail)
        try {
            $response2 = Invoke-RestMethod -Uri "http://localhost:8080/api/customer-contract" -Method POST -Body $testContract -ContentType "application/json"
            Write-Host "❌ Duplicate contract creation should have failed but succeeded" -ForegroundColor Red
        } catch {
            Write-Host "✓ Duplicate contract creation properly prevented" -ForegroundColor Green
        }
    } catch {
        Write-Host "❌ Contract creation test failed: $_" -ForegroundColor Red
    }

    Write-Host ""
    return $true
}

# Main execution
Write-Host "Starting deployment process..." -ForegroundColor White
Write-Host ""

$success = $true

# Step 1: Apply database constraints
if (-not $SkipDatabase) {
    if (-not (Apply-DatabaseConstraints)) {
        $success = $false
        if (-not $Force) {
            Write-Host "Database constraint application failed. Use -Force to continue anyway." -ForegroundColor Red
            exit 1
        }
    }
} else {
    Write-Host "SKIPPING: Database constraints (use -SkipDatabase to skip)" -ForegroundColor Yellow
    Write-Host ""
}

# Step 2: Build and deploy services
if (-not $SkipBuild) {
    if (-not (Build-AndDeployServices)) {
        $success = $false
        if (-not $Force) {
            Write-Host "Service build and deployment failed. Use -Force to continue anyway." -ForegroundColor Red
            exit 1
        }
    }
} else {
    Write-Host "SKIPPING: Service build and deployment (use -SkipBuild to skip)" -ForegroundColor Yellow
    Write-Host ""
}

# Step 3: Test duplicate prevention
if (-not $SkipTest) {
    Test-DuplicatePrevention
} else {
    Write-Host "SKIPPING: Duplicate prevention tests (use -SkipTest to skip)" -ForegroundColor Yellow
    Write-Host ""
}

# Summary
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "           DEPLOYMENT SUMMARY" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

if ($success) {
    Write-Host "✓ Deployment completed successfully!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Next steps:" -ForegroundColor White
    Write-Host "1. Test contract creation in the frontend (http://localhost:3000)" -ForegroundColor White
    Write-Host "2. Test payment processing" -ForegroundColor White
    Write-Host "3. Monitor logs for any duplicate creation attempts" -ForegroundColor White
    Write-Host "4. Check database for any remaining duplicates" -ForegroundColor White
} else {
    Write-Host "❌ Deployment completed with errors" -ForegroundColor Red
    Write-Host "Please check the logs above and resolve any issues" -ForegroundColor Red
}

Write-Host ""
Write-Host "To view service logs: docker-compose logs -f [service-name]" -ForegroundColor Cyan
Write-Host "To check database: psql -h localhost -U postgres -d [database-name]" -ForegroundColor Cyan
