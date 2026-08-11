#!/bin/bash
#
# ZeroFake end-to-end smoke test.
#
# Exercises the running stack over HTTP: authentication, role-based access
# control, the product catalogue, QR code generation and the verification
# workflow. Every assertion is against a real response from a real service.
#
# Usage:
#   ./scripts/smoke-test.sh
#
# Requires the stack to be up (docker compose up). Steps that need a live
# Hyperledger Fabric network are skipped automatically when the peer is
# unreachable, and reported as skipped rather than passed.

set -uo pipefail

AUTH_URL="${AUTH_URL:-http://localhost:8081}"
PRODUCT_URL="${PRODUCT_URL:-http://localhost:8082}"
BLOCKCHAIN_URL="${BLOCKCHAIN_URL:-http://localhost:8083}"
FRAUD_URL="${FRAUD_URL:-http://localhost:8085}"

PASSWORD="${SEED_DEFAULT_PASSWORD:-Password123!}"

PASS=0
FAIL=0
SKIP=0

GREEN=$'\033[0;32m'; RED=$'\033[0;31m'; YELLOW=$'\033[0;33m'; DIM=$'\033[2m'; OFF=$'\033[0m'

pass() { PASS=$((PASS+1)); echo "  ${GREEN}PASS${OFF}  $1"; }
fail() { FAIL=$((FAIL+1)); echo "  ${RED}FAIL${OFF}  $1"; [ -n "${2:-}" ] && echo "        ${DIM}$2${OFF}"; }
skip() { SKIP=$((SKIP+1)); echo "  ${YELLOW}SKIP${OFF}  $1"; [ -n "${2:-}" ] && echo "        ${DIM}$2${OFF}"; }
section() { echo ""; echo "== $1"; }

# status_of METHOD URL [TOKEN] [BODY]
status_of() {
  local method="$1" url="$2" token="${3:-}" body="${4:-}"
  local args=(-s -o /dev/null -w '%{http_code}' -X "$method" "$url" -H 'Content-Type: application/json')
  [ -n "$token" ] && args+=(-H "Authorization: Bearer $token")
  [ -n "$body" ] && args+=(-d "$body")
  curl "${args[@]}" 2>/dev/null
}

# body_of METHOD URL [TOKEN] [BODY]
body_of() {
  local method="$1" url="$2" token="${3:-}" body="${4:-}"
  local args=(-s -X "$method" "$url" -H 'Content-Type: application/json')
  [ -n "$token" ] && args+=(-H "Authorization: Bearer $token")
  [ -n "$body" ] && args+=(-d "$body")
  curl "${args[@]}" 2>/dev/null
}

# json_field FIELD  (reads stdin; first occurrence of a scalar field, no jq dependency)
json_field() {
  grep -o "\"$1\"[[:space:]]*:[[:space:]]*\"\?[^\",}]*" \
    | head -1 \
    | sed -E "s/\"$1\"[[:space:]]*:[[:space:]]*\"?//"
}

# data_id  (reads stdin; the id of the object inside the ApiResponse envelope)
#
# A product response also carries its category's id, so a naive search for
# "id" finds the wrong one. This anchors on the envelope.
data_id() {
  grep -o '"data"[[:space:]]*:[[:space:]]*{[[:space:]]*"id"[[:space:]]*:[[:space:]]*"[^"]*"' \
    | head -1 \
    | grep -o '[0-9a-fA-F-]\{36\}'
}

# first_list_id  (reads stdin; the id of the first object in a list payload)
first_list_id() {
  grep -o '"data"[[:space:]]*:[[:space:]]*\[[[:space:]]*{[[:space:]]*"id"[[:space:]]*:[[:space:]]*"[^"]*"' \
    | head -1 \
    | grep -o '[0-9a-fA-F-]\{36\}'
}

expect_status() {
  local label="$1" expected="$2" actual="$3"
  if [ "$actual" = "$expected" ]; then
    pass "$label"
  else
    fail "$label" "expected HTTP $expected, got $actual"
  fi
}

login() {
  body_of POST "$AUTH_URL/api/v1/auth/login" "" \
    "{\"email\":\"$1\",\"password\":\"$PASSWORD\"}"
}

echo "=============================================="
echo " ZeroFake smoke test"
echo "=============================================="

# ---------------------------------------------------------------------------
section "Service health"

