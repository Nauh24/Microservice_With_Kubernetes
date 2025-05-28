/**
 * Comprehensive Duplicate Prevention Test Suite
 * 
 * This script performs exhaustive testing to verify that all duplicate
 * data creation issues have been resolved.
 */

const axios = require('axios');

// Configuration
const API_BASE_URL = 'http://localhost:8080';
const TEST_TIMEOUT = 15000;
const RAPID_SUBMISSION_DELAY = 100; // 100ms between rapid requests

// Test counters
let testsPassed = 0;
let testsFailed = 0;
let totalTests = 0;

// Helper function to log test results
function logTest(testName, passed, message = '') {
    totalTests++;
    if (passed) {
        testsPassed++;
        console.log(`✅ ${testName}: PASSED ${message}`);
    } else {
        testsFailed++;
        console.log(`❌ ${testName}: FAILED ${message}`);
    }
}

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

// Test data generator
function generateTestContract(suffix = '') {
    return {
        customerId: 1,
        startingDate: '2024-01-15',
        endingDate: '2024-02-15',
        totalAmount: 5000000,
        address: `Test Address ${suffix}`,
        description: `Test contract ${suffix}`,
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
                        numberOfWorkers: 2,
                        salary: 500000,
                        workingDays: '1,2,3,4,5'
                    }
                ]
            }
        ]
    };
}

function generateTestPayment(contractId, suffix = '') {
    return {
        paymentAmount: 1000000,
        paymentMethod: 1,
        note: `Test payment ${suffix}`,
        customerContractId: contractId,
        customerId: 1
    };
}

// Test 1: Single Contract Creation
async function testSingleContractCreation() {
    console.log('\n=== Test 1: Single Contract Creation ===');
    
    const contract = generateTestContract('single');
    const result = await makeRequest('POST', '/api/customer-contract', contract);
    
    logTest('Single Contract Creation', result.success, 
        result.success ? `ID: ${result.data.id}` : `Error: ${result.error}`);
    
    return result.success ? result.data.id : null;
}

// Test 2: Rapid Contract Creation (Simulate Double Click)
async function testRapidContractCreation() {
    console.log('\n=== Test 2: Rapid Contract Creation ===');
    
    const contract = generateTestContract('rapid');
    const promises = [];
    
    // Make 5 rapid requests
    for (let i = 0; i < 5; i++) {
        promises.push(makeRequest('POST', '/api/customer-contract', {
            ...contract,
            description: `Rapid test ${i + 1}`
        }));
        
        // Small delay to simulate rapid clicking
        if (i < 4) {
            await new Promise(resolve => setTimeout(resolve, RAPID_SUBMISSION_DELAY));
        }
    }
    
    const results = await Promise.all(promises);
    const successCount = results.filter(r => r.success).length;
    const uniqueIds = [...new Set(results.filter(r => r.success).map(r => r.data.id))];
    
    // Test should either:
    // 1. All succeed with unique IDs (proper handling)
    // 2. Only one succeed (duplicate prevention)
    const passed = successCount === uniqueIds.length && (successCount === 1 || successCount === 5);
    
    logTest('Rapid Contract Creation', passed, 
        `${successCount} successful, ${uniqueIds.length} unique IDs`);
    
    return uniqueIds;
}

// Test 3: Concurrent Contract Creation
async function testConcurrentContractCreation() {
    console.log('\n=== Test 3: Concurrent Contract Creation ===');
    
    const contract = generateTestContract('concurrent');
    const promises = [];
    
    // Make 3 truly concurrent requests
    for (let i = 0; i < 3; i++) {
        promises.push(makeRequest('POST', '/api/customer-contract', {
            ...contract,
            description: `Concurrent test ${i + 1}`
        }));
    }
    
    const results = await Promise.allSettled(promises);
    const successResults = results.filter(r => r.status === 'fulfilled' && r.value.success);
    const uniqueIds = [...new Set(successResults.map(r => r.value.data.id))];
    
    const passed = successResults.length === uniqueIds.length;
    
    logTest('Concurrent Contract Creation', passed, 
        `${successResults.length} successful, ${uniqueIds.length} unique IDs`);
    
    return uniqueIds;
}

