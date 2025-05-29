# Test Contract Creation Fix
# This script tests if the contract creation works without address validation errors

Write-Host "=== CONTRACT ADDRESS VALIDATION FIX TEST ===" -ForegroundColor Green
Write-Host "Testing if contract creation works without address validation errors..." -ForegroundColor Yellow

# Test 1: Check if frontend is running
Write-Host "`n1. Checking if frontend is accessible..." -ForegroundColor Yellow
try {
    $frontendResponse = Invoke-WebRequest -Uri "http://localhost:3000" -Method GET -TimeoutSec 10
    if ($frontendResponse.StatusCode -eq 200) {
        Write-Host "✅ Frontend is accessible" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ Frontend is not accessible: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test 2: Check if customer-contract-service is running
Write-Host "`n2. Checking if customer-contract-service is accessible..." -ForegroundColor Yellow
try {
    $contractServiceResponse = Invoke-WebRequest -Uri "http://localhost:8083/actuator/health" -Method GET -TimeoutSec 10
    if ($contractServiceResponse.StatusCode -eq 200) {
        Write-Host "✅ Customer Contract Service is accessible" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ Customer Contract Service is not accessible: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test 3: Check if customer-service is running
Write-Host "`n3. Checking if customer-service is accessible..." -ForegroundColor Yellow
try {
    $customerServiceResponse = Invoke-WebRequest -Uri "http://localhost:8081/actuator/health" -Method GET -TimeoutSec 10
    if ($customerServiceResponse.StatusCode -eq 200) {
        Write-Host "✅ Customer Service is accessible" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ Customer Service is not accessible: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test 4: Check if job-service is running
Write-Host "`n4. Checking if job-service is accessible..." -ForegroundColor Yellow
try {
    $jobServiceResponse = Invoke-WebRequest -Uri "http://localhost:8082/actuator/health" -Method GET -TimeoutSec 10
    if ($jobServiceResponse.StatusCode -eq 200) {
        Write-Host "✅ Job Service is accessible" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ Job Service is not accessible: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test 5: Test contract creation with minimal data (no work location)
Write-Host "`n5. Testing contract creation without work location..." -ForegroundColor Yellow

# First, get a customer ID
try {
    $customersResponse = Invoke-WebRequest -Uri "http://localhost:8081/api/customers" -Method GET -TimeoutSec 10
    $customers = $customersResponse.Content | ConvertFrom-Json

    if ($customers.Count -eq 0) {
        Write-Host "❌ No customers found. Please create a customer first." -ForegroundColor Red
        exit 1
    }

    $customerId = $customers[0].id
    Write-Host "✅ Found customer with ID: $customerId" -ForegroundColor Green
} catch {
    Write-Host "❌ Failed to get customers: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Get a job category ID
try {
    $jobCategoriesResponse = Invoke-WebRequest -Uri "http://localhost:8082/api/job-categories" -Method GET -TimeoutSec 10
    $jobCategories = $jobCategoriesResponse.Content | ConvertFrom-Json

    if ($jobCategories.Count -eq 0) {
        Write-Host "❌ No job categories found. Please create a job category first." -ForegroundColor Red
        exit 1
    }

    $jobCategoryId = $jobCategories[0].id
    Write-Host "✅ Found job category with ID: $jobCategoryId" -ForegroundColor Green
} catch {
    Write-Host "❌ Failed to get job categories: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Create test contract without work location
$testContract = @{
    customerId = $customerId
    startingDate = "2025-06-01"
    endingDate = "2025-06-30"
    totalAmount = 1000000
    description = "Test contract without work location"
    jobDetails = @(
        @{
            jobCategoryId = $jobCategoryId
            startDate = "2025-06-01"
            endDate = "2025-06-30"
            # workLocation is intentionally omitted to test the fix
            workShifts = @(
                @{
                    startTime = "08:00"
                    endTime = "17:00"
                    numberOfWorkers = 2
                    salary = 500000
                    workingDays = "1,2,3,4,5"
                }
            )
        }
    )
} | ConvertTo-Json -Depth 10

try {
    $contractResponse = Invoke-WebRequest -Uri "http://localhost:8083/api/contracts" -Method POST -Body $testContract -ContentType "application/json" -TimeoutSec 30

    if ($contractResponse.StatusCode -eq 200 -or $contractResponse.StatusCode -eq 201) {
        Write-Host "✅ Contract created successfully without work location!" -ForegroundColor Green
        $createdContract = $contractResponse.Content | ConvertFrom-Json
        Write-Host "   Contract ID: $($createdContract.id)" -ForegroundColor Cyan
        Write-Host "   Contract Address: $($createdContract.address)" -ForegroundColor Cyan
    }
} catch {
    $errorMessage = $_.Exception.Message
    if ($_.Exception.Response) {
        $errorResponse = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($errorResponse)
        $errorContent = $reader.ReadToEnd()
        Write-Host "❌ Contract creation failed: $errorContent" -ForegroundColor Red
    } else {
        Write-Host "❌ Contract creation failed: $errorMessage" -ForegroundColor Red
    }
    exit 1
}

Write-Host "`nAll tests passed! The contract address validation fix is working correctly." -ForegroundColor Green
Write-Host "You should now be able to create contracts without the address validation error." -ForegroundColor Green
