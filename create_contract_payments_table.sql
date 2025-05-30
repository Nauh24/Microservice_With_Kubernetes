-- Script to create contract_payments table for many-to-many relationship
-- between payments and contracts

-- Connect to customer payment database
\c customerpaymentdb;

-- Create contract_payments table if it doesn't exist
CREATE TABLE IF NOT EXISTS contract_payments (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT NOT NULL,
    contract_id BIGINT NOT NULL,
    allocated_amount DECIMAL(15,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraint to customer_payments table
    CONSTRAINT fk_contract_payments_payment_id 
        FOREIGN KEY (payment_id) 
        REFERENCES customer_payments(id) 
        ON DELETE CASCADE,
    
    -- Constraints
    CONSTRAINT chk_contract_payments_positive_amount 
        CHECK (allocated_amount > 0),
    
    -- Unique constraint to prevent duplicate allocations
    CONSTRAINT uk_contract_payments_payment_contract 
        UNIQUE (payment_id, contract_id)
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_contract_payments_payment_id 
    ON contract_payments (payment_id);

CREATE INDEX IF NOT EXISTS idx_contract_payments_contract_id 
    ON contract_payments (contract_id);

CREATE INDEX IF NOT EXISTS idx_contract_payments_lookup 
    ON contract_payments (payment_id, contract_id);

-- Add trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_contract_payments_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_update_contract_payments_updated_at ON contract_payments;
CREATE TRIGGER trigger_update_contract_payments_updated_at
    BEFORE UPDATE ON contract_payments
    FOR EACH ROW
    EXECUTE FUNCTION update_contract_payments_updated_at();

-- Verify table creation
\d contract_payments;

-- Show table structure
SELECT 
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_name = 'contract_payments' 
    AND table_schema = 'public'
ORDER BY ordinal_position;

-- Show constraints
SELECT 
    tc.constraint_name,
    tc.constraint_type,
    kcu.column_name
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu 
    ON tc.constraint_name = kcu.constraint_name
WHERE tc.table_name = 'contract_payments'
    AND tc.table_schema = 'public'
ORDER BY tc.constraint_type, tc.constraint_name;

COMMIT;
