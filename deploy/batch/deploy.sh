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
services=(location-service bo-portal-service notification-service brand-service file-service agri-catalog-service post-service rice-disease-diagnosis-service profile-service auth-service api-gateway)
all_services=("${services[@]}")
mapfile -t services < "$HERE/selected-services.txt"
[[ ${#services[@]} -gt 0 ]] || { echo 'NO_BACKEND_CHANGES'; exit 0; }
"${dc[@]}" pull "${services[@]}"
"${dc[@]}" exec -T postgres pg_isready -U postgres -d postgres
python3 "$HERE/release.py" begin --kind backend --state /opt/nongthinh/state/backend.json --plan "$HERE/release-plan.json"

start_and_check() {
    local service="$1" cid state restarts started ready=0
    "${dc[@]}" up -d --no-deps --force-recreate "$service"
    cid="$("${dc[@]}" ps -aq "$service")"
    for ((attempt=0; attempt<90; attempt++)); do
        state="$(docker inspect -f '{{.State.Status}}' "$cid")"
        restarts="$(docker inspect -f '{{.RestartCount}}' "$cid")"
        if [[ "$state" == exited || "$state" == dead || "$state" == restarting || "$restarts" -gt 0 ]]; then break; fi
        started="$(docker inspect -f '{{.State.StartedAt}}' "$cid")"
        if [[ "$state" == running ]] && docker logs --since="$started" "$cid" 2>&1 | grep -E 'Started [A-Za-z0-9_.$]*Application in' >/dev/null; then
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

# Only selected services are recreated; other image tags and containers are preserved.
for service in "${services[@]}"; do start_and_check "$service"; done

# Check actual HTTP responses, not just startup log messages.
check_http() {
    local url="$1" expected="$2" code
    for ((attempt=0; attempt<12; attempt++)); do
        code=$(curl --silent --output /dev/null --connect-timeout 3 --max-time 10 \
            --write-out '%{http_code}' "$url") || code=000
        if [[ "$code" == "$expected" ]]; then echo "HTTP_OK: $url ($code)"; return; fi
        sleep 3
    done
    echo "HTTP_FAILED: $url expected=$expected actual=$code"
    return 1
}
check_http http://127.0.0.1:9090/auth/oauth2/authorization/keycloak 302
check_http http://127.0.0.1:9092/profile/farmer-profiles/me 401
check_http http://127.0.0.1:8888/api/v1/profile/farmer-profiles/me 401
for service in "${all_services[@]}"; do
    cid="$("${dc[@]}" ps -aq "$service")"
    status="$(docker inspect -f '{{.State.Status}}:{{.RestartCount}}:{{.State.OOMKilled}}' "$cid")"
    if [[ "$status" != running:0:false ]]; then
        echo "UNSTABLE_CONTAINER: $service $status"; exit 1
    fi
done
"${dc[@]}" ps
python3 "$HERE/release.py" finish --kind backend --state /opt/nongthinh/state/backend.json --plan "$HERE/release-plan.json"
echo 'BATCH_STARTUP_OK: all 11 backends running; Auth/Profile/Gateway HTTP checks passed.'
echo 'Model, SMTP, Kafka flows and public HTTPS still need functional verification.'
echo 'Review: curl -i http://127.0.0.1:8888/api/v1/profile/farmer-profiles/me'
echo "Compose overlay: $HERE/compose.remaining.yml"