for pair in "auth:$AUTH_URL" "product:$PRODUCT_URL" "blockchain:$BLOCKCHAIN_URL" "fraud:$FRAUD_URL"; do
  name="${pair%%:*}"; url="${pair#*:}"
  code=$(status_of GET "$url/actuator/health")
  expect_status "$name service is healthy" "200" "$code"
done

if [ "$FAIL" -gt 0 ]; then
  echo ""
  echo "${RED}Services are not up. Start the stack with: docker compose up -d${OFF}"
  exit 1
fi

# ---------------------------------------------------------------------------
section "Authentication"

MANUFACTURER_TOKEN=$(login "manufacturer@zerofake.com" | json_field accessToken)
CUSTOMER_TOKEN=$(login "customer@zerofake.com" | json_field accessToken)

if [ -n "$MANUFACTURER_TOKEN" ] && [ "${#MANUFACTURER_TOKEN}" -gt 40 ]; then
  pass "manufacturer can log in and receives an access token"
else
  fail "manufacturer can log in" "no access token in response"
  echo "${RED}Cannot continue without a token.${OFF}"; exit 1
fi

[ -n "$CUSTOMER_TOKEN" ] && pass "customer can log in" || fail "customer can log in"

code=$(status_of POST "$AUTH_URL/api/v1/auth/login" "" \
  '{"email":"manufacturer@zerofake.com","password":"WrongPassword1!"}')
expect_status "wrong password is 401, not 500" "401" "$code"

code=$(status_of POST "$AUTH_URL/api/v1/auth/login" "" \
  '{"email":"nobody@zerofake.com","password":"Password123!"}')
expect_status "unknown user is 401, not 500" "401" "$code"

code=$(status_of GET "$AUTH_URL/api/v1/auth/me" "$MANUFACTURER_TOKEN")
expect_status "authenticated user can read their profile" "200" "$code"

code=$(status_of GET "$AUTH_URL/api/v1/auth/me")
expect_status "profile is refused without a token" "401" "$code"

# The token must carry userId and role claims for downstream authorisation.
CLAIMS=$(echo "$MANUFACTURER_TOKEN" | cut -d. -f2 | tr '_-' '/+' | base64 -d 2>/dev/null)
echo "$CLAIMS" | grep -q '"userId"' \
  && pass "token carries the userId claim" \
  || fail "token carries the userId claim" "$CLAIMS"
echo "$CLAIMS" | grep -q 'ROLE_MANUFACTURER' \
  && pass "token carries the role claim" \
  || fail "token carries the role claim" "$CLAIMS"

MANUFACTURER_ID=$(echo "$CLAIMS" | json_field userId)

# ---------------------------------------------------------------------------
section "Access control"

code=$(status_of GET "$PRODUCT_URL/api/v1/products")
expect_status "product list is refused without a token" "401" "$code"

code=$(status_of GET "$BLOCKCHAIN_URL/api/v1/blockchain/transactions")
expect_status "ledger audit log is refused without a token" "401" "$code"

code=$(status_of GET "$FRAUD_URL/api/v1/fraud/reports")
expect_status "fraud reports are refused without a token" "401" "$code"

code=$(status_of POST "$BLOCKCHAIN_URL/api/v1/blockchain/register-product" "$CUSTOMER_TOKEN" \
  "{\"productId\":\"00000000-0000-0000-0000-000000000001\",\"manufacturerId\":\"$MANUFACTURER_ID\"}")
expect_status "a customer cannot register a product on the ledger" "403" "$code"

code=$(status_of GET "$FRAUD_URL/api/v1/fraud/reports" "$CUSTOMER_TOKEN")
expect_status "a customer cannot read aggregated fraud reports" "403" "$code"

code=$(status_of GET "$PRODUCT_URL/api/v1/products" "$CUSTOMER_TOKEN")
expect_status "a customer can browse the catalogue" "200" "$code"

code=$(status_of POST "$AUTH_URL/api/v1/auth/register" "" \
  '{"firstName":"Mallory","lastName":"Escalation","email":"mallory@zerofake.com","password":"Password123!","role":"ROLE_ADMIN"}')
expect_status "self-registration as ROLE_ADMIN is refused" "400" "$code"

# ---------------------------------------------------------------------------
section "Product catalogue"

