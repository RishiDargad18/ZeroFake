#!/bin/bash
#
# ZeroFake chaincode deployment.
#
# Packages, installs, approves and commits the ZeroFake chaincode on a running
# Hyperledger Fabric test-network, then runs a smoke test.
#
# Usage:
#   ./deploy-chaincode.sh
#
# Configuration (override by exporting, or via a .env file in the repo root):
#   FABRIC_SAMPLES      path to your fabric-samples checkout
#   CHAINCODE_NAME      chaincode name on the channel        (default: zerofake)
#   CHAINCODE_VERSION   chaincode version                    (default: 1.0)
#   CHAINCODE_SEQUENCE  lifecycle sequence                   (default: 1)
#   CHANNEL             channel name                         (default: mychannel)
#
# Bump CHAINCODE_VERSION *and* CHAINCODE_SEQUENCE when redeploying a change to
# an already-committed chaincode:
#   CHAINCODE_VERSION=1.1 CHAINCODE_SEQUENCE=2 ./deploy-chaincode.sh

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Load .env from the repo root if present, without clobbering existing exports.
if [ -f "$SCRIPT_DIR/.env" ]; then
  set -a
  # shellcheck disable=SC1091
  . "$SCRIPT_DIR/.env"
  set +a
fi

FABRIC_SAMPLES="${FABRIC_SAMPLES:-$HOME/hyperledger/fabric-samples}"
CHAINCODE_NAME="${CHAINCODE_NAME:-zerofake}"
CHAINCODE_VERSION="${CHAINCODE_VERSION:-1.0}"
CHAINCODE_SEQUENCE="${CHAINCODE_SEQUENCE:-1}"
CHANNEL="${CHANNEL:-mychannel}"

CHAINCODE_SRC="$SCRIPT_DIR/services/zerofake-chaincode"
TEST_NETWORK="$FABRIC_SAMPLES/test-network"

# --- preflight -------------------------------------------------------------

if [ ! -d "$TEST_NETWORK" ]; then
  echo "ERROR: Fabric test-network not found at: $TEST_NETWORK" >&2
  echo "       Set FABRIC_SAMPLES to your fabric-samples checkout." >&2
  exit 1
fi

if [ ! -d "$CHAINCODE_SRC" ]; then
  echo "ERROR: chaincode source not found at: $CHAINCODE_SRC" >&2
  exit 1
fi

for tool in peer go; do
  if ! command -v "$tool" >/dev/null 2>&1 \
     && ! [ -x "$FABRIC_SAMPLES/bin/$tool" ]; then
    echo "ERROR: '$tool' not found on PATH or in $FABRIC_SAMPLES/bin" >&2
    exit 1
  fi
done

export PATH="$PATH:$FABRIC_SAMPLES/bin"
export FABRIC_CFG_PATH="$FABRIC_SAMPLES/config"

ORDERER_CA="$TEST_NETWORK/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem"
ORDERER_ADDRESS="${ORDERER_ADDRESS:-localhost:7050}"

ORG1_TLS="$TEST_NETWORK/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt"
ORG2_TLS="$TEST_NETWORK/organizations/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt"

setOrg1() {
  export CORE_PEER_TLS_ENABLED=true
  export CORE_PEER_LOCALMSPID=Org1MSP
  export CORE_PEER_TLS_ROOTCERT_FILE="$ORG1_TLS"
  export CORE_PEER_MSPCONFIGPATH="$TEST_NETWORK/organizations/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp"
  export CORE_PEER_ADDRESS=localhost:7051
}

setOrg2() {
  export CORE_PEER_TLS_ENABLED=true
  export CORE_PEER_LOCALMSPID=Org2MSP
  export CORE_PEER_TLS_ROOTCERT_FILE="$ORG2_TLS"
  export CORE_PEER_MSPCONFIGPATH="$TEST_NETWORK/organizations/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp"
  export CORE_PEER_ADDRESS=localhost:9051
}

echo "=============================================="
echo " ZeroFake chaincode deployment"
echo "   fabric-samples : $FABRIC_SAMPLES"
echo "   chaincode      : $CHAINCODE_NAME v$CHAINCODE_VERSION (sequence $CHAINCODE_SEQUENCE)"
echo "   channel        : $CHANNEL"
echo "=============================================="

# --- 1. stage the source ---------------------------------------------------
# Copied to a temporary directory so that `go mod vendor` does not write into
# the repository, and so the path is free of characters the packager dislikes.

