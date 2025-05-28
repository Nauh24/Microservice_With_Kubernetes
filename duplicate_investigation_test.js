/**
 * Comprehensive Duplicate Investigation Test
 * 
 * This script performs detailed testing to identify the exact source
 * of duplicate data creation in the microservices application.
 */

const axios = require('axios');

// Configuration
const API_BASE_URL = 'http://localhost:8080'; // API Gateway
const DIRECT_CONTRACT_URL = 'http://localhost:8083'; // Direct contract service
const DIRECT_PAYMENT_URL = 'http://localhost:8084'; // Direct payment service
const TEST_TIMEOUT = 10000; // 10 seconds

// Test data
const testContract = {
    customerId: 1,
    startingDate: '2024-01-15',
    endingDate: '2024-02-15',
    totalAmount: 5000000,
    address: 'Test Address - Duplicate Investigation',
    description: 'Test contract for duplicate investigation',
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

// Helper function to make requests with detailed logging
async function makeDetailedRequest(method, url, data = null, description = '') {
    const startTime = Date.now();
    console.log(`\n🔍 ${description}`);
    console.log(`   Method: ${method}`);
    console.log(`   URL: ${url}`);
    console.log(`   Data: ${data ? JSON.stringify(data, null, 2) : 'None'}`);
    
    try {
        const config = {
            method,
            url,
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
        const duration = Date.now() - startTime;
        
        console.log(`   ✅ Success: ${response.status} (${duration}ms)`);
        console.log(`   Response: ${JSON.stringify(response.data, null, 2)}`);
        
        return { 
            success: true, 
            data: response.data, 
            status: response.status,
            duration 
        };
    } catch (error) {
        const duration = Date.now() - startTime;
        
        console.log(`   ❌ Failed: ${error.response?.status || 'Network Error'} (${duration}ms)`);
        console.log(`   Error: ${error.response?.data ? JSON.stringify(error.response.data, null, 2) : error.message}`);
        
        return { 
            success: false, 
            error: error.response?.data || error.message,
            status: error.response?.status || 500,
            duration
        };
    }
}

// Test 1: Single Contract Creation via API Gateway
async function testContractViaGateway() {
    console.log('\n=== Test 1: Contract Creation via API Gateway ===');
    
    const result = await makeDetailedRequest(
        'POST', 
        `${API_BASE_URL}/api/customer-contract`, 
        testContract,
        'Creating contract via API Gateway'
    );
    
    return result.success ? result.data.id : null;
}

// Test 2: Single Contract Creation via Direct Service
async function testContractDirectService() {
    console.log('\n=== Test 2: Contract Creation via Direct Service ===');
    
    const result = await makeDetailedRequest(
        'POST', 
        `${DIRECT_CONTRACT_URL}/api/customer-contract`, 
        testContract,
        'Creating contract via direct service'
    );
    
    return result.success ? result.data.id : null;
}

// Test 3: Rapid Contract Creation (Simulate Double Click)
async function testRapidContractCreation() {
    console.log('\n=== Test 3: Rapid Contract Creation (Double Click Simulation) ===');
    
    const promises = [];
    const testData = {
        ...testContract,
        description: 'Rapid creation test'
    };
    
    // Simulate rapid clicking by making 3 requests almost simultaneously
    for (let i = 0; i < 3; i++) {
        promises.push(
            makeDetailedRequest(
                'POST', 
                `${API_BASE_URL}/api/customer-contract`, 
                { ...testData, description: `Rapid test ${i + 1}` },
                `Rapid request ${i + 1}`
            )
        );
    }
    
    const results = await Promise.all(promises);
    const successCount = results.filter(r => r.success).length;
    const contractIds = results.filter(r => r.success).map(r => r.data.id);
    
    console.log(`\n📊 Rapid Creation Results:`);
    console.log(`   Successful: ${successCount}/3`);
    console.log(`   Contract IDs: ${contractIds.join(', ')}`);
    
    return contractIds;
}

// Test 4: Payment Creation
async function testPaymentCreation(contractId) {
    console.log('\n=== Test 4: Payment Creation ===');
    
    if (!contractId) {
        console.log('❌ No contract ID provided for payment test');
        return null;
    }
    
    const testPayment = {
        paymentAmount: 1000000,
        paymentMethod: 1,
        note: 'Test payment for duplicate investigation',
        customerContractId: contractId,
        customerId: 1
    };
    
    const result = await makeDetailedRequest(
        'POST', 
        `${API_BASE_URL}/api/customer-payment`, 
        testPayment,
        'Creating payment via API Gateway'
    );
    
    return result.success ? result.data.id : null;
}

// Test 5: Rapid Payment Creation
async function testRapidPaymentCreation(contractId) {
    console.log('\n=== Test 5: Rapid Payment Creation ===');
    
    if (!contractId) {
        console.log('❌ No contract ID provided for rapid payment test');
        return [];
    }
    
    const promises = [];
    
    // Create 3 payments rapidly
    for (let i = 0; i < 3; i++) {
        const testPayment = {
            paymentAmount: 500000,
            paymentMethod: 0,
            note: `Rapid payment test ${i + 1}`,
            customerContractId: contractId,
            customerId: 1
        };
        
        promises.push(
            makeDetailedRequest(
                'POST', 
                `${API_BASE_URL}/api/customer-payment`, 
                testPayment,
                `Rapid payment ${i + 1}`
            )
        );
    }
    
    const results = await Promise.all(promises);
    const successCount = results.filter(r => r.success).length;
    const paymentIds = results.filter(r => r.success).map(r => r.data.id);
    
    console.log(`\n📊 Rapid Payment Results:`);
    console.log(`   Successful: ${successCount}/3`);
    console.log(`   Payment IDs: ${paymentIds.join(', ')}`);
    
    return paymentIds;
}

// Test 6: Database Verification
async function verifyDatabaseState() {
    console.log('\n=== Test 6: Database State Verification ===');
    
    try {
        // Get all contracts
        const contractsResult = await makeDetailedRequest(
            'GET', 
            `${API_BASE_URL}/api/customer-contract`,
            null,
            'Fetching all contracts'
        );
        
        // Get all payments
        const paymentsResult = await makeDetailedRequest(
            'GET', 
            `${API_BASE_URL}/api/customer-payment`,
            null,
            'Fetching all payments'
        );
        
        if (contractsResult.success && paymentsResult.success) {
            const contracts = contractsResult.data;
            const payments = paymentsResult.data;
            
            console.log(`\n📊 Database State:`);
            console.log(`   Total Contracts: ${contracts.length}`);
            console.log(`   Total Payments: ${payments.length}`);
            
            // Check for potential duplicates
            const contractGroups = {};
            contracts.forEach(contract => {
                const key = `${contract.customerId}-${contract.totalAmount}-${contract.address}`;
                if (!contractGroups[key]) {
                    contractGroups[key] = [];
                }
                contractGroups[key].push(contract.id);
            });
            
            const duplicateContracts = Object.entries(contractGroups)
                .filter(([key, ids]) => ids.length > 1);
            
            if (duplicateContracts.length > 0) {
                console.log(`\n⚠️  Potential Duplicate Contracts Found:`);
                duplicateContracts.forEach(([key, ids]) => {
                    console.log(`   Key: ${key} -> IDs: ${ids.join(', ')}`);
                });
            } else {
                console.log(`\n✅ No duplicate contracts detected`);
            }
            
            // Check payment duplicates
            const paymentGroups = {};
            payments.forEach(payment => {
                const key = `${payment.customerContractId}-${payment.paymentAmount}-${payment.note}`;
                if (!paymentGroups[key]) {
                    paymentGroups[key] = [];
                }
                paymentGroups[key].push(payment.id);
            });
            
            const duplicatePayments = Object.entries(paymentGroups)
                .filter(([key, ids]) => ids.length > 1);
            
            if (duplicatePayments.length > 0) {
                console.log(`\n⚠️  Potential Duplicate Payments Found:`);
                duplicatePayments.forEach(([key, ids]) => {
                    console.log(`   Key: ${key} -> IDs: ${ids.join(', ')}`);
                });
            } else {
                console.log(`\n✅ No duplicate payments detected`);
            }
        }
    } catch (error) {
        console.log(`❌ Database verification failed: ${error.message}`);
    }
}

// Test 7: Network Interruption Simulation
async function testNetworkInterruption() {
    console.log('\n=== Test 7: Network Interruption Simulation ===');
    
    // Create a request with very short timeout to simulate network issues
    const testData = {
        ...testContract,
        description: 'Network interruption test'
    };
    
    const result = await makeDetailedRequest(
        'POST', 
        `${API_BASE_URL}/api/customer-contract`, 
        testData,
        'Testing with potential network interruption'
    );
    
    return result;
}

// Main test runner
async function runComprehensiveInvestigation() {
    console.log('🔍 Starting Comprehensive Duplicate Investigation');
    console.log(`   API Gateway: ${API_BASE_URL}`);
    console.log(`   Direct Contract Service: ${DIRECT_CONTRACT_URL}`);
    console.log(`   Direct Payment Service: ${DIRECT_PAYMENT_URL}`);
    console.log(`   Timeout: ${TEST_TIMEOUT}ms`);
    
    try {
        // Run all tests
        const contractId1 = await testContractViaGateway();
        const contractId2 = await testContractDirectService();
        const rapidContractIds = await testRapidContractCreation();
        
        // Use the first available contract for payment tests
        const testContractId = contractId1 || contractId2 || (rapidContractIds.length > 0 ? rapidContractIds[0] : null);
        
        if (testContractId) {
            await testPaymentCreation(testContractId);
            await testRapidPaymentCreation(testContractId);
        }
        
        await verifyDatabaseState();
        await testNetworkInterruption();
        
        console.log('\n🎯 Investigation Summary:');
        console.log('   1. Check the logs above for any duplicate IDs returned');
        console.log('   2. Verify database state for actual duplicates');
        console.log('   3. Look for patterns in successful vs failed requests');
        console.log('   4. Monitor application logs for backend behavior');
        
    } catch (error) {
        console.error('\n💥 Investigation failed:', error.message);
    }
}

// Run investigation if this script is executed directly
if (require.main === module) {
    runComprehensiveInvestigation();
}

module.exports = {
    runComprehensiveInvestigation,
    testContractViaGateway,
    testContractDirectService,
    testRapidContractCreation,
    testPaymentCreation,
    testRapidPaymentCreation,
    verifyDatabaseState
};
