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
and webhook-triggered builds are separate configuration steps.

Failed deployment stops the job; there is no automatic frontend rollback yet.
Use a previous archived bundle's deploy.sh to redeploy that frontend image if needed.
