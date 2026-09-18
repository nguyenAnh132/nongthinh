#!/usr/bin/env bash
set -Eeuo pipefail
HERE="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
cd /opt/nongthinh
exec 9>/opt/nongthinh/batch-deploy.lock
flock -n 9 || { echo 'Another batch deployment is running'; exit 1; }
test -f "$HERE/images.env" || { echo 'Use the bundle archived by Jenkins (images.env missing)'; exit 1; }
test -d env/batch || { echo 'Run python3 prepare.py first'; exit 1; }
dc=(docker compose --env-file /opt/nongthinh/.env --env-file "$HERE/images.env"
    -f /opt/nongthinh/compose.prod.yml -f "$HERE/compose.remaining.yml")
"${dc[@]}" config --quiet
services=(location-service bo-portal-service notification-service brand-service file-service agri-catalog-service post-service rice-disease-diagnosis-service api-gateway)
"${dc[@]}" pull "${services[@]}"
"${dc[@]}" exec -T postgres pg_isready -U postgres -d postgres

start_and_check() {
    local service="$1" cid state restarts ready=0
    "${dc[@]}" up -d --no-deps "$service"
    cid="$("${dc[@]}" ps -aq "$service")"
    for ((attempt=0; attempt<90; attempt++)); do
        state="$(docker inspect -f '{{.State.Status}}' "$cid")"
        restarts="$(docker inspect -f '{{.RestartCount}}' "$cid")"
        if [[ "$state" == exited || "$state" == dead || "$state" == restarting || "$restarts" -gt 0 ]]; then break; fi
        if [[ "$state" == running ]] && docker logs --tail=500 "$cid" 2>&1 | grep -E 'Started [A-Za-z0-9_.$]*Application in' >/dev/null; then
            ready=1; break
        fi
        sleep 3
    done
    if [[ "$ready" != 1 ]]; then
        echo "STARTUP_FAILED: $service. Previously started services remain running."
        "${dc[@]}" logs --tail=100 "$service"
        exit 1
    fi
    echo "STARTUP_OK: $service"
}

# Start sequentially to limit peak memory/CPU; fixes to internal URLs recreate auth/profile.
for service in "${services[@]}"; do start_and_check "$service"; done
start_and_check auth-service
start_and_check profile-service
"${dc[@]}" ps
echo 'BATCH_STARTUP_OK: application startup only; model, SMTP, Kafka flows and HTTPS still need functional verification.'
echo 'Review: curl -i http://127.0.0.1:8888/api/v1/profile/farmer-profiles/me'
echo "Compose overlay: $HERE/compose.remaining.yml"
