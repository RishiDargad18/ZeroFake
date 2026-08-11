#!/bin/bash
#
# Obtains a Let's Encrypt certificate and switches nginx to HTTPS.
#
#   ./deploy/setup-tls.sh zerofake.example.com you@example.com
#
# Requires:
#   - a domain whose A record already points at this machine
#   - port 80 reachable from the internet (Let's Encrypt validates over HTTP)
#   - the stack already running
#
# Without a domain, skip this. The platform works over plain HTTP, but tokens
# and passwords then travel in clear and it must not be used with real data.

set -euo pipefail

GREEN=$'\033[0;32m'; RED=$'\033[0;31m'; YELLOW=$'\033[0;33m'; OFF=$'\033[0m'
step() { echo ""; echo "${GREEN}==>${OFF} $1"; }
warn() { echo "${YELLOW}[!]${OFF} $1"; }
die()  { echo "${RED}[x]${OFF} $1" >&2; exit 1; }

DOMAIN="${1:-}"
EMAIL="${2:-}"

[ -n "$DOMAIN" ] && [ -n "$EMAIL" ] || die "Usage: $0 <domain> <email>"

DEPLOY_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$DEPLOY_DIR/.." && pwd)"
COMPOSE=(docker compose -f "$DEPLOY_DIR/docker-compose.prod.yml")

TEMPLATE="$DEPLOY_DIR/nginx/zerofake-tls.conf.template"
ACTIVE="$DEPLOY_DIR/nginx/zerofake.conf"

[ -f "$TEMPLATE" ] || die "Template not found: $TEMPLATE"

cd "$REPO_ROOT"

step "Checking that $DOMAIN resolves to this machine"
RESOLVED="$(getent hosts "$DOMAIN" | awk '{print $1}' | head -1 || true)"
PUBLIC_IP="$(curl -fsS --max-time 10 https://api.ipify.org || echo unknown)"

echo "     $DOMAIN      -> ${RESOLVED:-unresolved}"
echo "     this machine -> $PUBLIC_IP"

if [ "$RESOLVED" != "$PUBLIC_IP" ]; then
  warn "The domain does not resolve to this machine's public IP."
  warn "Validation will fail until the A record is correct and has propagated."
  read -r -p "    Continue anyway? [y/N] " reply
  [[ "$reply" =~ ^[Yy]$ ]] || exit 1
fi

step "Requesting the certificate"
"${COMPOSE[@]}" run --rm --entrypoint sh certbot -c \
  "certbot certonly --webroot -w /var/www/certbot \
     -d '$DOMAIN' --email '$EMAIL' \
     --agree-tos --no-eff-email --non-interactive" \
  || die "Certificate request failed. Is port 80 reachable from the internet?"

step "Switching nginx to HTTPS"
[ -f "$ACTIVE" ] && cp "$ACTIVE" "$ACTIVE.http.bak"
sed "s|@DOMAIN@|$DOMAIN|g" "$TEMPLATE" > "$ACTIVE"
echo "     rendered $ACTIVE for $DOMAIN"
echo "     previous configuration saved as $ACTIVE.http.bak"

step "Validating the configuration before reloading"
if ! "${COMPOSE[@]}" exec -T nginx nginx -t 2>&1 | tail -2; then
  warn "nginx rejected the configuration. Restoring the HTTP version."
  mv "$ACTIVE.http.bak" "$ACTIVE"
  "${COMPOSE[@]}" restart nginx
  die "TLS setup aborted; the site is still served over HTTP."
fi

"${COMPOSE[@]}" restart nginx
sleep 4

if curl -fsS --max-time 15 "https://$DOMAIN/healthz" >/dev/null 2>&1; then
  cat <<EOF

==============================================
 HTTPS is live: https://$DOMAIN
==============================================

One step remains. The client was built for the old origin, and the services
still allow it in CORS. Update .env:

  PUBLIC_URL=https://$DOMAIN

then rebuild so the client and the CORS configuration agree:

  docker compose -f deploy/docker-compose.prod.yml up -d --build

Renewal runs automatically in the certbot container every 12 hours.
EOF
else
  warn "HTTPS did not answer. Inspect with:"
  warn "  ${COMPOSE[*]} logs nginx"
  warn "To roll back:  mv $ACTIVE.http.bak $ACTIVE && ${COMPOSE[*]} restart nginx"
fi
