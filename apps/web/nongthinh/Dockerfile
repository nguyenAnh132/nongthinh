FROM node:24-bookworm-slim AS build
WORKDIR /build
ENV CI=true NG_CLI_ANALYTICS=false NODE_OPTIONS=--max-old-space-size=2048
COPY package.json package-lock.json ./
RUN --mount=type=cache,target=/root/.npm npm ci --no-audit --no-fund
COPY angular.json tsconfig*.json postcss.config.js ./
COPY src ./src
COPY public ./public
RUN npm run build -- --configuration production && \
    if [ ! -f dist/nongthinh/browser/index.html ]; then \
      cp dist/nongthinh/browser/index.csr.html dist/nongthinh/browser/index.html; \
    fi

FROM nginx:stable-alpine AS runtime
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /build/dist/nongthinh/browser/ /usr/share/nginx/html/
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=3s --start-period=10s --retries=6 \
  CMD wget -q -O /dev/null http://127.0.0.1:8080/healthz || exit 1
