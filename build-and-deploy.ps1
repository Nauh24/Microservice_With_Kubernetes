# PowerShell script to build and deploy all microservices to Docker
# This script builds all services and deploys them with external PostgreSQL connection

Write-Host "=== MICROSERVICE DOCKER BUILD AND DEPLOYMENT ===" -ForegroundColor Green
Write-Host ""

# Function to build Maven projects
function Build-MavenProject {
    param(
        [string]$ProjectPath,
        [string]$ProjectName
    )

    Write-Host "Building $ProjectName..." -ForegroundColor Yellow
    Set-Location $ProjectPath

    # Clean and package the project
    mvn clean package -DskipTests

    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ $ProjectName built successfully!" -ForegroundColor Green
    } else {
        Write-Host "✗ Failed to build $ProjectName" -ForegroundColor Red
        exit 1
    }

    Set-Location ..
}

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
            Write-Host "Please start PostgreSQL before continuing." -ForegroundColor Yellow
            return $false
        }
    } catch {
        Write-Host "✗ Error checking PostgreSQL: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# Check PostgreSQL connection
if (-not (Test-PostgreSQLConnection)) {
    Write-Host ""
    Write-Host "PREREQUISITES:" -ForegroundColor Cyan
    Write-Host "1. Start PostgreSQL service" -ForegroundColor White
    Write-Host "2. Ensure databases exist: customerdb, jobdb, customercontractdb, customerpaymentdb" -ForegroundColor White
    Write-Host "3. Run database-setup.sql script to clear data and setup Vietnamese job categories" -ForegroundColor White
    Write-Host ""
    Write-Host "To run database setup:" -ForegroundColor Yellow
    Write-Host "psql -U postgres -f database-setup.sql" -ForegroundColor White
    exit 1
}

Write-Host ""
Write-Host "1. BUILDING SPRING BOOT MICROSERVICES..." -ForegroundColor Cyan

# Build all Spring Boot microservices
$services = @(
    "api-gateway",
    "customer-service",
    "job-service",
    "customer-contract-service",
    "customer-payment-service",
    "customer-statistics-service"
)

foreach ($service in $services) {
    Build-MavenProject -ProjectPath $service -ProjectName $service
}

Write-Host ""
Write-Host "2. BUILDING DOCKER IMAGES..." -ForegroundColor Cyan

# Stop existing containers
Write-Host "Stopping existing containers..." -ForegroundColor Yellow
docker-compose down

# Build all Docker images using docker-compose
Write-Host "Building Docker images..." -ForegroundColor Yellow
docker-compose build

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ All Docker images built successfully!" -ForegroundColor Green
} else {
    Write-Host "✗ Failed to build Docker images" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "3. DEPLOYING MICROSERVICES..." -ForegroundColor Cyan

# Deploy all services
Write-Host "Starting all microservices..." -ForegroundColor Yellow
docker-compose up -d

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ All microservices deployed successfully!" -ForegroundColor Green
} else {
    Write-Host "✗ Failed to deploy microservices" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "4. CHECKING SERVICE STATUS..." -ForegroundColor Cyan

# Wait a moment for services to start
Start-Sleep -Seconds 10

# Check container status
Write-Host "Container Status:" -ForegroundColor Yellow
docker-compose ps

Write-Host ""
Write-Host "🎉 DEPLOYMENT COMPLETED SUCCESSFULLY!" -ForegroundColor Green
Write-Host ""
Write-Host "SERVICES ACCESSIBLE AT:" -ForegroundColor Cyan
Write-Host "  API Gateway:                  http://localhost:8080" -ForegroundColor White
Write-Host "  Customer Service:             http://localhost:8081" -ForegroundColor White
Write-Host "  Job Service:                  http://localhost:8082" -ForegroundColor White
Write-Host "  Customer Contract Service:    http://localhost:8083" -ForegroundColor White
Write-Host "  Customer Payment Service:     http://localhost:8084" -ForegroundColor White
Write-Host "  Customer Statistics Service:  http://localhost:8085" -ForegroundColor White
Write-Host ""
Write-Host "DATABASE CONFIGURATION:" -ForegroundColor Cyan
Write-Host "  Host: localhost:5432" -ForegroundColor White
Write-Host "  Databases: customerdb, jobdb, customercontractdb, customerpaymentdb" -ForegroundColor White
Write-Host "  Username: postgres" -ForegroundColor White
Write-Host "  Password: 1234" -ForegroundColor White
Write-Host ""
Write-Host "To check service health: .\test-services.ps1" -ForegroundColor Yellow
Write-Host "To view logs: docker-compose logs -f [service-name]" -ForegroundColor Yellow
Write-Host "To stop all services: docker-compose down" -ForegroundColor Yellow
