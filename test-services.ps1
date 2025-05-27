# PowerShell script to test all microservices

Write-Host "Testing all microservices..." -ForegroundColor Green
Write-Host ""

# Function to test service health
function Test-ServiceHealth {
    param(
        [string]$ServiceName,
        [string]$Url
    )
    
    try {
        $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 10
        if ($response.StatusCode -eq 200) {
            Write-Host "✅ $ServiceName is healthy" -ForegroundColor Green
            return $true
        } else {
            Write-Host "❌ $ServiceName returned status: $($response.StatusCode)" -ForegroundColor Red
            return $false
        }
    } catch {
        Write-Host "❌ $ServiceName is not responding: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# Test all services
$services = @(
    @{ Name = "Frontend"; Url = "http://localhost:3000" },
    @{ Name = "API Gateway"; Url = "http://localhost:8080/actuator/health" },
    @{ Name = "Customer Service"; Url = "http://localhost:8081/actuator/health" },
    @{ Name = "Job Service"; Url = "http://localhost:8082/actuator/health" },
    @{ Name = "Customer Contract Service"; Url = "http://localhost:8083/actuator/health" },
    @{ Name = "Customer Payment Service"; Url = "http://localhost:8084/actuator/health" },
    @{ Name = "Customer Statistics Service"; Url = "http://localhost:8085/actuator/health" }
)

$allHealthy = $true

foreach ($service in $services) {
    $isHealthy = Test-ServiceHealth -ServiceName $service.Name -Url $service.Url
    if (-not $isHealthy) {
        $allHealthy = $false
    }
    Start-Sleep -Seconds 1
}

Write-Host ""
if ($allHealthy) {
    Write-Host "🎉 All services are running successfully!" -ForegroundColor Green
    Write-Host ""
    Write-Host "You can access:" -ForegroundColor Cyan
    Write-Host "  Frontend:                     http://localhost:3000" -ForegroundColor White
    Write-Host "  API Gateway:                  http://localhost:8080" -ForegroundColor White
    Write-Host "  Customer Service:             http://localhost:8081" -ForegroundColor White
    Write-Host "  Job Service:                  http://localhost:8082" -ForegroundColor White
    Write-Host "  Customer Contract Service:    http://localhost:8083" -ForegroundColor White
    Write-Host "  Customer Payment Service:     http://localhost:8084" -ForegroundColor White
    Write-Host "  Customer Statistics Service:  http://localhost:8085" -ForegroundColor White
} else {
    Write-Host "⚠️  Some services are not healthy. Check the logs with:" -ForegroundColor Yellow
    Write-Host "  docker-compose logs [service-name]" -ForegroundColor White
}
