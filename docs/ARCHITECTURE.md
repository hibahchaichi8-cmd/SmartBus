# Architecture

## Responsibilities

| Service           | Technology                 | Own database       | Main responsibility                                                            |
| ----------------- | -------------------------- | ------------------ | ------------------------------------------------------------------------------ |
| auth-service      | Java 17, Spring Boot 4.1.1 | smartbus_auth      | Users, BCrypt, role assignment, RS256 token issuance, public JWKS              |
| transport-service | Java 17, Spring Boot 4.1.1 | smartbus_transport | Buses, stations, ordered routes, standard route fares                          |
| trip-service      | Java 17, Spring Boot 4.1.1 | smartbus_trip      | Daily schedules, direct journey search, driver assignments and trip progress   |
| tracking-service  | Express / Node 24          | smartbus_tracking  | Latest location and passenger count; public projection without driver identity |
| gateway           | Express / Node 24          | None               | Allowlisted API routing, bearer validation, request limits, proxying           |
| frontend          | Angular 21, Nginx          | None               | Citizen, administrator and driver interfaces                                   |

Each Java service is an independent Maven project. The parent `services/pom.xml` simply builds the three together. No service reads another service's collections. Internal HTTP uses a random shared service token; only worker containers can reach each other's unpublished ports through the Docker network.

## Request flow

1. Angular submits a DTO to `/api/v1/...` through Nginx and the gateway.
2. Gateway chooses an allowlisted service, verifies bearer tokens on protected requests, and never forwards client-supplied internal/identity headers.
3. The target service independently validates RS256 signature, issuer, audience and expiry. Java method annotations / Express role middleware enforce role rules.
4. Controller validates input, Service applies business rules, Repository persists entities, Mapper returns safe response DTOs.
5. Exceptions become JSON responses. Invalid fields are 400; unauthenticated 401; forbidden 403; missing resource 404; duplicates/state conflicts 409; dependency failures 503.

## Authentication

Only auth-service owns the private RSA key. Other services fetch the corresponding public JWKS from the configured auth URL, never a URL taken from the token. Issuer is `smartbus-auth`, audience is `smartbus-api`, algorithm is RS256. `sub` is the account ID; `roles` is an array. Public registration always creates CITIZEN. ADMIN is initialized privately from the environment; ADMIN can create DRIVER.

Tokens live for 30 minutes. No refresh-token, password-reset or revocation service is implemented. Browser sign-out removes its in-memory token; a copied token remains valid until expiry. The key volume must survive container restarts. Back it up privately with the database; deleting it invalidates existing tokens after verifier caches update. TLS and managed secrets are needed before any real public deployment.

## Data model and time

- `users`: id, fullName, normalized unique email, BCrypt passwordHash, role. Password hash never appears in API response DTOs.
- `buses`: unique registration and capacity (1–200).
- `stations`: name, optional coordinate pair.
- `routes`: unique code, name, ordered station references and strictly increasing minute offsets beginning at zero.
- `fares`: one route reference and integer amountMillimes. 1000 millimes = 1 TND. Sprint 1 offers one standard fare per route.
- `schedules`: one daily schedule per route, first/last departure and interval. API/UI use HH:mm; storage uses minutes. Overnight schedules are intentionally excluded.
- `trips`: bus/driver/route assignment, immutable descriptive snapshots, status, planned UTC instant, actual start/end, ordered checkpoint instants and optimistic version.
- `reports`: latest position and latest passenger count per trip. Position and occupancy timestamps are separate; occupancy does not make an old position fresh.

UI times use Africa/Tunis (UTC+01). Search accepts a local Tunis date/time. Trip creation requires an explicit offset in its datetime. Search finds the next recurring departure at the boarding station and calculates scheduled arrival. Driver ETA uses actual start and the most recently confirmed stop. It does not predict traffic. Live page flags overdue estimates.

## Integrity and limits

Role restrictions and trip ownership are enforced in worker services. Checkpoints must be confirmed in route order; all stops must be confirmed before finishing. Only PLANNED trips can be cancelled. Unique partial MongoDB indexes prevent two active trips for one driver or bus. Optimistic locking prevents lost trip state updates. Overlapping planned assignments are rejected within a single trip-service instance.

Cross-service checks are pragmatic HTTP checks, not distributed transactions. Reference checks block deleting referenced stations/routes/buses and fail closed if trip-service is unavailable. Historical trips retain their snapshots. Concurrent cross-service create/delete operations and overlap checks across multiple service replicas are not fully serialized. Run one instance of each business service for this prototype; stronger consistency needs reservation records or a coordinated workflow.

Catalog lists are bounded (default 200, maximum 500); trip admin/driver lists show up to 200 records. Search returns up to 50 direct routes. No pagination UI, transfers, offline operation or historical GPS analytics. Location updates are polled every five seconds; they must be recent, valid coordinates and newer than the previous report. Reports become stale after 60 seconds. MANUAL and SIMULATED data are visibly labelled. Browser GPS permission remains the driver's choice; leaving the page stops its watcher. Existing latest reports remain stored after finishing but are excluded from public active results.

One unauthenticated MongoDB instance with separate logical databases is used for a local classroom demo. Published ports bind to localhost. Logical databases are ownership boundaries, not separate credentials or hosts. For deployment, add MongoDB authentication, per-service accounts, HTTPS, centralized secrets, stronger rate limiting and backups. The in-memory gateway limiter is single-instance; behind Nginx its IP attribution is coarse. No cloud deployment is included.

The map uses Leaflet and OpenStreetMap tiles with attribution. External tiles require internet and reveal map tile requests to the tile provider; bus telemetry is sent only to this application's backend. Stop polylines are illustrative, not routed roads.

Technical references used: [Spring Security JWT](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html), [Spring Boot OAuth2](https://docs.spring.io/spring-boot/reference/security/oauth2.html), [jose](https://github.com/panva/jose), [Leaflet](https://leafletjs.com/reference-1.9.4.html), [OSM tile policy](https://operations.osmfoundation.org/policies/tiles/).
