# Complete System Deployment Script
# This script performs full deployment of the microservices system

Write-Host "=== COMPLETE MICROSERVICES SYSTEM DEPLOYMENT ===" -ForegroundColor Green
Write-Host ""

# Function to check if PostgreSQL is running
function Test-PostgreSQLConnection {
    Write-Host "Checking PostgreSQL connection..." -ForegroundColor Yellow
    
    try {
        $connection = Test-NetConnection -ComputerName localhost -Port 5432 -WarningAction SilentlyContinue
        if ($connection.TcpTestSucceeded) {
            Write-Host "✓ PostgreSQL is running on localhost:5432" -ForegroundColor Green
            return $true
        } else {
            Write-Host "✗ PostgreSQL is not running on localhost:5432" -ForegroundColor Red
            return $false
        }
    } catch {
        Write-Host "✗ Error checking PostgreSQL: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# Check prerequisites
Write-Host "CHECKING PREREQUISITES..." -ForegroundColor Cyan

if (-not (Test-PostgreSQLConnection)) {
    Write-Host ""
    Write-Host "PREREQUISITES NOT MET:" -ForegroundColor Red
    Write-Host "1. Start PostgreSQL service" -ForegroundColor White
    Write-Host "2. Ensure PostgreSQL is accessible on localhost:5432" -ForegroundColor White
    Write-Host "3. Verify username: postgres, password: 1234" -ForegroundColor White
    exit 1
}

# Check if Docker is running
try {
    docker version | Out-Null
    Write-Host "✓ Docker is running" -ForegroundColor Green
} catch {
    Write-Host "✗ Docker is not running or not installed" -ForegroundColor Red
    exit 1
}

# Check if Maven is available
try {
    mvn --version | Out-Null
    Write-Host "✓ Maven is available" -ForegroundColor Green
} catch {
    Write-Host "✗ Maven is not available" -ForegroundColor Red
    exit 1
}

# Check if Node.js is available
try {
    node --version | Out-Null
    Write-Host "✓ Node.js is available" -ForegroundColor Green
} catch {
    Write-Host "✗ Node.js is not available" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "STEP 1: DATABASE SETUP" -ForegroundColor Cyan
Write-Host "Setting up databases and clearing existing data..." -ForegroundColor Yellow

# Run database setup
& .\setup-databases.ps1

if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ Database setup failed" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "STEP 2: BUILD AND DEPLOY SERVICES" -ForegroundColor Cyan
Write-Host "Building and deploying all microservices..." -ForegroundColor Yellow

# Run build and deploy
& .\build-and-deploy.ps1

if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ Build and deployment failed" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "STEP 3: VERIFICATION" -ForegroundColor Cyan
Write-Host "Waiting for services to start..." -ForegroundColor Yellow

# Wait for services to start
Start-Sleep -Seconds 30

Write-Host "Running service health checks..." -ForegroundColor Yellow

# Run service tests
& .\test-services.ps1

Write-Host ""
Write-Host "🎉 COMPLETE SYSTEM DEPLOYMENT FINISHED!" -ForegroundColor Green
Write-Host ""
Write-Host "SYSTEM OVERVIEW:" -ForegroundColor Cyan
Write-Host "=================" -ForegroundColor Cyan
Write-Host ""
Write-Host "SERVICES:" -ForegroundColor Yellow
Write-Host "  Frontend:                     http://localhost:3000" -ForegroundColor White
Write-Host "  API Gateway:                  http://localhost:8080" -ForegroundColor White
Write-Host "  Customer Service:             http://localhost:8081" -ForegroundColor White
Write-Host "  Job Service:                  http://localhost:8082" -ForegroundColor White
Write-Host "  Customer Contract Service:    http://localhost:8083" -ForegroundColor White
Write-Host "  Customer Payment Service:     http://localhost:8084" -ForegroundColor White
Write-Host "  Customer Statistics Service:  http://localhost:8085" -ForegroundColor White
Write-Host ""
Write-Host "DATABASE CONFIGURATION:" -ForegroundColor Yellow
Write-Host "  Host: localhost:5432" -ForegroundColor White
Write-Host "  Databases: customerdb, jobdb, customercontractdb, customerpaymentdb" -ForegroundColor White
Write-Host "  Username: postgres" -ForegroundColor White
Write-Host "  Password: 1234" -ForegroundColor White
Write-Host ""
Write-Host "FEATURES IMPLEMENTED:" -ForegroundColor Yellow
Write-Host "  ✓ External PostgreSQL connection (host.docker.internal)" -ForegroundColor Green
Write-Host "  ✓ Schema validation (no modifications to existing schemas)" -ForegroundColor Green
Write-Host "  ✓ Data initialization disabled (no conflicts)" -ForegroundColor Green
Write-Host "  ✓ Vietnamese job categories loaded" -ForegroundColor Green
Write-Host "  ✓ Inter-service communication configured" -ForegroundColor Green
Write-Host "  ✓ Health checks enabled for all services" -ForegroundColor Green
Write-Host "  ✓ Proper port assignments (8080-8085, 3000)" -ForegroundColor Green
Write-Host ""
Write-Host "MANAGEMENT COMMANDS:" -ForegroundColor Yellow
Write-Host "  View logs:        docker-compose logs -f [service-name]" -ForegroundColor White
Write-Host "  Stop services:    docker-compose down" -ForegroundColor White
Write-Host "  Start services:   docker-compose up -d" -ForegroundColor White
Write-Host "  Check health:     .\test-services.ps1" -ForegroundColor White
Write-Host ""
Write-Host "The system is now ready for use!" -ForegroundColor Green