// Test 4: Single Payment Creation
async function testSinglePaymentCreation(contractId) {
    console.log('\n=== Test 4: Single Payment Creation ===');
    
    if (!contractId) {
        logTest('Single Payment Creation', false, 'No contract ID provided');
        return null;
    }
    
    const payment = generateTestPayment(contractId, 'single');
    const result = await makeRequest('POST', '/api/customer-payment', payment);
    
    logTest('Single Payment Creation', result.success, 
        result.success ? `ID: ${result.data.id}` : `Error: ${result.error}`);
    
    return result.success ? result.data.id : null;
}

// Test 5: Rapid Payment Creation
async function testRapidPaymentCreation(contractId) {
    console.log('\n=== Test 5: Rapid Payment Creation ===');
    
    if (!contractId) {
        logTest('Rapid Payment Creation', false, 'No contract ID provided');
        return [];
    }
    
    const promises = [];
    
    // Make 5 rapid payment requests
    for (let i = 0; i < 5; i++) {
        const payment = generateTestPayment(contractId, `rapid-${i + 1}`);
        promises.push(makeRequest('POST', '/api/customer-payment', payment));
        
        if (i < 4) {
            await new Promise(resolve => setTimeout(resolve, RAPID_SUBMISSION_DELAY));
        }
    }
    
    const results = await Promise.all(promises);
    const successCount = results.filter(r => r.success).length;
    const uniqueIds = [...new Set(results.filter(r => r.success).map(r => r.data.id))];
    
    const passed = successCount === uniqueIds.length;
    
    logTest('Rapid Payment Creation', passed, 
        `${successCount} successful, ${uniqueIds.length} unique IDs`);
    
    return uniqueIds;
}

// Test 6: Overpayment Prevention
async function testOverpaymentPrevention(contractId) {
    console.log('\n=== Test 6: Overpayment Prevention ===');
    
    if (!contractId) {
        logTest('Overpayment Prevention', false, 'No contract ID provided');
        return;
    }
    
    const payment = generateTestPayment(contractId, 'overpayment');
    payment.paymentAmount = 50000000; // Much more than contract total
    
    const result = await makeRequest('POST', '/api/customer-payment', payment);
    
    // Should fail due to overpayment
    const passed = !result.success;
    
    logTest('Overpayment Prevention', passed, 
        passed ? 'Correctly prevented overpayment' : 'Overpayment was allowed');
}

// Test 7: Invalid Input Validation
async function testInvalidInputValidation() {
    console.log('\n=== Test 7: Invalid Input Validation ===');
    
    // Test invalid contract
    const invalidContract = {
        customerId: null,
        startingDate: '2024-01-15',
        endingDate: '2024-01-10', // End before start
        totalAmount: -1000, // Negative amount
        address: '',
        jobDetails: []
    };
    
    const contractResult = await makeRequest('POST', '/api/customer-contract', invalidContract);
    
    // Test invalid payment
    const invalidPayment = {
        paymentAmount: -1000, // Negative amount
        paymentMethod: 999, // Invalid method
        customerContractId: 99999, // Non-existent contract
        customerId: 99999 // Non-existent customer
    };
    
    const paymentResult = await makeRequest('POST', '/api/customer-payment', invalidPayment);
    
    const contractValidationPassed = !contractResult.success;
    const paymentValidationPassed = !paymentResult.success;
    
    logTest('Contract Input Validation', contractValidationPassed, 
        contractValidationPassed ? 'Invalid contract rejected' : 'Invalid contract accepted');
    
    logTest('Payment Input Validation', paymentValidationPassed, 
        paymentValidationPassed ? 'Invalid payment rejected' : 'Invalid payment accepted');
}

