# Test Contract Creation Without Address Requirements
# This script tests that contracts can be created without requiring address fields

Write-Host "Testing Contract Creation Without Address Requirements" -ForegroundColor Cyan
Write-Host "====================================================" -ForegroundColor Cyan

# Test data for contract without work location
$contractWithoutWorkLocation = @{
    customerId = 1
    startingDate = "2024-06-01"
    endingDate = "2024-06-30"
    totalAmount = 5000000
    description = "Test contract without work location"
    jobDetails = @(
        @{
            jobCategoryId = 21
            startDate = "2024-06-01"
            endDate = "2024-06-30"
            workShifts = @(
                @{
                    startTime = "08:00"
                    endTime = "17:00"
                    numberOfWorkers = 3
                    salary = 500000
                    workingDays = "1,2,3,4,5"
                }
            )
        }
    )
} | ConvertTo-Json -Depth 5

# Test data for contract with empty address
$contractWithEmptyAddress = @{
    customerId = 1
    startingDate = "2024-07-01"
    endingDate = "2024-07-31"
    totalAmount = 6000000
    address = ""
    description = "Test contract with empty address"
    jobDetails = @(
        @{
            jobCategoryId = 21
            startDate = "2024-07-01"
            endDate = "2024-07-31"
            workLocation = "Another Test Work Location"
            workShifts = @(
                @{
                    startTime = "09:00"
                    endTime = "18:00"
                    numberOfWorkers = 2
                    salary = 600000
                    workingDays = "1,2,3,4,5"
                }
            )
        }
    )
} | ConvertTo-Json -Depth 5

# Test URLs
$apiGatewayUrl = "http://localhost:8080/api/customer-contract"
$directServiceUrl = "http://localhost:8083/api/contracts"

function Test-ContractCreation {
    param(
        [string]$TestName,
        [string]$Url,
        [string]$ContractData
    )

    Write-Host ""
    Write-Host "Testing: $TestName" -ForegroundColor Yellow
    Write-Host "URL: $Url" -ForegroundColor Gray

    try {
        $response = Invoke-RestMethod -Uri $Url -Method POST -Body $ContractData -ContentType "application/json" -TimeoutSec 30

        if ($response -and $response.id) {
            Write-Host "✓ SUCCESS: Contract created with ID $($response.id)" -ForegroundColor Green
            Write-Host "  - Customer ID: $($response.customerId)" -ForegroundColor Gray
            Write-Host "  - Total Amount: $($response.totalAmount)" -ForegroundColor Gray
            Write-Host "  - Address: '$($response.address)'" -ForegroundColor Gray
            Write-Host "  - Job Details Count: $($response.jobDetails.Count)" -ForegroundColor Gray
            return $true
        } else {
            Write-Host "✗ FAILED: Invalid response received" -ForegroundColor Red
            return $false
        }
    }
    catch {
        Write-Host "✗ FAILED: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.Exception.Response) {
            $statusCode = $_.Exception.Response.StatusCode
            Write-Host "  Status Code: $statusCode" -ForegroundColor Red
        }
        return $false
    }
}

# Run tests
Write-Host ""
Write-Host "Starting Contract Creation Tests..." -ForegroundColor White

$results = @()

# Test 1: Contract without work location via API Gateway
$results += Test-ContractCreation "Contract without work location (API Gateway)" $apiGatewayUrl $contractWithoutWorkLocation

# Test 2: Contract with empty address via API Gateway
$results += Test-ContractCreation "Contract with empty address (API Gateway)" $apiGatewayUrl $contractWithEmptyAddress

# Test 3: Contract without work location via Direct Service
$results += Test-ContractCreation "Contract without work location (Direct Service)" $directServiceUrl $contractWithoutWorkLocation

# Test 4: Contract with empty address via Direct Service
$results += Test-ContractCreation "Contract with empty address (Direct Service)" $directServiceUrl $contractWithEmptyAddress

# Summary
Write-Host ""
Write-Host "Test Results Summary:" -ForegroundColor Cyan
Write-Host "===================" -ForegroundColor Cyan

$successCount = ($results | Where-Object { $_ -eq $true }).Count
$totalCount = $results.Count

Write-Host "Successful tests: $successCount/$totalCount" -ForegroundColor $(if ($successCount -eq $totalCount) { "Green" } else { "Yellow" })

if ($successCount -eq $totalCount) {
    Write-Host ""
    Write-Host "🎉 ALL TESTS PASSED! Work location validation has been successfully removed." -ForegroundColor Green
    Write-Host "Contracts can now be created without requiring work location fields." -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "⚠️  Some tests failed. Please check the error messages above." -ForegroundColor Yellow
    Write-Host "Make sure the customer-contract-service has been restarted with the latest changes." -ForegroundColor Yellow
}

Write-Host ""
