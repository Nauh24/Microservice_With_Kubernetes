#!/bin/bash

echo "=== Contract Creation Debug Script ==="
echo ""

# Check if services are running
echo "1. Checking service status..."
echo "Customer Service (8085):"
curl -s http://localhost:8085/actuator/health || echo "❌ Customer Service not responding"

echo "Job Service (8086):"
curl -s http://localhost:8086/actuator/health || echo "❌ Job Service not responding"

echo "Customer Contract Service (8087):"
curl -s http://localhost:8087/actuator/health || echo "❌ Customer Contract Service not responding"

echo "API Gateway (8083):"
curl -s http://localhost:8083/actuator/health || echo "❌ API Gateway not responding"

echo ""

# Check database connection
echo "2. Checking database connection..."
docker exec postgres psql -U postgres -d customerContractMSDb -c "SELECT 1;" > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo "✅ Database connection successful"
else
    echo "❌ Database connection failed"
fi

echo ""

# Check work_shifts table structure
echo "3. Checking work_shifts table structure..."
docker exec postgres psql -U postgres -d customerContractMSDb -c "
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'work_shifts' 
ORDER BY ordinal_position;
"

echo ""

# Test API endpoints
echo "4. Testing API endpoints..."

echo "Testing GET /api/customer (via API Gateway):"
curl -s -w "HTTP Status: %{http_code}\n" http://localhost:8083/api/customer | head -5

echo ""
echo "Testing GET /api/job-category (via API Gateway):"
curl -s -w "HTTP Status: %{http_code}\n" http://localhost:8083/api/job-category | head -5

echo ""
echo "Testing GET /api/customer-contract (via API Gateway):"
curl -s -w "HTTP Status: %{http_code}\n" http://localhost:8083/api/customer-contract | head -5

echo ""

# Test contract creation with minimal data
echo "5. Testing contract creation with minimal valid data..."

# First, get a customer ID
CUSTOMER_ID=$(curl -s http://localhost:8083/api/customer | jq -r '.[0].id // empty' 2>/dev/null)
if [ -z "$CUSTOMER_ID" ]; then
    echo "❌ No customers found. Creating a test customer first..."
    CUSTOMER_RESPONSE=$(curl -s -X POST http://localhost:8083/api/customer \
        -H "Content-Type: application/json" \
        -d '{
            "fullName": "Test Customer",
            "companyName": "Test Company",
            "phoneNumber": "0123456789",
            "email": "test@example.com",
            "address": "Test Address"
        }')
    CUSTOMER_ID=$(echo $CUSTOMER_RESPONSE | jq -r '.id // empty' 2>/dev/null)
fi

# Get a job category ID
JOB_CATEGORY_ID=$(curl -s http://localhost:8083/api/job-category | jq -r '.[0].id // empty' 2>/dev/null)
if [ -z "$JOB_CATEGORY_ID" ]; then
    echo "❌ No job categories found. Creating a test job category first..."
    JOB_CATEGORY_RESPONSE=$(curl -s -X POST http://localhost:8083/api/job-category \
        -H "Content-Type: application/json" \
        -d '{
            "name": "Test Job Category",
            "description": "Test Description"
        }')
    JOB_CATEGORY_ID=$(echo $JOB_CATEGORY_RESPONSE | jq -r '.id // empty' 2>/dev/null)
fi

if [ -n "$CUSTOMER_ID" ] && [ -n "$JOB_CATEGORY_ID" ]; then
    echo "Using Customer ID: $CUSTOMER_ID"
    echo "Using Job Category ID: $JOB_CATEGORY_ID"
    
    echo "Creating test contract..."
    CONTRACT_RESPONSE=$(curl -s -X POST http://localhost:8083/api/customer-contract \
        -H "Content-Type: application/json" \
        -w "HTTP Status: %{http_code}\n" \
        -d "{
            \"customerId\": $CUSTOMER_ID,
            \"startingDate\": \"$(date -d '+1 day' '+%Y-%m-%d')\",
            \"endingDate\": \"$(date -d '+30 days' '+%Y-%m-%d')\",
            \"totalAmount\": 1000000,
            \"address\": \"Test Work Address\",
            \"description\": \"Test Contract\",
            \"jobDetails\": [
                {
                    \"jobCategoryId\": $JOB_CATEGORY_ID,
                    \"startDate\": \"$(date -d '+1 day' '+%Y-%m-%d')\",
                    \"endDate\": \"$(date -d '+30 days' '+%Y-%m-%d')\",
                    \"workLocation\": \"Test Location\",
                    \"workShifts\": [
                        {
                            \"startTime\": \"08:00\",
                            \"endTime\": \"17:00\",
                            \"numberOfWorkers\": 5,
                            \"salary\": 500000,
                            \"workingDays\": \"1,2,3,4,5\"
                        }
                    ]
                }
            ]
        }")
    
    echo "Contract creation response:"
    echo "$CONTRACT_RESPONSE"
else
    echo "❌ Could not get customer ID or job category ID for testing"
fi

echo ""
echo "=== Debug Complete ==="