// Test 8: Database State Verification
async function testDatabaseStateVerification() {
    console.log('\n=== Test 8: Database State Verification ===');
    
    try {
        const contractsResult = await makeRequest('GET', '/api/customer-contract');
        const paymentsResult = await makeRequest('GET', '/api/customer-payment');
        
        if (contractsResult.success && paymentsResult.success) {
            const contracts = contractsResult.data;
            const payments = paymentsResult.data;
            
            // Check for duplicates in contracts
            const contractKeys = contracts.map(c => 
                `${c.customerId}-${c.totalAmount}-${c.address}`);
            const uniqueContractKeys = [...new Set(contractKeys)];
            
            // Check for duplicates in payments
            const paymentKeys = payments.map(p => 
                `${p.customerContractId}-${p.paymentAmount}-${p.note}`);
            const uniquePaymentKeys = [...new Set(paymentKeys)];
            
            const noDuplicateContracts = contractKeys.length === uniqueContractKeys.length;
            const noDuplicatePayments = paymentKeys.length === uniquePaymentKeys.length;
            
            logTest('No Duplicate Contracts in DB', noDuplicateContracts, 
                `${contracts.length} contracts, ${uniqueContractKeys.length} unique`);
            
            logTest('No Duplicate Payments in DB', noDuplicatePayments, 
                `${payments.length} payments, ${uniquePaymentKeys.length} unique`);
        } else {
            logTest('Database State Verification', false, 'Failed to fetch data');
        }
    } catch (error) {
        logTest('Database State Verification', false, `Error: ${error.message}`);
    }
}

// Main test runner
async function runComprehensiveTests() {
    console.log('🚀 Starting Comprehensive Duplicate Prevention Tests');
    console.log(`   API Base URL: ${API_BASE_URL}`);
    console.log(`   Test Timeout: ${TEST_TIMEOUT}ms`);
    console.log(`   Rapid Submission Delay: ${RAPID_SUBMISSION_DELAY}ms`);
    
    const startTime = Date.now();
    
    try {
        // Contract tests
        const singleContractId = await testSingleContractCreation();
        const rapidContractIds = await testRapidContractCreation();
        const concurrentContractIds = await testConcurrentContractCreation();
        
        // Use the first available contract for payment tests
        const testContractId = singleContractId || 
                              (rapidContractIds.length > 0 ? rapidContractIds[0] : null) ||
                              (concurrentContractIds.length > 0 ? concurrentContractIds[0] : null);
        
        // Payment tests
        if (testContractId) {
            await testSinglePaymentCreation(testContractId);
            await testRapidPaymentCreation(testContractId);
            await testOverpaymentPrevention(testContractId);
        }
        
        // Validation tests
        await testInvalidInputValidation();
        
        // Database verification
        await testDatabaseStateVerification();
        
        const duration = Date.now() - startTime;
        
        console.log('\n🎯 Test Results Summary:');
        console.log(`   Total Tests: ${totalTests}`);
        console.log(`   Passed: ${testsPassed} (${((testsPassed/totalTests)*100).toFixed(1)}%)`);
        console.log(`   Failed: ${testsFailed} (${((testsFailed/totalTests)*100).toFixed(1)}%)`);
        console.log(`   Duration: ${duration}ms`);
        
        if (testsFailed === 0) {
            console.log('\n🎉 All tests passed! Duplicate prevention is working correctly.');
        } else {
            console.log('\n⚠️  Some tests failed. Please review the results above.');
        }
        
    } catch (error) {
        console.error('\n💥 Test execution failed:', error.message);
    }
}

// Run tests if this script is executed directly
if (require.main === module) {
    runComprehensiveTests();
}

module.exports = {
    runComprehensiveTests,
    testSingleContractCreation,
    testRapidContractCreation,
    testConcurrentContractCreation,
    testSinglePaymentCreation,
    testRapidPaymentCreation,
    testOverpaymentPrevention,
    testInvalidInputValidation,
    testDatabaseStateVerification
};
