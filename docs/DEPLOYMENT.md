# Deploying ZeroFake to a single VM

This is the deployment ZeroFake is designed for: **one machine running everything** — the Hyperledger Fabric network, PostgreSQL, all four services, the web client and an nginx reverse proxy.

Splitting it across managed services sounds tidier but is not practical, because Fabric needs to run somewhere and no mainstream PaaS will host a peer. One VM keeps the whole system in one place, which is also the easiest thing to demonstrate.

---

## What you need

| | Minimum | Comfortable |
|---|---|---|
| RAM | 8 GB | 16 GB |
| vCPU | 2 | 4 |
| Disk | 40 GB | 60 GB |
| OS | Ubuntu 22.04 or 24.04 | |

Four JVMs need roughly 3.5 GB between them, the Fabric test-network another 2–3 GB, PostgreSQL and nginx the rest. **Below 8 GB the OOM killer will start reaping containers**, usually mid-build, and the failure looks like a random crash rather than a memory problem.

**Oracle Cloud's Always Free tier** is the only genuinely free option large enough: an Ampere A1 instance with up to 4 OCPUs and 24 GB RAM. It is arm64, which the scripts handle. AWS and GCP free tiers cap at 1 GB, which is not enough for even one service.

You provision the VM yourself — creating the cloud account needs identity and payment details.

### Firewall

Open **80** and **443** only, in two places:

1. Your cloud provider's security list or security group.
2. The host firewall (`bootstrap-vm.sh` handles this).

**Do not open 7051.** The Fabric peer listens there, and it is reachable from the application containers over the Docker host gateway without being exposed to the internet.

---

## Deployment

### 1. Prepare the machine

```bash
git clone https://github.com/RishiDargad18/ZeroFake.git
cd ZeroFake
./deploy/bootstrap-vm.sh
```

Installs Docker and the Compose plugin, Go, the Fabric binaries and `fabric-samples`, opens the firewall, and adds 4 GB of swap. Safe to re-run.

Log out and back in afterwards so your Docker group membership applies, or run `newgrp docker`.

### 2. Start Fabric and deploy the chaincode

```bash
./deploy/setup-fabric.sh
```

Brings up the test-network with channel `mychannel`, then packages, installs, approves and commits the ZeroFake chaincode and runs a smoke test against it. It prints the crypto path you need next.

Re-running this **destroys the ledger** and rebuilds it. That is correct for a demonstration network and wrong for anything else.

### 3. Configure

```bash
cp deploy/.env.prod.example .env
nano .env
```

Six values have no defaults, and the stack refuses to start without them:

| Variable | How to set it |
|---|---|
| `PUBLIC_URL` | `https://your-domain` or `http://your-ip`, no trailing slash |
| `JWT_SECRET` | `openssl rand -base64 64 \| tr -d '[:space:]'` |
| `DB_USERNAME` / `DB_PASSWORD` | Anything; the port is not published |
| `SEED_ENABLED` | `true` for a demo, `false` for real data |
| `SEED_DEFAULT_PASSWORD` | **Not** the one from `.env.example` — that is published in this repository |
| `FABRIC_CRYPTO_PATH` | Printed by `setup-fabric.sh` |

### 4. Launch

```bash
docker compose -f deploy/docker-compose.prod.yml up -d --build
```

First build takes 10–20 minutes: four Maven builds from scratch.

```bash
curl http://localhost/healthz
./scripts/smoke-test.sh AUTH_URL=http://localhost/auth ...   # see below
```

### 5. TLS, if you have a domain

Point an A record at the machine, wait for it to propagate, then:

```bash
./deploy/setup-tls.sh your-domain.com you@example.com
```

Obtains a Let's Encrypt certificate, swaps nginx to the HTTPS configuration, and validates it before reloading — rolling back automatically if nginx rejects it. Renewal runs every 12 hours in the certbot container.

Afterwards set `PUBLIC_URL=https://your-domain.com` in `.env` and rebuild, so the client and the CORS configuration agree with the new origin.

**Without a domain the platform runs over plain HTTP.** Tokens and passwords travel in clear. Fine for a demo, unacceptable for real data.

---

## How the pieces fit

