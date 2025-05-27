# Microservice Connectivity Test Script
# This script tests all microservice connections systematically

Write-Host "=== MICROSERVICE CONNECTIVITY TEST ===" -ForegroundColor Green
Write-Host ""

# Test 1: Check if all services are running on their assigned ports
Write-Host "1. CHECKING SERVICE PORTS..." -ForegroundColor Yellow
$ports = @(8080, 8081, 8082, 8083, 8084, 8085, 3000)
$services = @("API Gateway", "Customer Service", "Job Service", "Customer Contract Service", "Customer Payment Service", "Customer Statistics Service", "Frontend")

for ($i = 0; $i -lt $ports.Length; $i++) {
    $port = $ports[$i]
    $service = $services[$i]
    
    try {
        $connection = Test-NetConnection -ComputerName localhost -Port $port -WarningAction SilentlyContinue
        if ($connection.TcpTestSucceeded) {
            Write-Host "✓ $service (Port $port): RUNNING" -ForegroundColor Green
        } else {
            Write-Host "✗ $service (Port $port): NOT RUNNING" -ForegroundColor Red
        }
    } catch {
        Write-Host "✗ $service (Port $port): ERROR - $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""

# Test 2: Health Check Endpoints
Write-Host "2. TESTING HEALTH ENDPOINTS..." -ForegroundColor Yellow

$healthTests = @(
    @{Url="http://localhost:8080/actuator/health"; Name="API Gateway Health"},
    @{Url="http://localhost:8083/api/customer-contract/health"; Name="Customer Contract Service Health"},
    @{Url="http://localhost:8084/api/customer-payment/health"; Name="Customer Payment Service Health"},
    @{Url="http://localhost:8085/api/customer-statistics/health"; Name="Customer Statistics Service Health"}
)

foreach ($test in $healthTests) {
    try {
        $response = Invoke-WebRequest -Uri $test.Url -Method GET -TimeoutSec 5
        if ($response.StatusCode -eq 200) {
            Write-Host "✓ $($test.Name): OK" -ForegroundColor Green
        } else {
            Write-Host "✗ $($test.Name): Status $($response.StatusCode)" -ForegroundColor Red
        }
    } catch {
        Write-Host "✗ $($test.Name): ERROR - $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""

# Test 3: Basic Service Endpoints
Write-Host "3. TESTING BASIC SERVICE ENDPOINTS..." -ForegroundColor Yellow

$serviceTests = @(
    @{Url="http://localhost:8081/api/customer"; Name="Customer Service - Get All Customers"},
    @{Url="http://localhost:8082/api/job-category"; Name="Job Service - Get All Job Categories"},
    @{Url="http://localhost:8083/api/customer-contract"; Name="Customer Contract Service - Get All Contracts"},
    @{Url="http://localhost:8084/api/customer-payment"; Name="Customer Payment Service - Get All Payments"},
    @{Url="http://localhost:8085/api/customer-statistics"; Name="Customer Statistics Service - Root"}
)

foreach ($test in $serviceTests) {
    try {
        $response = Invoke-WebRequest -Uri $test.Url -Method GET -TimeoutSec 5
        if ($response.StatusCode -eq 200) {
            Write-Host "✓ $($test.Name): OK" -ForegroundColor Green
        } else {
            Write-Host "✗ $($test.Name): Status $($response.StatusCode)" -ForegroundColor Red
        }
    } catch {
        Write-Host "✗ $($test.Name): ERROR - $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""

# Test 4: API Gateway Routing
Write-Host "4. TESTING API GATEWAY ROUTING..." -ForegroundColor Yellow

$gatewayTests = @(
    @{Url="http://localhost:8080/api/customer"; Name="Customer Service via Gateway"},
    @{Url="http://localhost:8080/api/job-category"; Name="Job Service via Gateway"},
    @{Url="http://localhost:8080/api/customer-contract"; Name="Customer Contract Service via Gateway"},
    @{Url="http://localhost:8080/api/customer-payment"; Name="Customer Payment Service via Gateway"},
    @{Url="http://localhost:8080/api/customer-statistics"; Name="Customer Statistics Service via Gateway"},
    @{Url="http://localhost:8080/api/job-detail"; Name="Job Detail Service via Gateway"},
    @{Url="http://localhost:8080/api/work-shift"; Name="Work Shift Service via Gateway"}
)

foreach ($test in $gatewayTests) {
    try {
        $response = Invoke-WebRequest -Uri $test.Url -Method GET -TimeoutSec 5
        if ($response.StatusCode -eq 200) {
            Write-Host "✓ $($test.Name): OK" -ForegroundColor Green
        } else {
            Write-Host "✗ $($test.Name): Status $($response.StatusCode)" -ForegroundColor Red
        }
    } catch {
        Write-Host "✗ $($test.Name): ERROR - $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""

# Test 5: Inter-Service Communication
Write-Host "5. TESTING INTER-SERVICE COMMUNICATION..." -ForegroundColor Yellow

$interServiceTests = @(
    @{Url="http://localhost:8084/api/customer-payment/customer/search?fullName="; Name="Payment Service → Customer Service"},
    @{Url="http://localhost:8085/api/customer-statistics/revenue?startDate=2025-01-01&endDate=2025-12-31"; Name="Statistics Service → Multiple Services"},
    @{Url="http://localhost:8083/api/customer-contract/date-range?startDate=2025-01-01&endDate=2025-12-31"; Name="Contract Service - Date Range"}
)

foreach ($test in $interServiceTests) {
    try {
        $response = Invoke-WebRequest -Uri $test.Url -Method GET -TimeoutSec 10
        if ($response.StatusCode -eq 200) {
            Write-Host "✓ $($test.Name): OK" -ForegroundColor Green
        } else {
            Write-Host "✗ $($test.Name): Status $($response.StatusCode)" -ForegroundColor Red
        }
    } catch {
        Write-Host "✗ $($test.Name): ERROR - $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""

# Test 6: CORS Configuration
Write-Host "6. TESTING CORS CONFIGURATION..." -ForegroundColor Yellow

try {
    $corsResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/customer" -Method OPTIONS -Headers @{"Origin"="http://localhost:3000"; "Access-Control-Request-Method"="GET"; "Access-Control-Request-Headers"="X-Requested-With"} -TimeoutSec 5
    if ($corsResponse.StatusCode -eq 200 -and $corsResponse.Headers["Access-Control-Allow-Origin"] -eq "http://localhost:3000") {
        Write-Host "✓ CORS Configuration: OK" -ForegroundColor Green
    } else {
        Write-Host "✗ CORS Configuration: FAILED" -ForegroundColor Red
    }
} catch {
    Write-Host "✗ CORS Configuration: ERROR - $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 7: Frontend Connectivity
Write-Host "7. TESTING FRONTEND CONNECTIVITY..." -ForegroundColor Yellow

try {
    $frontendResponse = Invoke-WebRequest -Uri "http://localhost:3000" -Method GET -TimeoutSec 5
    if ($frontendResponse.StatusCode -eq 200) {
        Write-Host "✓ Frontend Application: RUNNING" -ForegroundColor Green
    } else {
        Write-Host "✗ Frontend Application: Status $($frontendResponse.StatusCode)" -ForegroundColor Red
    }
} catch {
    Write-Host "✗ Frontend Application: ERROR - $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== CONNECTIVITY TEST COMPLETED ===" -ForegroundColor Green
Write-Host ""
Write-Host "SUMMARY:" -ForegroundColor Cyan
Write-Host "- All microservices should be running on their assigned ports"
Write-Host "- API Gateway should route requests to all services correctly"
Write-Host "- Inter-service communication should work via Feign clients"
Write-Host "- CORS should be properly configured for frontend access"
Write-Host "- Frontend should be able to connect to API Gateway"
Write-Host ""
Write-Host "If any tests failed, check the specific service logs for detailed error messages."
