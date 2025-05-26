#!/bin/bash

echo "=== Checking and Fixing Database Schema ==="

# Check if salary column exists
echo "Checking if salary column exists in work_shifts table..."
COLUMN_EXISTS=$(docker exec postgres psql -U postgres -d customerContractMSDb -t -c "
SELECT COUNT(*) 
FROM information_schema.columns 
WHERE table_name = 'work_shifts' AND column_name = 'salary';
" | tr -d ' ')

if [ "$COLUMN_EXISTS" = "0" ]; then
    echo "❌ Salary column does not exist. Adding it now..."
    
    docker exec postgres psql -U postgres -d customerContractMSDb -c "
    -- Add salary column to work_shifts table
    ALTER TABLE work_shifts ADD COLUMN salary DOUBLE PRECISION;
    
    -- Set default value for existing records
    UPDATE work_shifts SET salary = 0.0 WHERE salary IS NULL;
    
    -- Verify the column was added
    SELECT 'Salary column added successfully' as result;
    "
    
    if [ $? -eq 0 ]; then
        echo "✅ Salary column added successfully"
    else
        echo "❌ Failed to add salary column"
        exit 1
    fi
else
    echo "✅ Salary column already exists"
fi

# Show current table structure
echo ""
echo "Current work_shifts table structure:"
docker exec postgres psql -U postgres -d customerContractMSDb -c "
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns 
WHERE table_name = 'work_shifts' 
ORDER BY ordinal_position;
"

# Check if there are any existing work_shifts with NULL salary
echo ""
echo "Checking for work_shifts with NULL salary..."
NULL_SALARY_COUNT=$(docker exec postgres psql -U postgres -d customerContractMSDb -t -c "
SELECT COUNT(*) FROM work_shifts WHERE salary IS NULL;
" | tr -d ' ')

if [ "$NULL_SALARY_COUNT" != "0" ]; then
    echo "Found $NULL_SALARY_COUNT work_shifts with NULL salary. Updating them..."
    docker exec postgres psql -U postgres -d customerContractMSDb -c "
    UPDATE work_shifts SET salary = 0.0 WHERE salary IS NULL;
    SELECT 'Updated ' || ROW_COUNT() || ' records' as result;
    "
else
    echo "✅ No work_shifts with NULL salary found"
fi

echo ""
echo "=== Database Check Complete ==="
