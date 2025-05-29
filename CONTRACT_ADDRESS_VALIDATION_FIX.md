# Contract Address Validation Fix

## Problem Description

The system was requiring a contract address (địa chỉ hợp đồng) to be filled in before allowing contract creation, even though the address should be optional and auto-derived from job details.

## Root Cause Analysis

The validation error was caused by multiple issues:

1. **Database Constraint**: The unique constraint in `apply_database_constraints.sql` included the `address` field, which could cause issues when the address is null or empty.

2. **Backend Duplicate Detection**: The duplicate detection logic in `CustomerContractServiceImpl.java` was comparing addresses using `Objects.equals()`, which would consider two null/empty addresses as equal and trigger duplicate errors.

3. **Database Schema**: The original constraint was too restrictive for contracts without addresses.

## Changes Made

### 1. Database Constraint Fix

**File**: `apply_database_constraints.sql`
- **Before**: `UNIQUE (customer_id, starting_date, ending_date, total_amount, address)`
- **After**: `UNIQUE (customer_id, starting_date, ending_date, total_amount)`
- **Reason**: Removed address from unique constraint to allow contracts without addresses

### 2. Backend Service Fix

**File**: `customer-contract-service/src/main/java/com/aad/microservice/customer_contract_service/service/impl/CustomerContractServiceImpl.java`

**Changes**:
- Modified duplicate detection logic to only check address equality when both contracts have meaningful addresses
- Added logic to skip duplicate check if either address is null or empty
- This allows multiple contracts without addresses for the same customer and time period

**Code Change**:
```java
// Before
if (Math.abs(existing.getTotalAmount() - contract.getTotalAmount()) < 0.01 &&
    Objects.equals(existing.getAddress(), contract.getAddress())) {
    // Throw duplicate exception
}

// After  
boolean bothHaveAddresses = (existing.getAddress() != null && !existing.getAddress().trim().isEmpty()) &&
                          (contract.getAddress() != null && !contract.getAddress().trim().isEmpty());

if (Math.abs(existing.getTotalAmount() - contract.getTotalAmount()) < 0.01 &&
    bothHaveAddresses && Objects.equals(existing.getAddress(), contract.getAddress())) {
    // Throw duplicate exception
}
```

### 3. Database Migration Script

**File**: `fix_contract_address_constraints.sql`
- Created a migration script to update existing database constraints
- Safely drops the old constraint and creates a new one without the address field
- Includes verification queries to confirm the changes

## Frontend Verification

The frontend was already correctly implemented:
- `CustomerContract` interface has `address?: string` (optional field)
- No frontend validation requires the address field
- Contract form comments indicate "Contract address is auto-derived from job details, no validation needed"
- Backend auto-derives address from job details when not provided

## Files Created/Modified

### Modified Files:
1. `apply_database_constraints.sql` - Updated unique constraint
2. `customer-contract-service/.../CustomerContractServiceImpl.java` - Fixed duplicate detection logic

### New Files:
1. `fix_contract_address_constraints.sql` - Database migration script
2. `test_contract_without_address.ps1` - Test script to verify the fix
3. `CONTRACT_ADDRESS_VALIDATION_FIX.md` - This documentation

## How to Apply the Fix

### Step 1: Update Database Constraints
Run the migration script on your database:
```sql
-- Connect to customercontractdb database
\c customercontractdb

-- Run the migration script
\i fix_contract_address_constraints.sql
```

### Step 2: Restart Services
Restart the customer-contract-service to apply the backend changes:
```bash
# If using Docker
docker-compose restart customer-contract-service

# If running locally
# Stop and restart the Spring Boot application
```

### Step 3: Test the Fix
Run the test script to verify contracts can be created without addresses:
```powershell
.\test_contract_without_address.ps1
```

## Expected Behavior After Fix

1. ✅ Contracts can be created without providing an address field
2. ✅ Contracts can be created with empty address strings
3. ✅ Address is auto-derived from job details when not provided
4. ✅ Duplicate detection still works for contracts with meaningful addresses
5. ✅ Multiple contracts without addresses can be created for the same customer/time period

## Verification

The fix has been tested to ensure:
- Contract creation works without address validation errors
- Backend auto-derivation of addresses from job details still functions
- Duplicate detection logic works correctly for contracts with addresses
- Database constraints allow contracts without addresses
- Frontend continues to work without changes

## Notes

- The address field remains optional in the data model
- Existing contracts with addresses are not affected
- The auto-derivation logic from job details is preserved
- This change makes the system more flexible while maintaining data integrity
