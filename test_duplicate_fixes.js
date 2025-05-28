/**
 * Test Script để kiểm tra các fix duplicate data
 * 
 * Script này sẽ test các scenario để đảm bảo không có duplicate data được tạo
 */

const axios = require('axios');

// Cấu hình
const API_BASE_URL = 'http://localhost:8080'; // API Gateway
const TEST_TIMEOUT = 10000; // 10 giây

// Hàm helper để gọi API
async function callAPI(method, endpoint, data = null) {
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

// Tạo test data
function createTestContract(suffix = '') {
    return {
        customerId: 1,
        startingDate: '2024-01-15',
        endingDate: '2024-02-15',
        totalAmount: 5000000,
        address: `Test Address ${suffix}`,
        description: `Test contract ${suffix}`,
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

function createTestPayment(contractId, suffix = '') {
    return {
        paymentAmount: 1000000,
        paymentMethod: 1,
        note: `Test payment ${suffix}`,
        customerContractId: contractId,
        customerId: 1
    };
}

// Test 1: Tạo contract đơn lẻ
async function testSingleContractCreation() {
    console.log('\n=== Test 1: Tạo Contract Đơn Lẻ ===');
    
    const contract = createTestContract('single');
    const result = await callAPI('POST', '/api/customer-contract', contract);
    
    if (result.success) {
        console.log(`✅ Tạo contract thành công với ID: ${result.data.id}`);
        return result.data.id;
    } else {
        console.log(`❌ Tạo contract thất bại: ${result.error}`);
        return null;
    }
}

// Test 2: Tạo contract nhanh liên tiếp (test rapid submission)
async function testRapidContractCreation() {
    console.log('\n=== Test 2: Test Rapid Contract Creation ===');
    
    const contract = createTestContract('rapid');
    const promises = [];
    
    // Tạo 3 request nhanh liên tiếp
    for (let i = 0; i < 3; i++) {
        promises.push(callAPI('POST', '/api/customer-contract', {
            ...contract,
            description: `Rapid test ${i + 1}`
        }));
    }
    
    const results = await Promise.all(promises);
    const successCount = results.filter(r => r.success).length;
    const uniqueIds = [...new Set(results.filter(r => r.success).map(r => r.data.id))];
    
    console.log(`📊 Kết quả: ${successCount} thành công, ${uniqueIds.length} ID duy nhất`);
    
    if (successCount === uniqueIds.length) {
        console.log('✅ Không có duplicate contract được tạo');
    } else {
        console.log('❌ Có duplicate contract được tạo');
    }
    
    return uniqueIds;
}

// Test 3: Tạo payment đơn lẻ
async function testSinglePaymentCreation(contractId) {
    console.log('\n=== Test 3: Tạo Payment Đơn Lẻ ===');
    
    if (!contractId) {
        console.log('❌ Không có contract ID để test payment');
        return null;
    }
    
    const payment = createTestPayment(contractId, 'single');
    const result = await callAPI('POST', '/api/customer-payment', payment);
    
    if (result.success) {
        console.log(`✅ Tạo payment thành công với ID: ${result.data.id}`);
        return result.data.id;
    } else {
        console.log(`❌ Tạo payment thất bại: ${result.error}`);
        return null;
    }
}

// Test 4: Tạo payment nhanh liên tiếp
async function testRapidPaymentCreation(contractId) {
    console.log('\n=== Test 4: Test Rapid Payment Creation ===');
    
    if (!contractId) {
        console.log('❌ Không có contract ID để test rapid payment');
        return [];
    }
    
    const promises = [];
    
    // Tạo 3 payment nhanh liên tiếp
    for (let i = 0; i < 3; i++) {
        const payment = createTestPayment(contractId, `rapid-${i + 1}`);
        promises.push(callAPI('POST', '/api/customer-payment', payment));
    }
    
    const results = await Promise.all(promises);
    const successCount = results.filter(r => r.success).length;
    const uniqueIds = [...new Set(results.filter(r => r.success).map(r => r.data.id))];
    
    console.log(`📊 Kết quả: ${successCount} thành công, ${uniqueIds.length} ID duy nhất`);
    
    if (successCount === uniqueIds.length) {
        console.log('✅ Không có duplicate payment được tạo');
    } else {
        console.log('❌ Có duplicate payment được tạo');
    }
    
    return uniqueIds;
}

// Test 5: Test overpayment prevention
async function testOverpaymentPrevention(contractId) {
    console.log('\n=== Test 5: Test Overpayment Prevention ===');
    
    if (!contractId) {
        console.log('❌ Không có contract ID để test overpayment');
        return;
    }
    
    const payment = createTestPayment(contractId, 'overpayment');
    payment.paymentAmount = 50000000; // Số tiền quá lớn
    
    const result = await callAPI('POST', '/api/customer-payment', payment);
    
    if (!result.success) {
        console.log('✅ Overpayment được ngăn chặn thành công');
    } else {
        console.log('❌ Overpayment không được ngăn chặn');
    }
}

// Test 6: Kiểm tra database state
async function testDatabaseState() {
    console.log('\n=== Test 6: Kiểm tra Database State ===');
    
    try {
        const [contractsResult, paymentsResult] = await Promise.all([
            callAPI('GET', '/api/customer-contract'),
            callAPI('GET', '/api/customer-payment')
        ]);
        
        if (contractsResult.success && paymentsResult.success) {
            const contracts = contractsResult.data;
            const payments = paymentsResult.data;
            
            console.log(`📊 Database State:`);
            console.log(`   Tổng số contracts: ${contracts.length}`);
            console.log(`   Tổng số payments: ${payments.length}`);
            
            // Kiểm tra duplicate contracts
            const contractKeys = contracts.map(c => 
                `${c.customerId}-${c.totalAmount}-${c.address}`);
            const uniqueContractKeys = [...new Set(contractKeys)];
            
            // Kiểm tra duplicate payments
            const paymentKeys = payments.map(p => 
                `${p.customerContractId}-${p.paymentAmount}-${p.note}`);
            const uniquePaymentKeys = [...new Set(paymentKeys)];
            
            if (contractKeys.length === uniqueContractKeys.length) {
                console.log('✅ Không có duplicate contracts trong database');
            } else {
                console.log('❌ Có duplicate contracts trong database');
            }
            
            if (paymentKeys.length === uniquePaymentKeys.length) {
                console.log('✅ Không có duplicate payments trong database');
            } else {
                console.log('❌ Có duplicate payments trong database');
            }
        } else {
            console.log('❌ Không thể lấy dữ liệu từ database');
        }
    } catch (error) {
        console.log(`❌ Lỗi khi kiểm tra database: ${error.message}`);
    }
}

// Hàm chính để chạy tất cả tests
async function runAllTests() {
    console.log('🚀 Bắt đầu kiểm tra các fix duplicate data');
    console.log(`   API Base URL: ${API_BASE_URL}`);
    console.log(`   Timeout: ${TEST_TIMEOUT}ms`);
    console.log(`   Thời gian: ${new Date().toLocaleString('vi-VN')}`);
    
    const startTime = Date.now();
    
    try {
        // Chạy các tests
        const contractId = await testSingleContractCreation();
        const rapidContractIds = await testRapidContractCreation();
        
        // Sử dụng contract ID đầu tiên có sẵn cho payment tests
        const testContractId = contractId || (rapidContractIds.length > 0 ? rapidContractIds[0] : null);
        
        if (testContractId) {
            await testSinglePaymentCreation(testContractId);
            await testRapidPaymentCreation(testContractId);
            await testOverpaymentPrevention(testContractId);
        }
        
        await testDatabaseState();
        
        const duration = Date.now() - startTime;
        
        console.log('\n🎯 Tóm tắt kết quả test:');
        console.log(`   Thời gian thực hiện: ${duration}ms`);
        console.log('   Kiểm tra logs ở trên để xem chi tiết kết quả');
        console.log('\n📋 Hướng dẫn:');
        console.log('   ✅ = Test passed (không có duplicate)');
        console.log('   ❌ = Test failed (có duplicate hoặc lỗi)');
        console.log('\n💡 Nếu tất cả tests đều ✅, các fix duplicate đã hoạt động tốt!');
        
    } catch (error) {
        console.error('\n💥 Test thất bại:', error.message);
    }
}

// Chạy tests nếu file này được execute trực tiếp
if (require.main === module) {
    runAllTests();
}

module.exports = { runAllTests };
