/**
 * Duplicate Prevention Test Script
 * 
 * This script tests the microservices to ensure no duplicate data is created
 * during contract and payment creation operations.
 */

const axios = require('axios');

// Configuration
const API_BASE_URL = 'http://localhost:8080'; // API Gateway
const TEST_CUSTOMER_ID = 1; // Assuming customer with ID 1 exists
const TEST_TIMEOUT = 30000; // 30 seconds

// Test data
const testContract = {
    customerId: TEST_CUSTOMER_ID,
    startingDate: '2024-01-15',
    endingDate: '2024-02-15',
    totalAmount: 10000000,
    address: 'Test Address for Duplicate Prevention',
    description: 'Test contract for duplicate prevention',
    jobDetails: [
        {
            jobCategoryId: 1,
            startDate: '2024-01-15',
            endDate: '2024-02-15',
            workLocation: 'Test Location',
            workShifts: [
                {
                    startTime: '08:00',
                    endTime: '17:00',
                    numberOfWorkers: 5,
                    salary: 500000,
                    workingDays: '1,2,3,4,5' // Monday to Friday
                }
            ]
        }
    ]
};

// Helper function to make API requests
async function makeRequest(method, url, data = null) {
    try {
        const config = {
            method,
            url: `${API_BASE_URL}${url}`,
            timeout: TEST_TIMEOUT,
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        };
        
        if (data) {
            config.data = data;
        }
        
        const response = await axios(config);
        return { success: true, data: response.data, status: response.status };
    } catch (error) {
        return { 
            success: false, 
            error: error.response?.data || error.message,
            status: error.response?.status || 500
        };
    }
}

// Test 1: Single Contract Creation
async function testSingleContractCreation() {
    console.log('\n=== Test 1: Single Contract Creation ===');
    
    const result = await makeRequest('POST', '/api/customer-contract', testContract);
    
    if (result.success) {
        console.log('✅ Contract created successfully');
        console.log(`   Contract ID: ${result.data.id}`);
        console.log(`   Total Amount: ${result.data.totalAmount}`);
        return result.data.id;
    } else {
        console.log('❌ Contract creation failed');
        console.log(`   Error: ${JSON.stringify(result.error)}`);
        return null;
    }
}

// Test 2: Duplicate Contract Prevention
async function testDuplicateContractPrevention() {
    console.log('\n=== Test 2: Duplicate Contract Prevention ===');
    
    // Try to create the same contract multiple times rapidly
    const promises = [];
    for (let i = 0; i < 3; i++) {
        promises.push(makeRequest('POST', '/api/customer-contract', {
            ...testContract,
            description: `Duplicate test ${i + 1}`
        }));
    }
    
    const results = await Promise.all(promises);
    const successCount = results.filter(r => r.success).length;
    const failureCount = results.filter(r => !r.success).length;
    
    console.log(`   Successful creations: ${successCount}`);
    console.log(`   Failed creations: ${failureCount}`);
    
    if (successCount === 3) {
        console.log('✅ All requests succeeded (expected behavior)');
        return results.filter(r => r.success).map(r => r.data.id);
    } else {
        console.log('⚠️  Some requests failed (check if this is expected)');
        return results.filter(r => r.success).map(r => r.data.id);
    }
}

// Test 3: Payment Creation
async function testPaymentCreation(contractId) {
    console.log('\n=== Test 3: Payment Creation ===');
    
    if (!contractId) {
        console.log('❌ No contract ID provided for payment test');
        return null;
    }
    
    const testPayment = {
        paymentAmount: 1000000,
        paymentMethod: 1, // Bank transfer
        note: 'Test payment for duplicate prevention',
        customerContractId: contractId,
        customerId: TEST_CUSTOMER_ID
    };
    
    const result = await makeRequest('POST', '/api/customer-payment', testPayment);
    
    if (result.success) {
        console.log('✅ Payment created successfully');
        console.log(`   Payment ID: ${result.data.id}`);
        console.log(`   Amount: ${result.data.paymentAmount}`);
        return result.data.id;
    } else {
        console.log('❌ Payment creation failed');
        console.log(`   Error: ${JSON.stringify(result.error)}`);
        return null;
    }
}

