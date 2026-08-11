#!/bin/bash
#
# Brings up the Fabric test-network on this host and deploys the ZeroFake
# chaincode onto it.
#
#   ./deploy/setup-fabric.sh
#
# The network runs on the host rather than inside the application's compose
# stack, because the test-network manages its own containers, network and
# crypto material. The blockchain service reaches it through host.docker.internal.
#
# Re-running this tears the network down and rebuilds it from scratch, which
# DESTROYS THE LEDGER. That is the right behaviour for a demonstration network
# and the wrong behaviour for anything else.

set -euo pipefail

GREEN=$'\033[0;32m'; YELLOW=$'\033[0;33m'; RED=$'\033[0;31m'; OFF=$'\033[0m'
step() { echo ""; echo "${GREEN}==>${OFF} $1"; }
warn() { echo "${YELLOW}[!]${OFF} $1"; }
die()  { echo "${RED}[x]${OFF} $1" >&2; exit 1; }

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
FABRIC_SAMPLES="${FABRIC_SAMPLES:-$HOME/hyperledger/fabric-samples}"
CHANNEL="${CHANNEL:-mychannel}"

[ -d "$FABRIC_SAMPLES/test-network" ] \
  || die "fabric-samples not found at $FABRIC_SAMPLES. Run deploy/bootstrap-vm.sh first."

export PATH="$PATH:$FABRIC_SAMPLES/bin:/usr/local/go/bin"

command -v docker >/dev/null || die "docker not found"
docker info >/dev/null 2>&1 || die "Cannot talk to the Docker daemon. Try: newgrp docker"
command -v go >/dev/null || die "go not found (needed to build the chaincode)"

echo "=============================================="
echo " ZeroFake Fabric setup"
echo "   fabric-samples : $FABRIC_SAMPLES"
echo "   channel        : $CHANNEL"
echo "=============================================="

if docker ps --format '{{.Names}}' | grep -q '^peer0.org1.example.com$'; then
  warn "A Fabric network is already running."
  warn "Continuing will TEAR IT DOWN and destroy the existing ledger."
  read -r -p "    Proceed? [y/N] " reply
  [[ "$reply" =~ ^[Yy]$ ]] || exit 1
fi

step "1/3  Starting the test-network with channel '$CHANNEL'"
cd "$FABRIC_SAMPLES/test-network"
./network.sh down            >/dev/null 2>&1 || true
./network.sh up createChannel -c "$CHANNEL" -ca

step "2/3  Deploying the ZeroFake chaincode"
cd "$REPO_ROOT"
FABRIC_SAMPLES="$FABRIC_SAMPLES" CHANNEL="$CHANNEL" ./deploy-chaincode.sh

step "3/3  Recording the crypto path"
CRYPTO_PATH="$FABRIC_SAMPLES/test-network/organizations/peerOrganizations/org1.example.com"
[ -d "$CRYPTO_PATH" ] || die "Crypto material not found at $CRYPTO_PATH"

cat <<EOF

==============================================
 Fabric is up and the chaincode is committed.
==============================================

Add this to your .env before starting the platform:

  FABRIC_CRYPTO_PATH=$CRYPTO_PATH

Useful commands:

  docker ps --filter name=peer0                 # is the peer running?
  cd $FABRIC_SAMPLES/test-network && ./network.sh down    # tear down

Note that the test-network binds the peer to 0.0.0.0:7051 on this host. It has
no authentication of its own beyond TLS and MSP identity, so make sure port
7051 is NOT open in your cloud security list. Only 80 and 443 should be.
EOF
