# PowerShell script to build all Docker images for microservices

Write-Host "Building Docker images for all microservices..." -ForegroundColor Green

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
        Write-Host "$ProjectName built successfully!" -ForegroundColor Green
    } else {
        Write-Host "Failed to build $ProjectName" -ForegroundColor Red
        exit 1
    }

    Set-Location ..
}

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

# Build all Docker images using docker-compose
Write-Host "Building Docker images..." -ForegroundColor Yellow
docker-compose build

if ($LASTEXITCODE -eq 0) {
    Write-Host "All Docker images built successfully!" -ForegroundColor Green
    Write-Host "You can now run: docker-compose up -d" -ForegroundColor Cyan
} else {
    Write-Host "Failed to build Docker images" -ForegroundColor Red
    exit 1
}

Write-Host "Build process completed!" -ForegroundColor Green