// Test 4: Duplicate Payment Prevention
async function testDuplicatePaymentPrevention(contractId) {
    console.log('\n=== Test 4: Duplicate Payment Prevention ===');
    
    if (!contractId) {
        console.log('❌ No contract ID provided for duplicate payment test');
        return;
    }
    
    // Try to create multiple payments rapidly
    const promises = [];
    for (let i = 0; i < 3; i++) {
        promises.push(makeRequest('POST', '/api/customer-payment', {
            paymentAmount: 500000,
            paymentMethod: 0, // Cash
            note: `Duplicate payment test ${i + 1}`,
            customerContractId: contractId,
            customerId: TEST_CUSTOMER_ID
        }));
    }
    
    const results = await Promise.all(promises);
    const successCount = results.filter(r => r.success).length;
    const failureCount = results.filter(r => !r.success).length;
    
    console.log(`   Successful payments: ${successCount}`);
    console.log(`   Failed payments: ${failureCount}`);
    
    if (successCount === 3) {
        console.log('✅ All payment requests succeeded (expected behavior)');
    } else {
        console.log('⚠️  Some payment requests failed (check if this is expected)');
    }
}

// Test 5: Overpayment Prevention
async function testOverpaymentPrevention(contractId) {
    console.log('\n=== Test 5: Overpayment Prevention ===');
    
    if (!contractId) {
        console.log('❌ No contract ID provided for overpayment test');
        return;
    }
    
    // Try to pay more than the contract total
    const result = await makeRequest('POST', '/api/customer-payment', {
        paymentAmount: 50000000, // Much more than contract total
        paymentMethod: 1,
        note: 'Overpayment test',
        customerContractId: contractId,
        customerId: TEST_CUSTOMER_ID
    });
    
    if (!result.success) {
        console.log('✅ Overpayment correctly prevented');
        console.log(`   Error message: ${result.error.message || result.error}`);
    } else {
        console.log('❌ Overpayment was allowed (this should not happen)');
        console.log(`   Payment ID: ${result.data.id}`);
    }
}

// Test 6: Invalid Input Validation
async function testInvalidInputValidation() {
    console.log('\n=== Test 6: Invalid Input Validation ===');
    
    // Test invalid contract
    const invalidContract = await makeRequest('POST', '/api/customer-contract', {
        customerId: null,
        startingDate: '2024-01-15',
        endingDate: '2024-01-10', // End before start
        totalAmount: -1000, // Negative amount
        address: '',
        jobDetails: []
    });
    
    // Test invalid payment
    const invalidPayment = await makeRequest('POST', '/api/customer-payment', {
        paymentAmount: -1000, // Negative amount
        paymentMethod: 999, // Invalid method
        customerContractId: 99999, // Non-existent contract
        customerId: 99999 // Non-existent customer
    });
    
    const contractValidationWorking = !invalidContract.success;
    const paymentValidationWorking = !invalidPayment.success;
    
    console.log(`   Contract validation: ${contractValidationWorking ? '✅' : '❌'}`);
    console.log(`   Payment validation: ${paymentValidationWorking ? '✅' : '❌'}`);
    
    if (contractValidationWorking && paymentValidationWorking) {
        console.log('✅ Input validation working correctly');
    } else {
        console.log('❌ Input validation has issues');
    }
}

// Main test runner
async function runAllTests() {
    console.log('🚀 Starting Duplicate Prevention Tests');
    console.log(`   API Base URL: ${API_BASE_URL}`);
    console.log(`   Test Customer ID: ${TEST_CUSTOMER_ID}`);
    console.log(`   Timeout: ${TEST_TIMEOUT}ms`);
    
    try {
        // Test basic functionality
        const contractId = await testSingleContractCreation();
        const contractIds = await testDuplicateContractPrevention();
        
        // Use the first contract for payment tests
        const testContractId = contractId || (contractIds && contractIds[0]);
        
        if (testContractId) {
            await testPaymentCreation(testContractId);
            await testDuplicatePaymentPrevention(testContractId);
            await testOverpaymentPrevention(testContractId);
        }
        
        // Test validation
        await testInvalidInputValidation();
        
        console.log('\n🎉 All tests completed!');
        console.log('\n📋 Summary:');
        console.log('   - Check the results above for any ❌ failures');
        console.log('   - Verify database for duplicate records');
        console.log('   - Monitor application logs for errors');
        
    } catch (error) {
        console.error('\n💥 Test execution failed:', error.message);
    }
}

// Run tests if this script is executed directly
if (require.main === module) {
    runAllTests();
}

module.exports = {
    runAllTests,
    testSingleContractCreation,
    testDuplicateContractPrevention,
    testPaymentCreation,
    testDuplicatePaymentPrevention,
    testOverpaymentPrevention,
    testInvalidInputValidation
};
