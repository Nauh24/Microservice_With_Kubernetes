# Docker Deployment Guide for Microservices

This guide provides step-by-step instructions to build and deploy all microservices to Docker containers with external PostgreSQL database connection.

## Prerequisites

### 1. Software Requirements
- **Docker Desktop** (latest version)
- **PostgreSQL** (running on host machine via pgAdmin 4)
- **Java 17** (for building Spring Boot services)
- **Node.js** (for building React frontend)
- **Maven** (for building Spring Boot services)
- **PowerShell** (for running deployment scripts)

### 2. PostgreSQL Setup
Ensure PostgreSQL is running on your desktop with the following configuration:
- **Host**: localhost
- **Port**: 5432
- **Username**: postgres
- **Password**: 1234

### 3. Database Creation
Create the following databases in PostgreSQL:
```sql
CREATE DATABASE customerdb;
CREATE DATABASE jobdb;
CREATE DATABASE customercontractdb;
CREATE DATABASE customerpaymentdb;
```

## Deployment Steps

### Step 1: Database Setup and Data Clearing

1. **Clear existing data and setup Vietnamese job categories:**
   ```powershell
   psql -U postgres -f database-setup.sql
   ```

   This script will:
   - Clear all existing job/work data
   - Clear existing contract and payment data
   - Insert Vietnamese job categories
   - Preserve existing customer data (optional)

### Step 2: Build and Deploy All Services

1. **Run the automated build and deployment script:**
   ```powershell
   .\build-and-deploy.ps1
   ```

   This script will:
   - Check PostgreSQL connection
   - Build all Spring Boot microservices
   - Build React frontend
   - Create Docker images
   - Deploy all services with docker-compose

### Step 3: Verify Deployment

1. **Check service status:**
   ```powershell
   .\test-services.ps1
   ```

2. **View container status:**
   ```powershell
   docker-compose ps
   ```

3. **Check service logs:**
   ```powershell
   docker-compose logs -f [service-name]
   ```

## Service Configuration

### Port Assignments
- **API Gateway**: 8080
- **Customer Service**: 8081
- **Job Service**: 8082
- **Customer Contract Service**: 8083
- **Customer Payment Service**: 8084
- **Customer Statistics Service**: 8085
- **Frontend**: 3000

### Database Configuration
Each service connects to its own database:
- **Customer Service** → `customerdb`
- **Job Service** → `jobdb`
- **Customer Contract Service** → `customercontractdb`
- **Customer Payment Service** → `customerpaymentdb`
- **Customer Statistics Service** → No database (aggregates data from other services)

### Docker Configuration Features
- **External PostgreSQL Connection**: Services connect to host PostgreSQL via `host.docker.internal`
- **Schema Validation**: `spring.jpa.hibernate.ddl-auto=validate` prevents schema modifications
- **Data Initialization Disabled**: `spring.sql.init.mode=never` prevents conflicts
- **Health Checks**: All services include Docker health checks
- **Inter-service Communication**: Services communicate via Docker network

## Access URLs

After successful deployment, access the services at:

- **Frontend**: http://localhost:3000
- **API Gateway**: http://localhost:8080
- **Customer Service**: http://localhost:8081/actuator/health
- **Job Service**: http://localhost:8082/actuator/health
- **Customer Contract Service**: http://localhost:8083/actuator/health
- **Customer Payment Service**: http://localhost:8084/actuator/health
- **Customer Statistics Service**: http://localhost:8085/actuator/health

## Management Commands

### Start Services
```powershell
docker-compose up -d
```

### Stop Services
```powershell
docker-compose down
```

### View Logs
```powershell
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f customer-service
```

### Rebuild Services
```powershell
# Rebuild all
docker-compose build

# Rebuild specific service
docker-compose build customer-service
```

### Scale Services (if needed)
```powershell
docker-compose up -d --scale customer-service=2
```

## Troubleshooting

### Common Issues

1. **PostgreSQL Connection Failed**
   - Ensure PostgreSQL is running on localhost:5432
   - Check username/password (postgres/1234)
   - Verify databases exist

2. **Service Health Check Failed**
   - Wait 60 seconds for services to fully start
   - Check service logs: `docker-compose logs [service-name]`
   - Verify database connectivity

3. **Port Already in Use**
   - Stop existing services: `docker-compose down`
   - Check for other applications using the ports
   - Kill processes if necessary

4. **Build Failures**
   - Ensure Java 17 is installed
   - Run `mvn clean install` manually for each service
   - Check for compilation errors

### Logs and Monitoring

- **Service Health**: http://localhost:808X/actuator/health
- **Service Info**: http://localhost:808X/actuator/info
- **Metrics**: http://localhost:808X/actuator/metrics

## Vietnamese Job Categories

The system includes the following Vietnamese job categories:
- Công nhân xây dựng
- Thợ điện
- Thợ nước
- Thợ hàn
- Thợ sơn
- Công nhân vận chuyển
- Thợ mộc
- Thợ ốp lát
- Công nhân dọn dẹp
- Thợ cơ khí
- Công nhân bảo vệ
- Thợ làm vườn
- Công nhân kho bãi
- Thợ lái xe
- Công nhân phụ việc
- Nhân viên văn phòng
- Kỹ thuật viên
- Thợ máy
- Công nhân an toàn
- Thợ cắt gạch

## Support

For issues or questions:
1. Check service logs
2. Verify database connectivity
3. Ensure all prerequisites are met
4. Run health checks on individual services
