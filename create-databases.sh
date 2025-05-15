#!/bin/bash

# Đợi PostgreSQL khởi động
echo "Waiting for PostgreSQL to start..."
sleep 10

# Tạo các database
echo "Creating databases..."
docker exec postgres psql -U postgres -c "CREATE DATABASE \"customerMSDb\";"
docker exec postgres psql -U postgres -c "CREATE DATABASE \"jobMSDb\";"
docker exec postgres psql -U postgres -c "CREATE DATABASE \"customerContractMSDb\";"
docker exec postgres psql -U postgres -c "CREATE DATABASE \"customerPaymentMSDb\";"

echo "Databases created successfully!"

# Khởi động lại các service
echo "Restarting services..."
docker restart customer-service
docker restart job-service
docker restart customer-contract-service
docker restart customer-payment-service
docker restart customer-statistics-service
docker restart api-gateway

echo "All services restarted!"
