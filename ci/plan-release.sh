#!/usr/bin/env bash
set -Eeuo pipefail
set +x
kind="${1:?backend or frontend}"
[[ "$kind" == backend || "$kind" == frontend ]] || exit 2
# Missing state is a first deployment; connection/read failures must fail the job.
ssh -i "$DEPLOY_KEY" -o IdentitiesOnly=yes -o BatchMode=yes -o StrictHostKeyChecking=yes \
    "$DEPLOY_USER@160.250.186.236" \
    "if test -f /opt/nongthinh/state/$kind.json; then cat /opt/nongthinh/state/$kind.json; else printf '{}'; fi" \
    > previous-release.json
args=()
if [[ "${FORCE_REBUILD:-false}" == true ]]; then args+=(--force); fi
python3 ci/release.py plan --kind "$kind" --state previous-release.json --tag "$IMAGE_TAG" "${args[@]}"
