# Deploy microservices now
Write-Host "=== DEPLOYING MICROSERVICES ===" -ForegroundColor Green
Write-Host ""

# Function to build Maven projects
function Build-MavenProject {
    param(
        [string]$ProjectPath,
        [string]$ProjectName
    )

    Write-Host "Building $ProjectName..." -ForegroundColor Yellow
    Set-Location $ProjectPath

    mvn clean package -DskipTests

    if ($LASTEXITCODE -eq 0) {
        Write-Host "$ProjectName built successfully!" -ForegroundColor Green
    } else {
        Write-Host "Failed to build $ProjectName" -ForegroundColor Red
        Set-Location ..
        exit 1
    }

    Set-Location ..
}

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

# Build all Docker images
Write-Host "Building Docker images..." -ForegroundColor Yellow
docker-compose build

if ($LASTEXITCODE -eq 0) {
    Write-Host "All Docker images built successfully!" -ForegroundColor Green
} else {
    Write-Host "Failed to build Docker images" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "3. DEPLOYING MICROSERVICES..." -ForegroundColor Cyan

# Deploy all services
Write-Host "Starting all microservices..." -ForegroundColor Yellow
docker-compose up -d

if ($LASTEXITCODE -eq 0) {
    Write-Host "All microservices deployed successfully!" -ForegroundColor Green
} else {
    Write-Host "Failed to deploy microservices" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "4. CHECKING SERVICE STATUS..." -ForegroundColor Cyan

# Wait for services to start
Start-Sleep -Seconds 10

# Check container status
Write-Host "Container Status:" -ForegroundColor Yellow
docker-compose ps

Write-Host ""
Write-Host "DEPLOYMENT COMPLETED!" -ForegroundColor Green
Write-Host ""
Write-Host "SERVICES:" -ForegroundColor Cyan
Write-Host "  API Gateway:                  http://localhost:8080" -ForegroundColor White
Write-Host "  Customer Service:             http://localhost:8081" -ForegroundColor White
Write-Host "  Job Service:                  http://localhost:8082" -ForegroundColor White
Write-Host "  Customer Contract Service:    http://localhost:8083" -ForegroundColor White
Write-Host "  Customer Payment Service:     http://localhost:8084" -ForegroundColor White
Write-Host "  Customer Statistics Service:  http://localhost:8085" -ForegroundColor White
Write-Host ""
Write-Host "To check service health: .\test-services.ps1" -ForegroundColor Yellow
