-- Comprehensive Fix for Data Duplication Issues
-- Execute this script to prevent and clean up duplicate data
-- Run each section separately for each database

-- =====================================================
-- STEP 1: CONNECT TO CUSTOMER CONTRACT DATABASE
-- =====================================================
-- \c customercontractdb;

-- Check for existing duplicate contracts
SELECT 
    customer_id, 
    starting_date, 
    ending_date, 
    total_amount, 
    address,
    COUNT(*) as duplicate_count
FROM customer_contracts 
WHERE is_deleted = false
GROUP BY customer_id, starting_date, ending_date, total_amount, address
HAVING COUNT(*) > 1
ORDER BY duplicate_count DESC;

-- Clean up existing duplicates (mark as deleted, keep the first one)
WITH duplicate_contracts AS (
    SELECT 
        id,
        ROW_NUMBER() OVER (
            PARTITION BY customer_id, starting_date, ending_date, total_amount, address 
            ORDER BY id
        ) as rn
    FROM customer_contracts 
    WHERE is_deleted = false
)
UPDATE customer_contracts 
SET is_deleted = true, updated_at = NOW()
WHERE id IN (
    SELECT id FROM duplicate_contracts WHERE rn > 1
);

-- Add unique constraint to prevent future duplicates
ALTER TABLE customer_contracts 
ADD CONSTRAINT uk_customer_contracts_no_duplicates 
UNIQUE (customer_id, starting_date, ending_date, total_amount, address);

-- Add additional constraints for data validation
ALTER TABLE customer_contracts 
ADD CONSTRAINT chk_contract_positive_amount 
CHECK (total_amount > 0);

ALTER TABLE customer_contracts 
ADD CONSTRAINT chk_contract_valid_dates 
CHECK (ending_date >= starting_date);

ALTER TABLE customer_contracts 
ADD CONSTRAINT chk_contract_valid_status 
CHECK (status IN (0, 1, 2, 3));

-- Add performance indexes
CREATE INDEX IF NOT EXISTS idx_customer_contracts_lookup 
ON customer_contracts (customer_id, starting_date, ending_date);

CREATE INDEX IF NOT EXISTS idx_customer_contracts_status 
ON customer_contracts (status, is_deleted);

-- =====================================================
-- STEP 2: CONNECT TO CUSTOMER PAYMENT DATABASE
-- =====================================================
-- \c customerpaymentdb;

-- Check for existing duplicate payments
SELECT 
    customer_contract_id, 
    payment_amount, 
    DATE(payment_date) as payment_date_only,
    payment_method,
    note,
    COUNT(*) as duplicate_count
FROM customer_payments 
WHERE is_deleted = false
GROUP BY customer_contract_id, payment_amount, DATE(payment_date), payment_method, note
HAVING COUNT(*) > 1
ORDER BY duplicate_count DESC;

-- Clean up existing duplicate payments (mark as deleted, keep the first one)
WITH duplicate_payments AS (
    SELECT 
        id,
        ROW_NUMBER() OVER (
            PARTITION BY customer_contract_id, payment_amount, DATE(payment_date), payment_method, note 
            ORDER BY id
        ) as rn
    FROM customer_payments 
    WHERE is_deleted = false
)
UPDATE customer_payments 
SET is_deleted = true, updated_at = NOW()
WHERE id IN (
    SELECT id FROM duplicate_payments WHERE rn > 1
);

-- Add unique constraint to prevent future duplicate payments
-- Note: Using DATE(payment_date) to allow multiple payments on same day but prevent exact duplicates
ALTER TABLE customer_payments 
ADD CONSTRAINT uk_customer_payments_no_duplicates 
UNIQUE (customer_contract_id, payment_amount, DATE(payment_date), payment_method, note);

-- Add additional constraints for data validation
ALTER TABLE customer_payments 
ADD CONSTRAINT chk_payment_positive_amount 
CHECK (payment_amount > 0);

ALTER TABLE customer_payments 
ADD CONSTRAINT chk_payment_valid_method 
CHECK (payment_method IN (0, 1, 2, 3, 4));

-- Add performance indexes
CREATE INDEX IF NOT EXISTS idx_customer_payments_lookup 
ON customer_payments (customer_contract_id, payment_date);

CREATE INDEX IF NOT EXISTS idx_customer_payments_customer 
ON customer_payments (customer_id, is_deleted);

-- =====================================================
-- VERIFICATION QUERIES
-- =====================================================

-- Verify no duplicates remain in contracts
SELECT 'Checking for remaining duplicate contracts...' as status;
SELECT 
    customer_id, 
    starting_date, 
    ending_date, 
    total_amount, 
    address,
    COUNT(*) as count
FROM customer_contracts 
WHERE is_deleted = false
GROUP BY customer_id, starting_date, ending_date, total_amount, address
HAVING COUNT(*) > 1;

-- Verify no duplicates remain in payments
SELECT 'Checking for remaining duplicate payments...' as status;
SELECT 
    customer_contract_id, 
    payment_amount, 
    DATE(payment_date) as payment_date_only,
    payment_method,
    note,
    COUNT(*) as count
FROM customer_payments 
WHERE is_deleted = false
GROUP BY customer_contract_id, payment_amount, DATE(payment_date), payment_method, note
HAVING COUNT(*) > 1;

-- Check constraint status
SELECT 
    tc.constraint_name,
    tc.table_name,
    tc.constraint_type
FROM information_schema.table_constraints tc
WHERE tc.table_name IN ('customer_contracts', 'customer_payments')
    AND tc.constraint_type IN ('UNIQUE', 'CHECK')
ORDER BY tc.table_name, tc.constraint_type;

SELECT 'Database constraints applied successfully!' as status;
