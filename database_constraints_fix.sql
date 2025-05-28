-- Database Constraints to Prevent Duplicate Data Creation
-- Execute these SQL statements on your PostgreSQL databases
--
-- IMPORTANT: Run this script after ensuring all microservices are stopped
-- to avoid conflicts during constraint creation

-- =====================================================
-- CUSTOMER CONTRACT SERVICE DATABASE CONSTRAINTS
-- =====================================================

-- Connect to customer contract database
-- \c customerdb;

-- Add unique constraint to prevent duplicate contracts
-- This prevents the same customer from having multiple contracts with identical details
ALTER TABLE customer_contracts
ADD CONSTRAINT uk_customer_contracts_unique
UNIQUE (customer_id, starting_date, ending_date, total_amount, address);

-- Add index for better performance on contract lookups
CREATE INDEX IF NOT EXISTS idx_customer_contracts_customer_date
ON customer_contracts (customer_id, starting_date, ending_date);

-- Add constraint to ensure valid date ranges
ALTER TABLE customer_contracts
ADD CONSTRAINT chk_contract_date_range
CHECK (ending_date >= starting_date);

-- Add constraint to ensure positive amounts
ALTER TABLE customer_contracts
ADD CONSTRAINT chk_contract_positive_amount
CHECK (total_amount > 0);

-- Add constraint to ensure valid status values
ALTER TABLE customer_contracts
ADD CONSTRAINT chk_contract_valid_status
CHECK (status IN (0, 1, 2, 3));

-- =====================================================
-- CUSTOMER PAYMENT SERVICE DATABASE CONSTRAINTS
-- =====================================================

-- Connect to customer payment database
\c customerpaymentdb;

-- Add unique constraint to prevent duplicate payments
-- This prevents identical payments for the same contract
ALTER TABLE customer_payments
ADD CONSTRAINT uk_customer_payments_unique
UNIQUE (customer_contract_id, payment_amount, payment_date, payment_method);

-- Add index for better performance on payment lookups
CREATE INDEX IF NOT EXISTS idx_customer_payments_contract_date
ON customer_payments (customer_contract_id, payment_date);

-- Add constraint to ensure positive payment amounts
ALTER TABLE customer_payments
ADD CONSTRAINT chk_payment_positive_amount
CHECK (payment_amount > 0);

-- Add constraint to ensure valid payment methods
ALTER TABLE customer_payments
ADD CONSTRAINT chk_payment_valid_method
CHECK (payment_method IN (0, 1, 2, 3, 4));

-- =====================================================
-- VERIFICATION QUERIES
-- =====================================================

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
HAVING COUNT(*) > 1;

-- Check for existing duplicate payments
SELECT
    customer_contract_id,
    payment_amount,
    payment_date,
    payment_method,
    COUNT(*) as duplicate_count
FROM customer_payments
WHERE is_deleted = false
GROUP BY customer_contract_id, payment_amount, payment_date, payment_method
HAVING COUNT(*) > 1;

-- =====================================================
-- CLEANUP EXISTING DUPLICATES (IF ANY)
-- =====================================================

-- WARNING: Review these queries carefully before executing
-- They will remove duplicate records, keeping only the first occurrence

-- Remove duplicate contracts (keeping the one with the lowest ID)
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

-- Remove duplicate payments (keeping the one with the lowest ID)
WITH duplicate_payments AS (
    SELECT
        id,
        ROW_NUMBER() OVER (
            PARTITION BY customer_contract_id, payment_amount, payment_date, payment_method
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

-- =====================================================
-- MONITORING QUERIES
-- =====================================================

-- Query to monitor for constraint violations
SELECT
    schemaname,
    tablename,
    attname,
    n_distinct,
    correlation
FROM pg_stats
WHERE tablename IN ('customer_contracts', 'customer_payments')
ORDER BY tablename, attname;

-- Query to check constraint status
SELECT
    tc.constraint_name,
    tc.table_name,
    tc.constraint_type,
    cc.check_clause
FROM information_schema.table_constraints tc
LEFT JOIN information_schema.check_constraints cc
    ON tc.constraint_name = cc.constraint_name
WHERE tc.table_name IN ('customer_contracts', 'customer_payments')
ORDER BY tc.table_name, tc.constraint_type;

-- =====================================================
-- PERFORMANCE MONITORING
-- =====================================================

-- Monitor index usage
SELECT
    schemaname,
    tablename,
    indexname,
    idx_scan,
    idx_tup_read,
    idx_tup_fetch
FROM pg_stat_user_indexes
WHERE tablename IN ('customer_contracts', 'customer_payments')
ORDER BY tablename, idx_scan DESC;

-- Monitor table statistics
SELECT
    schemaname,
    tablename,
    n_tup_ins,
    n_tup_upd,
    n_tup_del,
    n_live_tup,
    n_dead_tup
FROM pg_stat_user_tables
WHERE tablename IN ('customer_contracts', 'customer_payments')
ORDER BY tablename;

-- =====================================================
-- ROLLBACK SCRIPTS (IF NEEDED)
-- =====================================================

-- To remove the constraints if they cause issues:

-- Remove contract constraints
-- ALTER TABLE customer_contracts DROP CONSTRAINT IF EXISTS uk_customer_contracts_unique;
-- ALTER TABLE customer_contracts DROP CONSTRAINT IF EXISTS chk_contract_date_range;
-- ALTER TABLE customer_contracts DROP CONSTRAINT IF EXISTS chk_contract_positive_amount;
-- ALTER TABLE customer_contracts DROP CONSTRAINT IF EXISTS chk_contract_valid_status;

-- Remove payment constraints
-- ALTER TABLE customer_payments DROP CONSTRAINT IF EXISTS uk_customer_payments_unique;
-- ALTER TABLE customer_payments DROP CONSTRAINT IF EXISTS chk_payment_positive_amount;
-- ALTER TABLE customer_payments DROP CONSTRAINT IF EXISTS chk_payment_valid_method;

-- Remove indexes
-- DROP INDEX IF EXISTS idx_customer_contracts_customer_date;
-- DROP INDEX IF EXISTS idx_customer_payments_contract_date;
