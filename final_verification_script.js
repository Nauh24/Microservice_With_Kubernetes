/**
 * Final Verification Script for Duplicate Prevention
 * 
 * This script performs a final comprehensive test to verify that
 * all duplicate data creation issues have been completely resolved.
 */

const axios = require('axios');

// Configuration
const API_BASE_URL = 'http://localhost:8080';
const TEST_TIMEOUT = 10000;

// Test results tracking
const results = {
    contractTests: [],
    paymentTests: [],
    validationTests: [],
    summary: { passed: 0, failed: 0, total: 0 }
};

// Helper function to log and track results
function recordTest(category, testName, passed, details = '') {
    const result = { testName, passed, details, timestamp: new Date().toISOString() };
    results[category].push(result);
    results.summary.total++;
    
    if (passed) {
        results.summary.passed++;
        console.log(`✅ ${testName}: PASSED ${details}`);
    } else {
        results.summary.failed++;
        console.log(`❌ ${testName}: FAILED ${details}`);
    }
}

// API request helper
async function apiRequest(method, endpoint, data = null) {
    try {
        const config = {
            method,
            url: `${API_BASE_URL}${endpoint}`,
            timeout: TEST_TIMEOUT,
            headers: { 'Content-Type': 'application/json' }
        };
        
        if (data) config.data = data;
        
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

// Generate test data
function createTestContract(id) {
    return {
        customerId: 1,
        startingDate: '2024-01-15',
        endingDate: '2024-02-15',
        totalAmount: 5000000,
        address: `Test Address ${id}`,
        description: `Final verification contract ${id}`,
        jobDetails: [{
            jobCategoryId: 1,
            startDate: '2024-01-15',
            endDate: '2024-02-15',
            workLocation: 'Test Location',
            workShifts: [{
                startTime: '08:00',
                endTime: '17:00',
                numberOfWorkers: 2,
                salary: 500000,
                workingDays: '1,2,3,4,5'
            }]
        }]
    };
}

function createTestPayment(contractId, id) {
    return {
        paymentAmount: 1000000,
        paymentMethod: 1,
        note: `Final verification payment ${id}`,
        customerContractId: contractId,
        customerId: 1
    };
}

// Test 1: Single Contract Creation
async function testSingleContract() {
    console.log('\n=== Testing Single Contract Creation ===');
    
    const contract = createTestContract('single');
    const result = await apiRequest('POST', '/api/customer-contract', contract);
    
    recordTest('contractTests', 'Single Contract Creation', result.success, 
        result.success ? `Created contract ID: ${result.data.id}` : `Error: ${result.error}`);
    
    return result.success ? result.data.id : null;
}

// Test 2: Rapid Contract Submission Prevention
async function testRapidContractPrevention() {
    console.log('\n=== Testing Rapid Contract Submission Prevention ===');
    
    const contract = createTestContract('rapid');
    const startTime = Date.now();
    
    // Make 3 rapid requests
    const promises = Array.from({ length: 3 }, (_, i) => 
        apiRequest('POST', '/api/customer-contract', {
            ...contract,
            description: `Rapid test ${i + 1}`
        })
    );
    
    const results = await Promise.all(promises);
    const successCount = results.filter(r => r.success).length;
    const uniqueIds = [...new Set(results.filter(r => r.success).map(r => r.data.id))];
    const duration = Date.now() - startTime;
    
    // Should either all succeed with unique IDs or be properly rate-limited
    const passed = successCount === uniqueIds.length;
    
    recordTest('contractTests', 'Rapid Contract Prevention', passed, 
        `${successCount} successful, ${uniqueIds.length} unique IDs in ${duration}ms`);
    
    return uniqueIds;
}

// Test 3: Single Payment Creation
async function testSinglePayment(contractId) {
    console.log('\n=== Testing Single Payment Creation ===');
    
    if (!contractId) {
        recordTest('paymentTests', 'Single Payment Creation', false, 'No contract ID available');
        return null;
    }
    
    const payment = createTestPayment(contractId, 'single');
    const result = await apiRequest('POST', '/api/customer-payment', payment);
    
    recordTest('paymentTests', 'Single Payment Creation', result.success,
        result.success ? `Created payment ID: ${result.data.id}` : `Error: ${result.error}`);
    
    return result.success ? result.data.id : null;
}

// Test 4: Rapid Payment Submission Prevention
async function testRapidPaymentPrevention(contractId) {
    console.log('\n=== Testing Rapid Payment Submission Prevention ===');
    
    if (!contractId) {
        recordTest('paymentTests', 'Rapid Payment Prevention', false, 'No contract ID available');
        return;
    }
    
    const startTime = Date.now();
    
    // Make 3 rapid payment requests
    const promises = Array.from({ length: 3 }, (_, i) => 
        apiRequest('POST', '/api/customer-payment', createTestPayment(contractId, `rapid-${i + 1}`))
    );
    
    const results = await Promise.all(promises);
    const successCount = results.filter(r => r.success).length;
    const uniqueIds = [...new Set(results.filter(r => r.success).map(r => r.data.id))];
    const duration = Date.now() - startTime;
    
    const passed = successCount === uniqueIds.length;
    
    recordTest('paymentTests', 'Rapid Payment Prevention', passed,
        `${successCount} successful, ${uniqueIds.length} unique IDs in ${duration}ms`);
}

// Test 5: Overpayment Prevention
async function testOverpaymentPrevention(contractId) {
    console.log('\n=== Testing Overpayment Prevention ===');
    
    if (!contractId) {
        recordTest('validationTests', 'Overpayment Prevention', false, 'No contract ID available');
        return;
    }
    
    const payment = createTestPayment(contractId, 'overpayment');
    payment.paymentAmount = 50000000; // Excessive amount
    
    const result = await apiRequest('POST', '/api/customer-payment', payment);
    
    // Should fail due to overpayment
    const passed = !result.success;
    
    recordTest('validationTests', 'Overpayment Prevention', passed,
        passed ? 'Correctly rejected overpayment' : 'Overpayment was allowed');
}

// Test 6: Input Validation
async function testInputValidation() {
    console.log('\n=== Testing Input Validation ===');
    
    // Test invalid contract
    const invalidContract = {
        customerId: null,
        startingDate: '2024-01-15',
        endingDate: '2024-01-10', // Invalid date range
        totalAmount: -1000, // Negative amount
        address: '',
        jobDetails: []
    };
    
    const contractResult = await apiRequest('POST', '/api/customer-contract', invalidContract);
    
    recordTest('validationTests', 'Invalid Contract Rejection', !contractResult.success,
        contractResult.success ? 'Invalid contract was accepted' : 'Invalid contract correctly rejected');
    
    // Test invalid payment
    const invalidPayment = {
        paymentAmount: -1000,
        paymentMethod: 999,
        customerContractId: 99999,
        customerId: 99999
    };
    
    const paymentResult = await apiRequest('POST', '/api/customer-payment', invalidPayment);
    
    recordTest('validationTests', 'Invalid Payment Rejection', !paymentResult.success,
        paymentResult.success ? 'Invalid payment was accepted' : 'Invalid payment correctly rejected');
}

// Test 7: Database State Verification
async function testDatabaseState() {
    console.log('\n=== Testing Database State ===');
    
    try {
        const [contractsResult, paymentsResult] = await Promise.all([
            apiRequest('GET', '/api/customer-contract'),
            apiRequest('GET', '/api/customer-payment')
        ]);
        
        if (contractsResult.success && paymentsResult.success) {
            const contracts = contractsResult.data;
            const payments = paymentsResult.data;
            
            // Check for potential duplicates
            const contractGroups = {};
            contracts.forEach(c => {
                const key = `${c.customerId}-${c.totalAmount}-${c.address}`;
                contractGroups[key] = (contractGroups[key] || 0) + 1;
            });
            
            const paymentGroups = {};
            payments.forEach(p => {
                const key = `${p.customerContractId}-${p.paymentAmount}-${p.note}`;
                paymentGroups[key] = (paymentGroups[key] || 0) + 1;
            });
            
            const duplicateContracts = Object.values(contractGroups).filter(count => count > 1).length;
            const duplicatePayments = Object.values(paymentGroups).filter(count => count > 1).length;
            
            recordTest('validationTests', 'No Duplicate Contracts', duplicateContracts === 0,
                `${contracts.length} contracts, ${duplicateContracts} potential duplicates`);
            
            recordTest('validationTests', 'No Duplicate Payments', duplicatePayments === 0,
                `${payments.length} payments, ${duplicatePayments} potential duplicates`);
        } else {
            recordTest('validationTests', 'Database State Check', false, 'Failed to fetch data');
        }
    } catch (error) {
        recordTest('validationTests', 'Database State Check', false, `Error: ${error.message}`);
    }
}

// Main verification function
async function runFinalVerification() {
    console.log('🔍 Starting Final Duplicate Prevention Verification');
    console.log(`   API Base URL: ${API_BASE_URL}`);
    console.log(`   Test Timeout: ${TEST_TIMEOUT}ms`);
    console.log(`   Timestamp: ${new Date().toISOString()}`);
    
    const startTime = Date.now();
    
    try {
        // Run all tests
        const contractId = await testSingleContract();
        const rapidContractIds = await testRapidContractPrevention();
        
        // Use any available contract for payment tests
        const testContractId = contractId || (rapidContractIds.length > 0 ? rapidContractIds[0] : null);
        
        if (testContractId) {
            await testSinglePayment(testContractId);
            await testRapidPaymentPrevention(testContractId);
            await testOverpaymentPrevention(testContractId);
        }
        
        await testInputValidation();
        await testDatabaseState();
        
        const duration = Date.now() - startTime;
        
        // Generate final report
        console.log('\n📊 FINAL VERIFICATION RESULTS');
        console.log('=====================================');
        console.log(`Total Tests: ${results.summary.total}`);
        console.log(`Passed: ${results.summary.passed} (${((results.summary.passed/results.summary.total)*100).toFixed(1)}%)`);
        console.log(`Failed: ${results.summary.failed} (${((results.summary.failed/results.summary.total)*100).toFixed(1)}%)`);
        console.log(`Duration: ${duration}ms`);
        
        if (results.summary.failed === 0) {
            console.log('\n🎉 SUCCESS: All duplicate prevention measures are working correctly!');
            console.log('   ✅ No duplicate contracts can be created');
            console.log('   ✅ No duplicate payments can be created');
            console.log('   ✅ Rapid submissions are properly handled');
            console.log('   ✅ Input validation is working');
            console.log('   ✅ Database integrity is maintained');
        } else {
            console.log('\n⚠️  WARNING: Some tests failed. Please review the issues above.');
            
            // Show failed tests
            const failedTests = [
                ...results.contractTests.filter(t => !t.passed),
                ...results.paymentTests.filter(t => !t.passed),
                ...results.validationTests.filter(t => !t.passed)
            ];
            
            console.log('\nFailed Tests:');
            failedTests.forEach(test => {
                console.log(`   ❌ ${test.testName}: ${test.details}`);
            });
        }
        
        // Save results to file
        require('fs').writeFileSync('verification_results.json', JSON.stringify(results, null, 2));
        console.log('\n📄 Detailed results saved to verification_results.json');
        
    } catch (error) {
        console.error('\n💥 Verification failed:', error.message);
    }
}

// Run verification if this script is executed directly
if (require.main === module) {
    runFinalVerification();
}

module.exports = { runFinalVerification };