```
                    internet
                       |
                   :80 / :443
                       |
              +--------v---------+
              |      nginx       |   the only exposed port
              +--------+---------+
                       |
   /  ->  frontend     |     /auth/        ->  auth-service:8081
                       |     /products/    ->  product-service:8082
                       |     /blockchain/  ->  blockchain-service:8083
                       |     /fraud/       ->  fraud-detection-service:8085
                       |
              +--------v---------+          +------------------+
              |    PostgreSQL    |          |  Fabric network  |
              |  (not published) |          |   on the host    |
              +------------------+          +------------------+
```

Everything is served from **one origin**. The browser never talks to a service port, which means one port is exposed instead of five, there are no cross-origin requests at all, and TLS terminates in exactly one place.

The frontend is built with `VITE_AUTH_API=/auth` and friends — relative paths, not absolute URLs — so it inherits whatever origin it is served from and needs no rebuild when the domain changes. Only CORS does.

---

## Differences from the development stack

| | `docker-compose.yml` | `deploy/docker-compose.prod.yml` |
|---|---|---|
| Exposed ports | 3000, 8081–8085, 5432 | 80 and 443 only |
| Frontend API URLs | `http://localhost:808x` | `/auth`, `/products`, … |
| Restart policy | none | `unless-stopped` |
| Memory limits | none | per-container, leaving room for Fabric |
| Rate limiting | none | 20 logins/min, 30 req/s per IP |
| Seeding | defaults to on | must be set explicitly |
| TLS | none | Let's Encrypt with auto-renewal |

---

## Operating it

```bash
# where things stand
docker compose -f deploy/docker-compose.prod.yml ps

# logs
docker compose -f deploy/docker-compose.prod.yml logs -f auth-service

# deploy a new version
git pull && docker compose -f deploy/docker-compose.prod.yml up -d --build

# database shell
docker compose -f deploy/docker-compose.prod.yml exec postgres psql -U zerofake zerofake_product

# is Fabric alive?
docker ps --filter name=peer0
```

### Backups

The state worth keeping is the Postgres volume and the Fabric ledger.

```bash
docker run --rm -v zerofake_postgres-data:/data -v "$PWD":/backup alpine \
  tar czf /backup/postgres-$(date +%F).tar.gz -C /data .
```

The ledger lives in the peer's own volumes, created by the test-network. For a demonstration deployment it is usually simpler to accept that tearing down Fabric loses it.

---

## Troubleshooting

**Services restart-loop with `Unable to determine Dialect without JDBC metadata`.**
That message is misleading — the real cause is a database connection failure, and it is almost always this: **PostgreSQL only honours `POSTGRES_USER` and `POSTGRES_PASSWORD` when the data directory is empty.** If you change `DB_USERNAME` or `DB_PASSWORD` after the first start, the volume keeps the old role and the new one never exists. Either create the role by hand, or remove the volume and let it re-initialise:

```bash
docker compose -f deploy/docker-compose.prod.yml down
docker volume rm zerofake_postgres-data      # destroys all data
```

**`nginx: connect() failed (111: Connection refused)`.**
A service behind the proxy has not finished starting, or is crash-looping. Check `docker compose ps` and read that service's logs.

**Blockchain endpoints return 502.**
Fabric is unreachable. Check `docker ps --filter name=peer0`; if the peer is gone, re-run `./deploy/setup-fabric.sh`. A 502 here is correct behaviour — the platform reports "authenticity cannot be confirmed" rather than pretending a product is counterfeit.

**429 on login.**
The rate limiter is doing its job: 20 logins per minute per IP, with a burst of 10. Adjust `login_zone` in `deploy/nginx/zerofake.conf` if a demo audience shares one address.

**The build is killed part-way through.**
Out of memory. Confirm swap exists (`swapon --show`) and consider building one service at a time:

```bash
docker compose -f deploy/docker-compose.prod.yml build auth-service
```

**Certbot fails to validate.**
Port 80 must be reachable from the internet, in the cloud security list *and* the host firewall, and the A record must already resolve to the machine. `curl http://your-domain/.well-known/acme-challenge/test` from elsewhere should reach nginx rather than time out.

---

## Before you call it public

- [ ] `SEED_DEFAULT_PASSWORD` is not the one from `.env.example`
- [ ] `JWT_SECRET` is freshly generated, not the development default
- [ ] Only 80 and 443 are open, in both the cloud security list and the host firewall
- [ ] TLS is configured, or you accept that credentials travel in clear
- [ ] `SEED_ENABLED=false` if the deployment holds anything real
- [ ] You know `docker compose ... logs` is where to look when something breaks

A public deployment with the documented demo password hands anyone an administrator account. That is the single most likely way to get this wrong.
