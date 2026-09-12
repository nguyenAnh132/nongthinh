# Nongthinh

This project was generated using [Angular CLI](https://github.com/angular/angular-cli) version 21.2.8.

## Development server

To start a local development server, run:

```bash
ng serve
```

Once the server is running, open your browser and navigate to `http://localhost:4200/`. The application will automatically reload whenever you modify any of the source files.

### Backend proxy & authentication

The portal proxies `/api` requests to the API gateway at `http://localhost:8888` (see `proxy.conf.json`). Authentication is cookie-based and session cookies are issued by `auth-service` (port `9090`, via the gateway).

When running everything over plain HTTP locally, the cookies must not be marked `Secure`, otherwise the browser will silently drop them and `/me` will keep returning `401` after login. Start `auth-service` with the bundled `dev` profile or override the cookie environment variables, e.g.:

```bash
# Spring profile (uses services/auth-service/src/main/resources/application-dev.yaml)
SPRING_PROFILES_ACTIVE=dev mvn -pl services/auth-service spring-boot:run

# Or override directly
COOKIE_SECURE=false COOKIE_SAME_SITE=Lax mvn -pl services/auth-service spring-boot:run
```

Production deployments must keep `COOKIE_SECURE=true` and serve the portal over HTTPS.

## Code scaffolding

Angular CLI includes powerful code scaffolding tools. To generate a new component, run:

```bash
ng generate component component-name
```

For a complete list of available schematics (such as `components`, `directives`, or `pipes`), run:

```bash
ng generate --help
```

## Building

To build the project run:

```bash
ng build
```

This will compile your project and store the build artifacts in the `dist/` directory. By default, the production build optimizes your application for performance and speed.

## Running unit tests

To execute unit tests with the [Vitest](https://vitest.dev/) test runner, use the following command:

```bash
ng test
```

## Running end-to-end tests

For end-to-end (e2e) testing, run:

```bash
ng e2e
```

Angular CLI does not come with an end-to-end testing framework by default. You can choose one that suits your needs.

## Additional Resources

For more information on using the Angular CLI, including detailed command references, visit the [Angular CLI Overview and Command Reference](https://angular.dev/tools/cli) page.