BUILD_DIR="$(mktemp -d)"
trap 'rm -rf "$BUILD_DIR"' EXIT

echo ""
echo "--- 1/8 Staging chaincode source"
cp -r "$CHAINCODE_SRC/." "$BUILD_DIR/"

echo "--- 2/8 Vendoring Go dependencies"
(cd "$BUILD_DIR" && go mod vendor)

# --- 2. package ------------------------------------------------------------

PACKAGE="$BUILD_DIR/${CHAINCODE_NAME}.tar.gz"
LABEL="${CHAINCODE_NAME}_${CHAINCODE_VERSION}"

echo "--- 3/8 Packaging chaincode"
peer lifecycle chaincode package "$PACKAGE" \
  --path "$BUILD_DIR" \
  --lang golang \
  --label "$LABEL"

# --- 3. install ------------------------------------------------------------

echo "--- 4/8 Installing on Org1"
setOrg1
peer lifecycle chaincode install "$PACKAGE" || echo "    (already installed)"

echo "--- 5/8 Installing on Org2"
setOrg2
peer lifecycle chaincode install "$PACKAGE" || echo "    (already installed)"

setOrg1
PACKAGE_ID="$(peer lifecycle chaincode queryinstalled 2>&1 \
  | grep "Package ID:" \
  | grep "$LABEL" \
  | head -1 \
  | sed 's/.*Package ID: //' \
  | sed 's/, Label.*//')"

if [ -z "$PACKAGE_ID" ]; then
  echo "ERROR: could not determine the package ID for label '$LABEL'." >&2
  exit 1
fi

echo "    Package ID: $PACKAGE_ID"

# --- 4. approve and commit -------------------------------------------------

approve() {
  peer lifecycle chaincode approveformyorg \
    --channelID "$CHANNEL" --name "$CHAINCODE_NAME" \
    --version "$CHAINCODE_VERSION" --package-id "$PACKAGE_ID" \
    --sequence "$CHAINCODE_SEQUENCE" \
    --tls --cafile "$ORDERER_CA" --orderer "$ORDERER_ADDRESS"
}

echo "--- 6/8 Approving for Org1 and Org2"
setOrg1 && approve
setOrg2 && approve

setOrg1
peer lifecycle chaincode checkcommitreadiness \
  --channelID "$CHANNEL" --name "$CHAINCODE_NAME" \
  --version "$CHAINCODE_VERSION" --sequence "$CHAINCODE_SEQUENCE" \
  --tls --cafile "$ORDERER_CA" --output json

echo "--- 7/8 Committing chaincode"
peer lifecycle chaincode commit \
  --channelID "$CHANNEL" --name "$CHAINCODE_NAME" \
  --version "$CHAINCODE_VERSION" --sequence "$CHAINCODE_SEQUENCE" \
  --tls --cafile "$ORDERER_CA" --orderer "$ORDERER_ADDRESS" \
  --peerAddresses localhost:7051 --tlsRootCertFiles "$ORG1_TLS" \
  --peerAddresses localhost:9051 --tlsRootCertFiles "$ORG2_TLS"

peer lifecycle chaincode querycommitted --channelID "$CHANNEL" --name "$CHAINCODE_NAME"

# --- 5. smoke test ---------------------------------------------------------

SMOKE_PRODUCT="smoke-$(date +%s)"

echo "--- 8/8 Smoke test with product '$SMOKE_PRODUCT'"
sleep 3

peer chaincode invoke \
  -C "$CHANNEL" -n "$CHAINCODE_NAME" \
  -c "{\"function\":\"RegisterProduct\",\"Args\":[\"$SMOKE_PRODUCT\",\"manufacturer-smoke\"]}" \
  --tls --cafile "$ORDERER_CA" --orderer "$ORDERER_ADDRESS" \
  --peerAddresses localhost:7051 --tlsRootCertFiles "$ORG1_TLS" \
  --peerAddresses localhost:9051 --tlsRootCertFiles "$ORG2_TLS"

sleep 3

peer chaincode query \
  -C "$CHANNEL" -n "$CHAINCODE_NAME" \
  -c "{\"function\":\"VerifyProduct\",\"Args\":[\"$SMOKE_PRODUCT\"]}"

echo ""
echo "=============================================="
echo " Deployment complete."
echo "=============================================="
