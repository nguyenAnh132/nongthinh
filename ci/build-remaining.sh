#!/usr/bin/env bash
set -Eeuo pipefail
set +x
: "${IMAGE_TAG:?Missing IMAGE_TAG}"
: "${DOCKERHUB_USER:?Missing Docker Hub username}"
: "${DOCKERHUB_TOKEN:?Missing Docker Hub token}"
[[ "$IMAGE_TAG" =~ ^batch-[0-9]+-[a-f0-9]{12}$ ]] || exit 2
ROOT="$(pwd)"
TASK_TMP="$(mktemp -d)"
export DOCKER_CONFIG="$TASK_TMP/docker"
CHECK_CONTAINER=""
cleanup() {
    if [[ -n "$CHECK_CONTAINER" ]]; then docker rm "$CHECK_CONTAINER" >/dev/null 2>&1 || true; fi
    # Only remove the exact temporary directory created by mktemp above.
    case "$TASK_TMP" in /tmp/tmp.*) rm -rf -- "$TASK_TMP" ;; esac
}
trap cleanup EXIT
mkdir -p "$DOCKER_CONFIG" "$TASK_TMP/source" "$TASK_TMP/m2" "$TASK_TMP/bundle"
printf '%s' "$DOCKERHUB_TOKEN" | docker login --username "$DOCKERHUB_USER" --password-stdin

# Clean tracked inputs only: no stale workspace files or ignored local secrets.
git archive HEAD services | tar -x -C "$TASK_TMP/source"
services=(location-service bo-portal-service notification-service brand-service file-service agri-catalog-service post-service rice-disease-diagnosis-service)
mkdir -p "$ROOT/batch-test-reports/$IMAGE_TAG"
for service in "${services[@]}"; do
    context="$TASK_TMP/source/services/$service"
    printf '\nsrc/main/resources/application-dev.*\n' >> "$context/.dockerignore"
    if [[ "${RUN_TESTS:-false}" == true ]]; then
        echo "TEST: $service"
        status=0
        docker run --rm --user "$(id -u):$(id -g)" \
            --env MAVEN_CONFIG=/tmp/maven \
            --env MAVEN_OPTS=-Duser.home=/tmp \
            --mount "type=bind,source=$context,target=/source" \
            --mount "type=bind,source=$TASK_TMP/m2,target=/m2" \
            --workdir /source maven:3.9-eclipse-temurin-21 \
            mvn -B -ntp -Dmaven.repo.local=/m2 -Duser.timezone=UTC test || status=$?
        mkdir -p "$ROOT/batch-test-reports/$IMAGE_TAG/$service"
        if [[ -d "$context/target/surefire-reports" ]]; then
            cp "$context"/target/surefire-reports/*.xml "$ROOT/batch-test-reports/$IMAGE_TAG/$service/" 2>/dev/null || true
        fi
        if [[ $status -ne 0 ]]; then echo "TEST_FAILED: $service"; exit "$status"; fi
    fi
    echo "BUILD: $service"
    image="nguyenanh132/nongthinh-$service:$IMAGE_TAG"
    docker build --pull --progress=plain --tag "$image" "$context"
    CHECK_CONTAINER="$(docker create "$image")"
    docker cp "$CHECK_CONTAINER:/app/app.jar" "$TASK_TMP/app.jar"
    docker run --rm --network none \
        --mount "type=bind,source=$TASK_TMP,target=/verify,readonly" \
        --entrypoint jar maven:3.9-eclipse-temurin-21 \
        tf /verify/app.jar > "$TASK_TMP/entries.txt"
    if grep -E '(^|/)application-dev[^/]*\.(yml|yaml|properties)$' "$TASK_TMP/entries.txt"; then
        echo "ERROR: Dev config in $service image"; exit 1
    fi
    grep -Eq '^BOOT-INF/classes/application-prod\.ya?ml$' "$TASK_TMP/entries.txt"
    docker rm "$CHECK_CONTAINER" >/dev/null
    CHECK_CONTAINER=""
done

# Push only after every selected service has built and passed its enabled checks.
for service in "${services[@]}"; do
    docker push "nguyenanh132/nongthinh-$service:$IMAGE_TAG"
done
cp deploy/batch/{services.json,compose.remaining.yml,prepare.py,deploy.sh} "$TASK_TMP/bundle/"
printf 'REMAINING_TAG=%s\nGATEWAY_TAG=build-3-e90ec03641f7\n' "$IMAGE_TAG" > "$TASK_TMP/bundle/images.env"
tar -czf "nongthinh-backends-$IMAGE_TAG.tar.gz" -C "$TASK_TMP/bundle" .
echo "BATCH_IMAGES_PUSHED: $IMAGE_TAG"
echo "RUN_TESTS=${RUN_TESTS:-false}"