CATEGORY_ID=$(body_of GET "$PRODUCT_URL/api/v1/categories" "$MANUFACTURER_TOKEN" | first_list_id)
[ -n "$CATEGORY_ID" ] && pass "seeded categories are available" || fail "seeded categories are available"

CODE="SMOKE-$(date +%s)-$RANDOM"
CREATED=$(body_of POST "$PRODUCT_URL/api/v1/products" "$MANUFACTURER_TOKEN" \
  "{\"productCode\":\"$CODE\",\"productName\":\"Smoke Test Widget\",\"description\":\"Created by the smoke test\",\"brand\":\"ZeroFake\",\"manufacturerId\":\"$MANUFACTURER_ID\",\"categoryId\":\"$CATEGORY_ID\"}")

PRODUCT_ID=$(echo "$CREATED" | data_id)

if [ -n "$PRODUCT_ID" ] && [ "${#PRODUCT_ID}" -eq 36 ]; then
  pass "manufacturer can create a product ($CODE)"
else
  fail "manufacturer can create a product" "$CREATED"
fi

echo "$CREATED" | grep -q '"blockchainStatus":"PENDING"' \
  && pass "a new product starts as PENDING" \
  || fail "a new product starts as PENDING"

echo "$CREATED" | grep -q "\"qrCodePath\":\"$PRODUCT_ID.png\"" \
  && pass "a QR code path is assigned on creation" \
  || fail "a QR code path is assigned on creation"

code=$(status_of GET "$PRODUCT_URL/api/v1/products/$PRODUCT_ID/qr-code" "$MANUFACTURER_TOKEN")
expect_status "the QR code image is downloadable" "200" "$code"

CONTENT_TYPE=$(curl -s -o /dev/null -w '%{content_type}' \
  -H "Authorization: Bearer $MANUFACTURER_TOKEN" \
  "$PRODUCT_URL/api/v1/products/$PRODUCT_ID/qr-code" 2>/dev/null)
[ "$CONTENT_TYPE" = "image/png" ] \
  && pass "the QR code is served as a PNG" \
  || fail "the QR code is served as a PNG" "content-type was $CONTENT_TYPE"

code=$(status_of POST "$PRODUCT_URL/api/v1/products" "$CUSTOMER_TOKEN" \
  "{\"productCode\":\"$CODE-X\",\"productName\":\"Forbidden\",\"brand\":\"X\",\"manufacturerId\":\"$MANUFACTURER_ID\",\"categoryId\":\"$CATEGORY_ID\"}")
expect_status "a customer cannot create a product" "403" "$code"

code=$(status_of POST "$PRODUCT_URL/api/v1/products" "$MANUFACTURER_TOKEN" \
  "{\"productCode\":\"$CODE\",\"productName\":\"Duplicate\",\"brand\":\"X\",\"manufacturerId\":\"$MANUFACTURER_ID\",\"categoryId\":\"$CATEGORY_ID\"}")
expect_status "a duplicate product code is 409" "409" "$code"

code=$(status_of PATCH "$PRODUCT_URL/api/v1/products/$PRODUCT_ID/blockchain-status?status=NONSENSE" "$MANUFACTURER_TOKEN")
expect_status "an invalid blockchain status is 400, not 500" "400" "$code"

code=$(status_of GET "$PRODUCT_URL/api/v1/products/11111111-1111-1111-1111-111111111111" "$MANUFACTURER_TOKEN")
expect_status "an unknown product is 404" "404" "$code"

# ---------------------------------------------------------------------------
section "Verification and fraud detection"

FABRIC_UP=false
if (timeout 3 bash -c "</dev/tcp/127.0.0.1/7051") 2>/dev/null; then
  FABRIC_UP=true
fi

# A product that exists in no catalogue at all is the strongest counterfeit
# signal there is, and needs no blockchain to detect.
GHOST="99999999-9999-9999-9999-999999999999"
VERDICT=$(body_of POST "$FRAUD_URL/api/v1/fraud/verify" "$CUSTOMER_TOKEN" \
  "{\"productId\":\"$GHOST\",\"ipAddress\":\"203.0.113.5\",\"deviceInfo\":\"smoke-test\",\"location\":\"Bengaluru\"}")

echo "$VERDICT" | grep -q '"verificationResult":"COUNTERFEIT"' \
  && pass "an unknown product is reported COUNTERFEIT" \
  || fail "an unknown product is reported COUNTERFEIT" "$VERDICT"

