# Frontend deployment

Frontend routes currently use `RenderMode.Client`. The Docker image serves Angular
browser output using Nginx, renaming `index.csr.html` to `index.html` if needed.
No SSR Node process is deployed. If server routes change to SSR, revisit this setup.
Node 24 is used for Angular 21 compatibility. Only explicit build inputs enter the
Docker context; only browser output enters the runtime image. No unit tests run.

## First build

1. Create public Docker Hub repository `nguyenanh132/nongthinh-frontend`.
2. Commit/push `apps/web/nongthinh/Dockerfile`, `.dockerignore`, `nginx.conf`,
   `ci/Jenkinsfile.frontend`, `ci/build-frontend.sh`, and `deploy/frontend/` to main.
3. Create Jenkins Pipeline job `nongthinh-frontend`, Pipeline script from SCM:
   repository `https://github.com/nguyenAnh132/nongthinh.git`, credential `nongthinh`,
   branch `*/main`, Script Path `ci/Jenkinsfile.frontend`.
4. Build with `DEPLOY=true` (default). Existing Docker Hub and SSH credentials are reused.
   Avoid running alongside backend builds on the 4 GB Jenkins VPS.
5. Expect `FRONTEND_DEPLOY_OK` and `FRONTEND_RELEASE: web-N-COMMIT`.

Jenkins archives and copies the release bundle automatically. The independent
Compose project `nongthinh-web` binds only `127.0.0.1:8081` on the deploy VPS.
It does not modify backend Compose or secrets. Deploy waits for container health
and verifies the HTML contains `<app-root`. This is not a browser/login test.

## Domain and TLS next step

`nginx-http.conf` is the HOST Nginx bootstrap configuration, copied into the release
bundle but NOT installed automatically. Host Nginx sends `/api/v1/` to Gateway
at 127.0.0.1:8888 and other paths to frontend at 127.0.0.1:8081. It preserves API
paths, disables buffering for SSE and sets forwarded headers at the public edge.
The container itself returns 404 for `/api/`; it only serves frontend assets.

After Jenkins succeeds, install/configure host Nginx, check `nginx -t`, then obtain
the certificate for `nongthinh.nvtanh.id.vn` using Certbot. Check IPv4 A and IPv6
AAAA records and provider firewall reachability for ports 80/443 first.
Login requires HTTPS because auth cookies are secure and the callback is HTTPS.
Do not overwrite Certbot's later TLS configuration with the HTTP bootstrap on updates.

Smoke checks on the VPS before TLS:

```bash
curl -i http://127.0.0.1:8081/healthz
curl -I http://127.0.0.1:8081/
```

After TLS, validate homepage/deep-link reload, static assets, login callback,
authenticated API requests, uploads, and SSE in the browser. Certificate renewal
is a separate configuration step.

## Automatic main deployments

The pipeline registers `githubPush()` and defaults `DEPLOY=true`. Reuse the existing
GitHub push webhook at `<JENKINS_URL>/github-webhook/`. Run the updated pipeline once
manually with `DEPLOY=true` to register its trigger, then verify a subsequent main
push starts a build. Both jobs can be triggered, but each plans against its last
successful deployment and skips build/deploy when no relevant paths changed.
Frontend paths select frontend; backend service paths do not. Shared/unknown paths
select both jobs conservatively. State is stored at `/opt/nongthinh/state/frontend.json`.
Initial deployment or interrupted deployment rebuilds the job. `FORCE_REBUILD=true`
forces a rebuild; `DEPLOY=false` never advances the successful-deployment baseline.
Jenkins and the deploy host need Python 3 for `ci/release.py`.
Set the build node to 1 executor on the shared 4 GB VPS to
prevent the two jobs building simultaneously. `disableConcurrentBuilds()` only
serializes builds within one job. Deploy does not overwrite host Nginx/TLS.

Failed deployment stops the job; there is no automatic frontend rollback yet.
Stale bundles are refused by the state guard. To revert source, commit a revert to
main and run this job. After any manual image rollback, use `FORCE_REBUILD=true`
to reconcile the pipeline; do not modify deployment state manually.
