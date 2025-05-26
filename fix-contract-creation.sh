#!/bin/bash

echo "Fixing contract creation issue..."

# Step 1: Add salary column to work_shifts table
echo "Step 1: Adding salary column to work_shifts table..."
docker exec postgres psql -U postgres -d customerContractMSDb -f /tmp/add-salary-column.sql

# Copy the SQL file to the container first
docker cp add-salary-column.sql postgres:/tmp/add-salary-column.sql

# Execute the SQL script
docker exec postgres psql -U postgres -d customerContractMSDb -c "
-- Add salary column to work_shifts table if it doesn't exist
ALTER TABLE work_shifts ADD COLUMN IF NOT EXISTS salary DOUBLE PRECISION;

-- Set default value for existing records (0.0)
UPDATE work_shifts SET salary = 0.0 WHERE salary IS NULL;

-- Verify the column was added
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'work_shifts' AND column_name = 'salary';
"

echo "Step 2: Restarting customer-contract-service..."
# Restart the customer-contract-service to pick up the new validation
docker restart customer-contract-service

echo "Step 3: Waiting for service to start..."
sleep 10

echo "Contract creation fix completed!"
echo "The following changes were made:"
echo "1. Added salary column to work_shifts table"
echo "2. Added comprehensive validation for JobDetail and WorkShift fields"
echo "3. Restarted customer-contract-service"
echo ""
echo "You can now try creating a contract again."
