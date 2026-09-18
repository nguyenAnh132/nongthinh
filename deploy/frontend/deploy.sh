#!/usr/bin/env bash
set -Eeuo pipefail
HERE="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
exec 9>/opt/nongthinh/frontend-deploy.lock
flock -n 9 || { echo 'Another frontend deployment is running'; exit 1; }
dc=(docker compose --env-file "$HERE/images.env" -f "$HERE/compose.yml")
"${dc[@]}" config --quiet
"${dc[@]}" pull frontend
python3 "$HERE/release.py" begin --kind frontend --state /opt/nongthinh/state/frontend.json --plan "$HERE/release-plan.json"
"${dc[@]}" up -d --wait --wait-timeout 90 frontend
curl --fail --silent --show-error --max-time 10 http://127.0.0.1:8081/healthz
curl --fail --silent --show-error --max-time 10 http://127.0.0.1:8081/ | grep '<app-root' >/dev/null
python3 "$HERE/release.py" finish --kind frontend --state /opt/nongthinh/state/frontend.json --plan "$HERE/release-plan.json"
echo 'FRONTEND_DEPLOY_OK: localhost:8081; public HTTPS is configured separately.'
