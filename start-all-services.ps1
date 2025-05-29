#!/usr/bin/env pwsh

# Script to start all microservices locally for development
Write-Host "🚀 Starting All Microservices Locally..." -ForegroundColor Green

# Function to start a service in a new terminal
function Start-ServiceInNewTerminal {
    param(
        [string]$ServiceName,
        [string]$ServicePath,
        [int]$Port
    )
    
    Write-Host "Starting $ServiceName on port $Port..." -ForegroundColor Yellow
    
    # Start the service in a new PowerShell window
    Start-Process powershell -ArgumentList @(
        "-NoExit",
        "-Command",
        "cd '$ServicePath'; Write-Host 'Starting $ServiceName on port $Port...' -ForegroundColor Green; mvn spring-boot:run"
    ) -WindowStyle Normal
    
    # Wait a bit before starting the next service
    Start-Sleep -Seconds 3
}

# Check if Maven is available
try {
    mvn --version | Out-Null
    Write-Host "✅ Maven is available" -ForegroundColor Green
} catch {
    Write-Host "❌ Maven is not available. Please install Maven first." -ForegroundColor Red
    exit 1
}

# Get the current directory
$rootDir = Get-Location

# Start all microservices
Write-Host "Starting microservices in order..." -ForegroundColor Cyan

# 1. Customer Service
Start-ServiceInNewTerminal -ServiceName "Customer Service" -ServicePath "$rootDir\customer-service" -Port 8081

# 2. Job Service  
Start-ServiceInNewTerminal -ServiceName "Job Service" -ServicePath "$rootDir\job-service" -Port 8082

# 3. Customer Contract Service
Start-ServiceInNewTerminal -ServiceName "Customer Contract Service" -ServicePath "$rootDir\customer-contract-service" -Port 8083

# 4. Customer Payment Service
Start-ServiceInNewTerminal -ServiceName "Customer Payment Service" -ServicePath "$rootDir\customer-payment-service" -Port 8084

# 5. Customer Statistics Service
Start-ServiceInNewTerminal -ServiceName "Customer Statistics Service" -ServicePath "$rootDir\customer-statistics-service" -Port 8085

# 6. API Gateway (last)
Start-ServiceInNewTerminal -ServiceName "API Gateway" -ServicePath "$rootDir\api-gateway" -Port 8080

Write-Host ""
Write-Host "🎉 All services are starting up!" -ForegroundColor Green
Write-Host ""
Write-Host "Services will be available on:" -ForegroundColor Cyan
Write-Host "  • API Gateway: http://localhost:8080" -ForegroundColor White
Write-Host "  • Customer Service: http://localhost:8081" -ForegroundColor White
Write-Host "  • Job Service: http://localhost:8082" -ForegroundColor White
Write-Host "  • Customer Contract Service: http://localhost:8083" -ForegroundColor White
Write-Host "  • Customer Payment Service: http://localhost:8084" -ForegroundColor White
Write-Host "  • Customer Statistics Service: http://localhost:8085" -ForegroundColor White
Write-Host ""
Write-Host "⏳ Please wait 2-3 minutes for all services to fully start up..." -ForegroundColor Yellow
Write-Host ""
Write-Host "🌐 Frontend will connect to API Gateway at: http://localhost:8080" -ForegroundColor Magenta
Write-Host ""
Write-Host "To start the frontend, run:" -ForegroundColor Cyan
Write-Host "  cd microservice_fe" -ForegroundColor White
Write-Host "  npm start" -ForegroundColor White
Write-Host ""
Write-Host "Press any key to continue..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
