-- Fix Contract Address Constraints
-- This script removes the address requirement from database constraints
-- Run this script on the customercontractdb database

-- =====================================================
-- REMOVE EXISTING CONSTRAINT THAT INCLUDES ADDRESS
-- =====================================================

-- Drop the existing unique constraint that includes address
ALTER TABLE customer_contracts
DROP CONSTRAINT IF EXISTS uk_customer_contracts_no_duplicates;

-- =====================================================
-- ADD NEW CONSTRAINT WITHOUT ADDRESS REQUIREMENT
-- =====================================================

-- Add new unique constraint without address field
-- This allows contracts to be created without requiring an address
ALTER TABLE customer_contracts
ADD CONSTRAINT uk_customer_contracts_no_duplicates
UNIQUE (customer_id, starting_date, ending_date, total_amount);

-- =====================================================
-- VERIFICATION
-- =====================================================

-- Check that the constraint was created successfully
SELECT 
    tc.constraint_name,
    tc.table_name,
    kcu.column_name
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu 
    ON tc.constraint_name = kcu.constraint_name
WHERE tc.table_name = 'customer_contracts' 
    AND tc.constraint_type = 'UNIQUE'
    AND tc.constraint_name = 'uk_customer_contracts_no_duplicates'
ORDER BY kcu.ordinal_position;

-- =====================================================
-- SUCCESS MESSAGE
-- =====================================================

SELECT 'Contract address constraints fixed successfully! Contracts can now be created without requiring an address.' as status;
