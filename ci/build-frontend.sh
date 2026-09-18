#!/usr/bin/env bash
set -Eeuo pipefail
set +x
: "${IMAGE_TAG:?Missing IMAGE_TAG}"
: "${DOCKERHUB_USER:?Missing DOCKERHUB_USER}"
: "${DOCKERHUB_TOKEN:?Missing DOCKERHUB_TOKEN}"
[[ "$IMAGE_TAG" =~ ^web-[0-9]+-[a-f0-9]{12}$ ]] || exit 2
TASK_TMP="$(mktemp -d)"
export DOCKER_CONFIG="$TASK_TMP/docker"
cleanup() { case "$TASK_TMP" in /tmp/tmp.*) rm -rf -- "$TASK_TMP" ;; esac; }
trap cleanup EXIT
mkdir -p "$DOCKER_CONFIG" "$TASK_TMP/source" "$TASK_TMP/bundle"
printf '%s' "$DOCKERHUB_TOKEN" | docker login --username "$DOCKERHUB_USER" --password-stdin
git archive HEAD apps/web/nongthinh deploy/frontend | tar -x -C "$TASK_TMP/source"
image="nguyenanh132/nongthinh-frontend:$IMAGE_TAG"
docker build --pull --progress=plain --tag "$image" "$TASK_TMP/source/apps/web/nongthinh"
docker run --rm --network none "$image" nginx -t
docker push "$image"
cp "$TASK_TMP/source/deploy/frontend/"{compose.yml,deploy.sh,nginx-http.conf} "$TASK_TMP/bundle/"
printf 'FRONTEND_TAG=%s\n' "$IMAGE_TAG" > "$TASK_TMP/bundle/images.env"
tar -czf "nongthinh-frontend-$IMAGE_TAG.tar.gz" -C "$TASK_TMP/bundle" .
echo "FRONTEND_IMAGE_PUSHED: $image"
