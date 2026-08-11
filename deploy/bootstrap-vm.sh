#!/bin/bash
#
# Prepares a fresh Ubuntu 22.04/24.04 VM to run ZeroFake.
#
#   curl -fsSL https://raw.githubusercontent.com/RishiDargad18/ZeroFake/main/deploy/bootstrap-vm.sh | bash
#
# or, from a clone:
#
#   ./deploy/bootstrap-vm.sh
#
# Installs Docker, the Compose plugin, Go and the Fabric binaries, and opens
# the firewall for HTTP and HTTPS. Safe to re-run: every step checks first.
#
# Tested on Oracle Cloud Ampere (arm64) and x86_64. Needs 8 GB RAM to run the
# Fabric network and four JVMs comfortably.

set -euo pipefail

GREEN=$'\033[0;32m'; YELLOW=$'\033[0;33m'; RED=$'\033[0;31m'; OFF=$'\033[0m'
step() { echo ""; echo "${GREEN}==>${OFF} $1"; }
warn() { echo "${YELLOW}[!]${OFF} $1"; }
die()  { echo "${RED}[x]${OFF} $1" >&2; exit 1; }

[ "$(id -u)" -eq 0 ] && die "Run as a normal user with sudo, not as root."

ARCH="$(uname -m)"
case "$ARCH" in
  x86_64)  FABRIC_ARCH="linux-amd64" ;;
  aarch64) FABRIC_ARCH="linux-arm64" ;;
  *)       die "Unsupported architecture: $ARCH" ;;
esac

FABRIC_VERSION="${FABRIC_VERSION:-2.5.9}"
FABRIC_CA_VERSION="${FABRIC_CA_VERSION:-1.5.12}"
GO_VERSION="${GO_VERSION:-1.22.5}"
INSTALL_DIR="${INSTALL_DIR:-$HOME/hyperledger}"

echo "=============================================="
echo " ZeroFake VM bootstrap"
echo "   architecture : $ARCH ($FABRIC_ARCH)"
echo "   fabric       : $FABRIC_VERSION"
echo "=============================================="

# --- sanity ----------------------------------------------------------------

TOTAL_MB=$(free -m | awk '/^Mem:/{print $2}')
if [ "$TOTAL_MB" -lt 7000 ]; then
  warn "This machine has ${TOTAL_MB} MB of RAM."
  warn "Fabric plus four JVMs needs about 8 GB. Expect the OOM killer."
  read -r -p "    Continue anyway? [y/N] " reply
  [[ "$reply" =~ ^[Yy]$ ]] || exit 1
fi

step "1/6  System packages"
sudo apt-get update -qq
sudo apt-get install -y -qq ca-certificates curl gnupg git jq unzip ufw

step "2/6  Docker Engine and the Compose plugin"
if command -v docker >/dev/null 2>&1; then
  echo "     already installed: $(docker --version)"
else
  sudo install -m 0755 -d /etc/apt/keyrings
  curl -fsSL https://download.docker.com/linux/ubuntu/gpg \
    | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
  sudo chmod a+r /etc/apt/keyrings/docker.gpg

  echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" \
    | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

  sudo apt-get update -qq
  sudo apt-get install -y -qq docker-ce docker-ce-cli containerd.io \
                              docker-buildx-plugin docker-compose-plugin
  sudo usermod -aG docker "$USER"
  warn "You were added to the docker group. Log out and back in, or run:  newgrp docker"
fi

step "3/6  Go (required to build the chaincode)"
if command -v go >/dev/null 2>&1 && go version | grep -q "go1.2"; then
  echo "     already installed: $(go version)"
else
  GO_TARBALL="go${GO_VERSION}.${FABRIC_ARCH/linux-/linux-}.tar.gz"
  curl -fsSLo /tmp/go.tar.gz "https://go.dev/dl/go${GO_VERSION}.linux-$(dpkg --print-architecture).tar.gz"
  sudo rm -rf /usr/local/go
  sudo tar -C /usr/local -xzf /tmp/go.tar.gz
  rm -f /tmp/go.tar.gz
  grep -q '/usr/local/go/bin' "$HOME/.profile" 2>/dev/null \
    || echo 'export PATH=$PATH:/usr/local/go/bin' >> "$HOME/.profile"
  export PATH=$PATH:/usr/local/go/bin
  echo "     installed: $(go version)"
fi

step "4/6  Hyperledger Fabric binaries and samples"
mkdir -p "$INSTALL_DIR"
if [ -d "$INSTALL_DIR/fabric-samples/test-network" ]; then
  echo "     already present at $INSTALL_DIR/fabric-samples"
else
  cd "$INSTALL_DIR"
  curl -fsSLo install-fabric.sh \
    https://raw.githubusercontent.com/hyperledger/fabric/main/scripts/install-fabric.sh
  chmod +x install-fabric.sh
  # Pulls the docker images, the CLI binaries and fabric-samples.
  ./install-fabric.sh --fabric-version "$FABRIC_VERSION" \
                      --ca-version "$FABRIC_CA_VERSION" docker samples binary
fi

grep -q 'fabric-samples/bin' "$HOME/.profile" 2>/dev/null \
  || echo "export PATH=\$PATH:$INSTALL_DIR/fabric-samples/bin" >> "$HOME/.profile"

step "5/6  Firewall"
# Oracle Cloud images ship with restrictive iptables rules in addition to ufw;
# both need opening, and the cloud-side security list must allow 80/443 too.
sudo ufw allow OpenSSH               >/dev/null 2>&1 || true
sudo ufw allow 80/tcp                >/dev/null 2>&1 || true
sudo ufw allow 443/tcp               >/dev/null 2>&1 || true
sudo ufw --force enable              >/dev/null 2>&1 || true
echo "     ufw: $(sudo ufw status | head -1)"

if sudo iptables -L INPUT -n 2>/dev/null | grep -q REJECT; then
  warn "iptables has REJECT rules (common on Oracle Cloud images). Opening 80/443:"
  sudo iptables -I INPUT 1 -p tcp --dport 80  -j ACCEPT
  sudo iptables -I INPUT 1 -p tcp --dport 443 -j ACCEPT
  sudo netfilter-persistent save >/dev/null 2>&1 \
    || warn "Could not persist iptables rules; they will not survive a reboot."
fi

step "6/6  Swap (protects against build-time memory spikes)"
if swapon --show | grep -q .; then
  echo "     swap already configured"
else
  sudo fallocate -l 4G /swapfile
  sudo chmod 600 /swapfile
  sudo mkswap /swapfile >/dev/null
  sudo swapon /swapfile
  grep -q '/swapfile' /etc/fstab || echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab >/dev/null
  echo "     4 GB swap enabled"
fi

cat <<EOF

==============================================
 Bootstrap complete.
==============================================

Next:

  1. Log out and back in (so docker group membership applies), then:

       git clone https://github.com/RishiDargad18/ZeroFake.git
       cd ZeroFake

  2. Start Fabric and deploy the chaincode:

       ./deploy/setup-fabric.sh

  3. Configure and launch the platform:

       cp deploy/.env.prod.example .env
       nano .env                       # set JWT_SECRET, passwords, PUBLIC_URL
       docker compose -f deploy/docker-compose.prod.yml up -d --build

  4. If you have a domain pointed at this machine:

       ./deploy/setup-tls.sh your-domain.com you@example.com

Remember to open ports 80 and 443 in your cloud provider's security list as
well as the host firewall. On Oracle Cloud that is the VCN security list; the
host firewall alone is not enough.
EOF
