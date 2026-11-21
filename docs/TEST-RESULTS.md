# Actual verification results

Verified locally on November 7, 2026. These results describe the generated project, not the older project or a deployment.

## Passed

| Check | Actual result |
|---|---|
| Java 17 / Maven `verify` | All three Spring Boot 4.1.1 services built; 4 JUnit tests passed, zero failures. |
| Gateway `npm test` | 1 Node test passed: internal-path isolation, missing authentication, spoofed role header, malformed JSON. |
| Tracking `npm test` | 4 Node tests passed: RS256 signature/expiry/audience/roles, location age/bounds, occupancy validation, privacy/freshness and ownership/state rules. |
| Angular production build | Passed with Angular 21.2.26 and TypeScript 5.9.3. |
| Integration smoke script | 49 HTTP requests passed against all five backend processes and real MongoDB, plus fare/duration/departure/seats/privacy/state assertions. |
| Additional direct worker checks | 9 requests passed: valid/expired/wrong-audience/wrong-issuer/missing-expiry/empty-role JWTs, missing worker authentication and untrusted internal calls. Public JWKS contained no private RSA parameters. |
| Compose configuration | `docker compose config --quiet` passed with temporary validation-only environment values. |
| Browser review | Citizen registration/search, admin sign-in/assignment, driver sign-in/start, passenger count, simulated location, live map, sequential checkpoint confirmations and completion passed. Narrow and desktop layouts inspected. Screenshots in `docs/screenshots`. |

The HTTP integration checks used Java service ports 18081–18083, Express ports 13000/13003, and dedicated temporary MongoDB databases. They did not use or migrate the old project's databases. The browser used the compiled Angular output behind a local same-origin test proxy at port 14200. Test passwords and signing keys were confined to the isolated QA environment and are excluded from the source archive.

The integration checks covered registration role injection (server still assigns CITIZEN), login rejection, duplicate email/registration, missing or tampered bearer token, citizen/admin separation, CRUD/reference restrictions, timetable validation, ordered trip ownership and state transitions, overlap rejection, invalid passenger count, simulated location reporting, replayed location timestamps, completed-trip hiding, and absence of passwordHash in API responses.

## Not verified / limits

- **Docker images and the full Compose runtime were not executed for this new project.** Access to the Docker Desktop engine was denied in this tool session. Dockerfiles and Compose are provided; Compose configuration was checked. Java/Node services were executed directly against real MongoDB instead.
- Browser GPS hardware reporting and permission-denial branches were not exercised; manual/simulated reporting and server-side validation were checked. No actual bus GPS or traffic feed is connected.
- Browser review used the compiled frontend with a local test proxy, not the Nginx container. Nginx CSP/proxy configuration has not been runtime-tested here.
- No load test, multi-replica consistency test, payment integration, public HTTPS deployment, automated browser suite, or Teams/JIRA/GitHub upload was performed.
- Tests are focused classroom-prototype checks, not a claim of exhaustive security or production readiness.

## Repeat the source checks

Install dependencies with `npm ci` in frontend and both Express folders, then run `scripts/check.ps1`. Java tests require Java 17 and Maven. The integration script requires Python 3 and a running **isolated** application, with API_BASE, ADMIN_EMAIL and ADMIN_PASSWORD in the environment. It creates test accounts and historical trips; do not point it at a real user's database.

Screenshots show disposable QA data, not a production database or real passenger tracking.