echo "$VERDICT" | grep -q 'PRODUCT_NOT_FOUND' \
  && pass "the PRODUCT_NOT_FOUND rule is reported" \
  || fail "the PRODUCT_NOT_FOUND rule is reported" "$VERDICT"

echo "$VERDICT" | grep -q '"riskScore":100' \
  && pass "an unknown product scores 100 risk" \
  || fail "an unknown product scores 100 risk" "$VERDICT"

code=$(status_of POST "$FRAUD_URL/api/v1/fraud/verify" "" \
  "{\"productId\":\"$GHOST\",\"location\":\"Bengaluru\"}")
expect_status "verification is refused without a token" "401" "$code"

REPORT=$(body_of POST "$FRAUD_URL/api/v1/fraud/reports" "$CUSTOMER_TOKEN" \
  "{\"productId\":\"$PRODUCT_ID\",\"description\":\"Raised by the smoke test.\"}")
echo "$REPORT" | grep -q '"reportId"' \
  && pass "a user can raise a fraud report" \
  || fail "a user can raise a fraud report" "$REPORT"

if [ "$FABRIC_UP" = true ]; then
  REG=$(body_of POST "$BLOCKCHAIN_URL/api/v1/blockchain/register-product" "$MANUFACTURER_TOKEN" \
    "{\"productId\":\"$PRODUCT_ID\",\"manufacturerId\":\"$MANUFACTURER_ID\"}")

  echo "$REG" | grep -q '"status":"SUCCESS"' \
    && pass "the product is registered on the ledger" \
    || fail "the product is registered on the ledger" "$REG"

  TX_ID=$(echo "$REG" | json_field transactionId)
  [ -n "$TX_ID" ] && [ "${TX_ID#ALREADY_REGISTERED}" = "$TX_ID" ] \
    && pass "a real Fabric transaction ID is recorded" \
    || fail "a real Fabric transaction ID is recorded" "got '$TX_ID'"

  echo "$REG" | grep -qE '"blockNumber":[0-9]+' \
    && pass "the committing block number is recorded" \
    || fail "the committing block number is recorded" "$REG"

  STATUS=$(body_of GET "$PRODUCT_URL/api/v1/products/$PRODUCT_ID" "$MANUFACTURER_TOKEN")
  echo "$STATUS" | grep -q '"blockchainStatus":"REGISTERED"' \
    && pass "the catalogue is promoted to REGISTERED by the blockchain service" \
    || fail "the catalogue is promoted to REGISTERED" "$STATUS"

  code=$(status_of POST "$BLOCKCHAIN_URL/api/v1/blockchain/register-product" "$MANUFACTURER_TOKEN" \
    "{\"productId\":\"$PRODUCT_ID\",\"manufacturerId\":\"$MANUFACTURER_ID\"}")
  expect_status "re-registering the same product is 409" "409" "$code"

  GENUINE=$(body_of POST "$FRAUD_URL/api/v1/fraud/verify" "$CUSTOMER_TOKEN" \
    "{\"productId\":\"$PRODUCT_ID\",\"ipAddress\":\"203.0.113.5\",\"deviceInfo\":\"smoke-test\",\"location\":\"Bengaluru\"}")
  echo "$GENUINE" | grep -q '"verificationResult":"GENUINE"' \
    && pass "a registered product verifies as GENUINE" \
    || fail "a registered product verifies as GENUINE" "$GENUINE"

  code=$(status_of GET "$BLOCKCHAIN_URL/api/v1/blockchain/products/$PRODUCT_ID/history" "$CUSTOMER_TOKEN")
  expect_status "the ledger history is readable" "200" "$code"
else
  skip "ledger registration"          "Fabric peer is not listening on :7051"
  skip "BLOCKCHAIN_MISMATCH detection" "Fabric peer is not listening on :7051"
  skip "GENUINE verification"          "Fabric peer is not listening on :7051"
  skip "ledger history"                "Fabric peer is not listening on :7051"
fi

# ---------------------------------------------------------------------------
echo ""
echo "=============================================="
printf " %sPassed: %d%s   %sFailed: %d%s   %sSkipped: %d%s\n" \
  "$GREEN" "$PASS" "$OFF" "$RED" "$FAIL" "$OFF" "$YELLOW" "$SKIP" "$OFF"
echo "=============================================="

[ "$FAIL" -eq 0 ] || exit 1
