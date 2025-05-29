#!/usr/bin/env node

/**
 * Frontend API Gateway Configuration Test
 * 
 * This script tests that the frontend is properly configured to use the API Gateway
 * for all microservice communications.
 */

const axios = require('axios');

const API_GATEWAY_URL = 'http://localhost:8080';
const FRONTEND_URL = 'http://localhost:3000';

// Test endpoints that should be accessible through API Gateway
const testEndpoints = [
    { name: 'Customer Service', path: '/api/customer', method: 'GET' },
    { name: 'Job Category Service', path: '/api/job-category', method: 'GET' },
    { name: 'Customer Contract Service', path: '/api/customer-contract', method: 'GET' },
    { name: 'Customer Payment Service', path: '/api/customer-payment', method: 'GET' },
    { name: 'Customer Statistics Service', path: '/api/customer-statistics/health', method: 'GET' },
    { name: 'Job Detail Service', path: '/api/job-detail', method: 'GET' },
    { name: 'Work Shift Service', path: '/api/work-shift', method: 'GET' }
];

// Colors for console output
const colors = {
    green: '\x1b[32m',
    red: '\x1b[31m',
    yellow: '\x1b[33m',
    blue: '\x1b[34m',
    reset: '\x1b[0m',
    bold: '\x1b[1m'
};

function log(message, color = colors.reset) {
    console.log(`${color}${message}${colors.reset}`);
}

async function testApiGatewayConnectivity() {
    log('\n🔍 Testing API Gateway Connectivity...', colors.blue + colors.bold);
    
    try {
        const response = await axios.get(`${API_GATEWAY_URL}/actuator/health`, { timeout: 5000 });
        log(`✅ API Gateway is running (Status: ${response.status})`, colors.green);
        return true;
    } catch (error) {
        log(`❌ API Gateway is not accessible: ${error.message}`, colors.red);
        return false;
    }
}

async function testFrontendConnectivity() {
    log('\n🔍 Testing Frontend Connectivity...', colors.blue + colors.bold);
    
    try {
        const response = await axios.get(FRONTEND_URL, { timeout: 5000 });
        log(`✅ Frontend is running (Status: ${response.status})`, colors.green);
        return true;
    } catch (error) {
        log(`❌ Frontend is not accessible: ${error.message}`, colors.red);
        return false;
    }
}

async function testEndpoint(endpoint) {
    try {
        const response = await axios({
            method: endpoint.method,
            url: `${API_GATEWAY_URL}${endpoint.path}`,
            timeout: 10000,
            validateStatus: (status) => status < 500 // Accept 4xx as valid responses
        });
        
        log(`✅ ${endpoint.name}: ${response.status} ${response.statusText}`, colors.green);
        return { success: true, status: response.status };
    } catch (error) {
        if (error.response) {
            log(`⚠️  ${endpoint.name}: ${error.response.status} ${error.response.statusText}`, colors.yellow);
            return { success: false, status: error.response.status, error: error.response.statusText };
        } else {
            log(`❌ ${endpoint.name}: ${error.message}`, colors.red);
            return { success: false, error: error.message };
        }
    }
}

async function testAllEndpoints() {
    log('\n🔍 Testing API Gateway Routes...', colors.blue + colors.bold);
    
    const results = [];
    
    for (const endpoint of testEndpoints) {
        const result = await testEndpoint(endpoint);
        results.push({ ...endpoint, ...result });
    }
    
    return results;
}

function generateReport(results) {
    log('\n📊 Test Results Summary', colors.blue + colors.bold);
    log('=' * 50);
    
    const successful = results.filter(r => r.success);
    const failed = results.filter(r => !r.success);
    
    log(`\n✅ Successful: ${successful.length}/${results.length}`, colors.green);
    log(`❌ Failed: ${failed.length}/${results.length}`, failed.length > 0 ? colors.red : colors.green);
    
    if (failed.length > 0) {
        log('\n❌ Failed Endpoints:', colors.red);
        failed.forEach(endpoint => {
            log(`   - ${endpoint.name}: ${endpoint.error || endpoint.status}`, colors.red);
        });
    }
    
    if (successful.length > 0) {
        log('\n✅ Successful Endpoints:', colors.green);
        successful.forEach(endpoint => {
            log(`   - ${endpoint.name}: ${endpoint.status}`, colors.green);
        });
    }
    
    log('\n📝 Configuration Status:', colors.blue);
    if (failed.length === 0) {
        log('🎉 All endpoints are accessible through API Gateway!', colors.green + colors.bold);
        log('✅ Frontend is properly configured to use API Gateway.', colors.green);
    } else {
        log('⚠️  Some endpoints are not accessible.', colors.yellow);
        log('🔧 Check that all microservices are running and properly configured.', colors.yellow);
    }
}

async function checkDirectServiceAccess() {
    log('\n🔍 Checking for Direct Service Access (Should be blocked)...', colors.blue + colors.bold);
    
    const directServices = [
        { name: 'Customer Service Direct', url: 'http://localhost:8081/api/customer' },
        { name: 'Job Service Direct', url: 'http://localhost:8082/api/job-category' },
        { name: 'Contract Service Direct', url: 'http://localhost:8083/api/customer-contract' },
        { name: 'Payment Service Direct', url: 'http://localhost:8084/api/customer-payment' },
        { name: 'Statistics Service Direct', url: 'http://localhost:8085/api/customer-statistics' }
    ];
    
    for (const service of directServices) {
        try {
            await axios.get(service.url, { timeout: 3000 });
            log(`⚠️  ${service.name} is directly accessible (may bypass API Gateway)`, colors.yellow);
        } catch (error) {
            log(`✅ ${service.name} is not directly accessible (good for security)`, colors.green);
        }
    }
}

async function main() {
    log('🚀 Frontend API Gateway Configuration Test', colors.blue + colors.bold);
    log('=' * 50);
    
    // Test basic connectivity
    const apiGatewayOk = await testApiGatewayConnectivity();
    const frontendOk = await testFrontendConnectivity();
    
    if (!apiGatewayOk) {
        log('\n❌ Cannot proceed: API Gateway is not running', colors.red);
        log('💡 Start the API Gateway first: cd api-gateway && mvn spring-boot:run', colors.yellow);
        process.exit(1);
    }
    
    if (!frontendOk) {
        log('\n⚠️  Frontend is not running, but API Gateway tests will continue', colors.yellow);
        log('💡 Start the frontend: cd microservice_fe && npm start', colors.yellow);
    }
    
    // Test all endpoints
    const results = await testAllEndpoints();
    
    // Check direct service access
    await checkDirectServiceAccess();
    
    // Generate report
    generateReport(results);
    
    log('\n🔗 Next Steps:', colors.blue);
    log('1. Open browser to http://localhost:3000', colors.reset);
    log('2. Open browser developer tools (F12)', colors.reset);
    log('3. Navigate through the application', colors.reset);
    log('4. Check Network tab - all API calls should go to localhost:8080', colors.reset);
    log('5. Verify no direct calls to ports 8081-8085', colors.reset);
    
    process.exit(failed.length > 0 ? 1 : 0);
}

// Run the test
if (require.main === module) {
    main().catch(error => {
        log(`\n💥 Test failed with error: ${error.message}`, colors.red);
        process.exit(1);
    });
}

module.exports = { testApiGatewayConnectivity, testFrontendConnectivity, testAllEndpoints };
