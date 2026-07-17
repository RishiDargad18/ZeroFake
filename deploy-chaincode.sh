#!/bin/bash
set -e

# ZeroFake Chaincode Re-Deployment Script
# Fresh channel setup, so we use sequence=1.

FABRIC_SAMPLES=/home/rishi_dargad18/hyperledger/fabric-samples
TEST_NETWORK=$FABRIC_SAMPLES/test-network
CHAINCODE_NAME=zerofake
CHAINCODE_VERSION=2.0
CHAINCODE_SEQUENCE=1
CHANNEL=mychannel

export PATH="$PATH:$FABRIC_SAMPLES/bin"
export FABRIC_CFG_PATH=$FABRIC_SAMPLES/config

setOrg1() {
  export CORE_PEER_TLS_ENABLED=true
  export CORE_PEER_LOCALMSPID=Org1MSP
  export CORE_PEER_TLS_ROOTCERT_FILE=$TEST_NETWORK/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt
  export CORE_PEER_MSPCONFIGPATH=$TEST_NETWORK/organizations/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp
  export CORE_PEER_ADDRESS=localhost:7051
}

setOrg2() {
  export CORE_PEER_TLS_ENABLED=true
  export CORE_PEER_LOCALMSPID=Org2MSP
  export CORE_PEER_TLS_ROOTCERT_FILE=$TEST_NETWORK/organizations/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt
  export CORE_PEER_MSPCONFIGPATH=$TEST_NETWORK/organizations/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp
  export CORE_PEER_ADDRESS=localhost:9051
}

ORDERER_CA=$TEST_NETWORK/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem
ORDERER_ADDRESS=localhost:7050

# Copy chaincode to /tmp to avoid Windows path issues
CHAINCODE_TMP=/tmp/zerofake-chaincode
rm -rf $CHAINCODE_TMP
cp -r "/mnt/c/Users/RISHI D/Desktop/Projects/ZeroFake/services/zerofake-chaincode" $CHAINCODE_TMP

echo "=== Step 1: Vendor Go dependencies ==="
cd $CHAINCODE_TMP
go mod vendor
cd /tmp

echo "=== Step 2: Package chaincode (version 2.0) ==="
rm -f /tmp/zerofake.tar.gz
peer lifecycle chaincode package zerofake.tar.gz \
  --path $CHAINCODE_TMP \
  --lang golang \
  --label ${CHAINCODE_NAME}_${CHAINCODE_VERSION}
echo "Packaged."

echo "=== Step 3: Install on Org1 ==="
setOrg1
peer lifecycle chaincode install /tmp/zerofake.tar.gz || true
echo "Installed on Org1."

echo "=== Step 4: Install on Org2 ==="
setOrg2
peer lifecycle chaincode install /tmp/zerofake.tar.gz || true
echo "Installed on Org2."

echo "=== Step 5: Get Package ID ==="
setOrg1
INSTALLED=$(peer lifecycle chaincode queryinstalled 2>&1)
echo "Installed packages: $INSTALLED"
PACKAGE_ID=$(echo "$INSTALLED" | grep "Package ID:" | grep "${CHAINCODE_NAME}_${CHAINCODE_VERSION}" | head -1 | sed 's/.*Package ID: //' | sed 's/, Label.*//')
echo "Package ID: $PACKAGE_ID"

if [ -z "$PACKAGE_ID" ]; then
  echo "ERROR: Could not determine package ID!"
  exit 1
fi

echo "=== Step 6: Approve for Org1 (sequence=$CHAINCODE_SEQUENCE) ==="
setOrg1
peer lifecycle chaincode approveformyorg \
  --channelID $CHANNEL --name $CHAINCODE_NAME \
  --version $CHAINCODE_VERSION --package-id "$PACKAGE_ID" \
  --sequence $CHAINCODE_SEQUENCE \
  --tls --cafile $ORDERER_CA --orderer $ORDERER_ADDRESS
echo "Approved Org1."

echo "=== Step 7: Approve for Org2 (sequence=$CHAINCODE_SEQUENCE) ==="
setOrg2
peer lifecycle chaincode approveformyorg \
  --channelID $CHANNEL --name $CHAINCODE_NAME \
  --version $CHAINCODE_VERSION --package-id "$PACKAGE_ID" \
  --sequence $CHAINCODE_SEQUENCE \
  --tls --cafile $ORDERER_CA --orderer $ORDERER_ADDRESS
echo "Approved Org2."

echo "=== Step 8: Check commit readiness ==="
setOrg1
peer lifecycle chaincode checkcommitreadiness \
  --channelID $CHANNEL --name $CHAINCODE_NAME \
  --version $CHAINCODE_VERSION --sequence $CHAINCODE_SEQUENCE \
  --tls --cafile $ORDERER_CA --output json

echo "=== Step 9: Commit chaincode ==="
setOrg1
peer lifecycle chaincode commit \
  --channelID $CHANNEL --name $CHAINCODE_NAME \
  --version $CHAINCODE_VERSION --sequence $CHAINCODE_SEQUENCE \
  --tls --cafile $ORDERER_CA --orderer $ORDERER_ADDRESS \
  --peerAddresses localhost:7051 \
  --tlsRootCertFiles $TEST_NETWORK/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt \
  --peerAddresses localhost:9051 \
  --tlsRootCertFiles $TEST_NETWORK/organizations/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt
echo "Committed."

echo "=== Step 10: Verify committed chaincode ==="
setOrg1
peer lifecycle chaincode querycommitted --channelID $CHANNEL

echo ""
echo "=== Step 11: Smoke test - Register product ==="
sleep 3
peer chaincode invoke \
  -C $CHANNEL -n $CHAINCODE_NAME \
  -c '{"function":"RegisterProduct","Args":["PRODUCT-SMOKE-001","MANUFACTURER-001"]}' \
  --tls --cafile $ORDERER_CA --orderer $ORDERER_ADDRESS \
  --peerAddresses localhost:7051 \
  --tlsRootCertFiles $TEST_NETWORK/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt \
  --peerAddresses localhost:9051 \
  --tlsRootCertFiles $TEST_NETWORK/organizations/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt

sleep 3

echo "=== Step 12: Smoke test - Verify product ==="
peer chaincode query \
  -C $CHANNEL -n $CHAINCODE_NAME \
  -c '{"function":"VerifyProduct","Args":["PRODUCT-SMOKE-001"]}'

echo ""
echo "=============================="
echo "  DEPLOYMENT COMPLETE!"
echo "=============================="
