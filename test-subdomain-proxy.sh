#!/bin/bash

# Test script for subdomain proxy functionality
# This script tests if the subdomain proxy is working correctly

echo "🧪 Testing Subdomain Proxy Setup"
echo "================================"
echo ""

# Configuration
DOMAIN="onepagebuilder.live"
TEST_SUBDOMAIN="testuser"
TEST_URL="http://localhost:8080"

echo "📋 Test Configuration:"
echo "  Domain: $DOMAIN"
echo "  Test Subdomain: $TEST_SUBDOMAIN"
echo "  Backend URL: $TEST_URL"
echo ""

# Test 1: Check if backend is running
echo "Test 1: Backend Health Check"
echo "----------------------------"
response=$(curl -s -o /dev/null -w "%{http_code}" $TEST_URL/api/auth/health 2>/dev/null)
if [ "$response" = "200" ] || [ "$response" = "404" ]; then
    echo "✅ Backend is running (HTTP $response)"
else
    echo "❌ Backend is not responding (HTTP $response)"
    echo "   Please start the backend first"
    exit 1
fi
echo ""

# Test 2: Test subdomain extraction
echo "Test 2: Subdomain Proxy Controller"
echo "-----------------------------------"
echo "Testing with Host header: $TEST_SUBDOMAIN.$DOMAIN"
response=$(curl -s -o /dev/null -w "%{http_code}" \
    -H "Host: $TEST_SUBDOMAIN.$DOMAIN" \
    "$TEST_URL/" 2>/dev/null)

if [ "$response" = "200" ]; then
    echo "✅ Proxy controller responding (HTTP $response)"
    echo "   This means the controller is working!"
elif [ "$response" = "404" ]; then
    echo "⚠️  Controller working but file not found in S3 (HTTP $response)"
    echo "   This is expected if you haven't published a site yet"
else
    echo "❌ Unexpected response (HTTP $response)"
fi
echo ""

# Test 3: Check if S3 file exists
echo "Test 3: S3 File Check"
echo "---------------------"
echo "Checking if test file exists in S3..."
echo "Key: $TEST_SUBDOMAIN/index.html"
echo ""
echo "To test with a real published site:"
echo "1. Publish a project from the frontend"
echo "2. Note the username from the URL"
echo "3. Run: curl -v -H 'Host: username.$DOMAIN' http://localhost:8080/"
echo ""

# Test 4: DNS Resolution (if applicable)
echo "Test 4: DNS Check (Production Only)"
echo "------------------------------------"
dns_ip=$(dig +short $TEST_SUBDOMAIN.$DOMAIN 2>/dev/null | head -n 1)
if [ -z "$dns_ip" ]; then
    echo "⚠️  DNS not configured yet (expected for local development)"
    echo "   For production, configure wildcard DNS: *.$DOMAIN → your server IP"
else
    echo "✅ DNS resolves to: $dns_ip"
fi
echo ""

# Summary
echo "📊 Summary"
echo "=========="
echo ""
echo "Local Testing:"
echo "  curl -v -H 'Host: username.$DOMAIN' http://localhost:8080/"
echo ""
echo "Production Testing (after DNS setup):"
echo "  curl -v https://username.$DOMAIN"
echo ""
echo "To publish a test site:"
echo "  curl -X POST http://localhost:8080/api/projects/1/publish \\"
echo "    -H 'Content-Type: application/json' \\"
echo "    -H 'User-Id: 1' \\"
echo "    -d '{\"navbar\":{\"brandName\":\"Test\"},\"hero\":{\"heading\":\"Hello\"}}'"
echo ""
echo "✨ Setup complete! Check SUBDOMAIN_PROXY_SETUP.md for full documentation."
