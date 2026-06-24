#!/bin/sh
set -eu

BASE_URL="${BASE_URL:-http://order-api:8080}"
SLEEP_SECONDS="${SLEEP_SECONDS:-5}"

echo "Generating checkout traffic against ${BASE_URL}"

COUNT=0
while true; do
  COUNT=$((COUNT + 1))
  curl -fsS "${BASE_URL}/api/catalog" >/dev/null || true
  curl -fsS -X POST "${BASE_URL}/api/checkout" \
    -H "Content-Type: application/json" \
    -d "{\"customerId\":\"loadgen-${COUNT}\",\"sku\":\"coffee-beans\",\"quantity\":1}" >/dev/null || true

  if [ $((COUNT % 7)) -eq 0 ]; then
    curl -fsS "${BASE_URL}/api/simulate/slow?delayMs=1500" >/dev/null || true
  fi

  if [ $((COUNT % 11)) -eq 0 ]; then
    curl -fsS "${BASE_URL}/api/simulate/error" >/dev/null || true
  fi

  sleep "${SLEEP_SECONDS}"
done
