#!/usr/bin/env pwsh

# Script to test all API Gateway endpoints
Write-Host "🧪 Testing API Gateway Endpoints..." -ForegroundColor Green

$apiGatewayUrl = "http://localhost:8080"

# Function to test an endpoint
function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Url,
        [string]$Method = "GET"
    )

    Write-Host "Testing $Name..." -ForegroundColor Yellow
    try {
        $response = Invoke-WebRequest -Uri "$apiGatewayUrl$Url" -Method $Method -UseBasicParsing -TimeoutSec 10
        if ($response.StatusCode -eq 200) {
            Write-Host "✅ $Name - Status: $($response.StatusCode)" -ForegroundColor Green

            # Show content length for data endpoints
            if ($response.Content.Length -gt 0) {
                $contentLength = $response.Content.Length
                Write-Host "   📊 Response size: $contentLength bytes" -ForegroundColor Cyan

                # Try to parse as JSON and show count if it's an array
                try {
                    $jsonData = $response.Content | ConvertFrom-Json
                    if ($jsonData -is [array]) {
                        Write-Host "   📋 Records count: $($jsonData.Count)" -ForegroundColor Cyan
                    }
                } catch {
                    # Not JSON or not an array, that's fine
                }
            }
        } else {
            Write-Host "⚠️ $Name - Status: $($response.StatusCode)" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "❌ $Name - Error: $($_.Exception.Message)" -ForegroundColor Red
    }
    Write-Host ""
}

Write-Host "🔍 Testing API Gateway Health..." -ForegroundColor Cyan
Test-Endpoint -Name "API Gateway Health" -Url "/actuator/health"

Write-Host "👥 Testing Customer Service Endpoints..." -ForegroundColor Cyan
Test-Endpoint -Name "Customer List" -Url "/api/customer"

Write-Host "💼 Testing Job Service Endpoints..." -ForegroundColor Cyan
Test-Endpoint -Name "Job Category List" -Url "/api/job-category"

Write-Host "📋 Testing Customer Contract Service Endpoints..." -ForegroundColor Cyan
Test-Endpoint -Name "Customer Contract List" -Url "/api/customer-contract"
Test-Endpoint -Name "Job Detail List" -Url "/api/job-detail"
Test-Endpoint -Name "Work Shift List" -Url "/api/work-shift"

Write-Host "💰 Testing Customer Payment Service Endpoints..." -ForegroundColor Cyan
Test-Endpoint -Name "Customer Payment List" -Url "/api/customer-payment"

Write-Host "📊 Testing Customer Statistics Service Endpoints..." -ForegroundColor Cyan
Test-Endpoint -Name "Customer Statistics Health" -Url "/api/customer-statistics/health"

# Test with date parameters for statistics
$startDate = (Get-Date).AddDays(-30).ToString("yyyy-MM-dd")
$endDate = (Get-Date).ToString("yyyy-MM-dd")
Test-Endpoint -Name "Customer Revenue Statistics" -Url "/api/customer-statistics/revenue?startDate=$startDate`&endDate=$endDate"

Write-Host "🎉 API Gateway Testing Complete!" -ForegroundColor Green
Write-Host ""
Write-Host "🌐 Frontend is available at: http://localhost:3000" -ForegroundColor Magenta
Write-Host "🔗 API Gateway is available at: http://localhost:8080" -ForegroundColor Magenta
Write-Host ""
Write-Host "All services should now be working correctly!" -ForegroundColor Green
