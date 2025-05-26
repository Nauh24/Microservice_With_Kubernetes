-- Add salary column to work_shifts table
-- This script adds the salary column that was added to the WorkShift entity

-- Add salary column to work_shifts table if it doesn't exist
ALTER TABLE work_shifts ADD COLUMN IF NOT EXISTS salary DOUBLE PRECISION;

-- Set default value for existing records (0.0)
UPDATE work_shifts SET salary = 0.0 WHERE salary IS NULL;

-- Verify the column was added
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'work_shifts' AND column_name = 'salary';
