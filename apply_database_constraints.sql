-- Apply Database Constraints to Prevent Duplicate Data
-- Run this script on each database separately

-- =====================================================
-- FOR CUSTOMER CONTRACT DATABASE (customercontractdb)
-- =====================================================

-- 1. Add unique constraint to prevent duplicate contracts
-- This prevents identical contracts from being created
-- Note: Removed address from unique constraint to allow contracts without addresses
ALTER TABLE customer_contracts
ADD CONSTRAINT uk_customer_contracts_no_duplicates
UNIQUE (customer_id, starting_date, ending_date, total_amount);

-- 2. Add check constraints for data validation
ALTER TABLE customer_contracts
ADD CONSTRAINT chk_contract_positive_amount
CHECK (total_amount > 0);

ALTER TABLE customer_contracts
ADD CONSTRAINT chk_contract_valid_dates
CHECK (ending_date >= starting_date);

ALTER TABLE customer_contracts
ADD CONSTRAINT chk_contract_valid_status
CHECK (status IN (0, 1, 2, 3));

-- 3. Add performance indexes
CREATE INDEX IF NOT EXISTS idx_customer_contracts_lookup
ON customer_contracts (customer_id, starting_date, ending_date);

-- =====================================================
-- FOR CUSTOMER PAYMENT DATABASE (customerpaymentdb)
-- =====================================================

-- 1. Add unique constraint to prevent duplicate payments
-- This prevents identical payments from being created
ALTER TABLE customer_payments
ADD CONSTRAINT uk_customer_payments_no_duplicates
UNIQUE (customer_contract_id, payment_amount, payment_date, payment_method, note);

-- 2. Add check constraints for data validation
ALTER TABLE customer_payments
ADD CONSTRAINT chk_payment_positive_amount
CHECK (payment_amount > 0);

ALTER TABLE customer_payments
ADD CONSTRAINT chk_payment_valid_method
CHECK (payment_method IN (0, 1, 2, 3, 4));

-- 3. Add performance indexes
CREATE INDEX IF NOT EXISTS idx_customer_payments_lookup
ON customer_payments (customer_contract_id, payment_date);

-- =====================================================
-- VERIFICATION QUERIES
-- =====================================================

-- Check for existing duplicate contracts
SELECT
    customer_id,
    starting_date,
    ending_date,
    total_amount,
    COUNT(*) as count
FROM customer_contracts
WHERE is_deleted = false
GROUP BY customer_id, starting_date, ending_date, total_amount
HAVING COUNT(*) > 1;

-- Check for existing duplicate payments
SELECT
    customer_contract_id,
    payment_amount,
    payment_date,
    payment_method,
    note,
    COUNT(*) as count
FROM customer_payments
WHERE is_deleted = false
GROUP BY customer_contract_id, payment_amount, payment_date, payment_method, note
HAVING COUNT(*) > 1;

-- =====================================================
-- CLEANUP EXISTING DUPLICATES (IF ANY)
-- =====================================================

-- Mark duplicate contracts as deleted (keeping the first one)
WITH duplicate_contracts AS (
    SELECT
        id,
        ROW_NUMBER() OVER (
            PARTITION BY customer_id, starting_date, ending_date, total_amount
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

-- Mark duplicate payments as deleted (keeping the first one)
WITH duplicate_payments AS (
    SELECT
        id,
        ROW_NUMBER() OVER (
            PARTITION BY customer_contract_id, payment_amount, payment_date, payment_method, note
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
-- SUCCESS MESSAGE
-- =====================================================

SELECT 'Database constraints applied successfully!' as status;
