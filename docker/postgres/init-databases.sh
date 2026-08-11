#!/bin/bash
# Creates one database per service.
#
# Each ZeroFake service owns its own schema and never reads another service's
# tables, so they are kept in separate databases even though a single
# PostgreSQL instance hosts them all in local development.

set -e

for db in zerofake_auth zerofake_product zerofake_blockchain zerofake_fraud; do
  echo "Creating database: $db"
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres <<-EOSQL
    SELECT 'CREATE DATABASE $db'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$db')\gexec
EOSQL
done

echo "ZeroFake databases ready."
