-- Simple script to create contract_payments table
-- Run this in pgAdmin Query Tool for customerpaymentdb database

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

-- Verify table creation
SELECT 'Table contract_payments created successfully' as result;

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
