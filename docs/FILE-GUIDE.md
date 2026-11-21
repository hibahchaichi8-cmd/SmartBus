# Reading the project

Start with one simple feature, then follow the same pattern for the others.

## Example: create a bus

1. `frontend/src/app/pages/admin/admin.component.html` collects registration and capacity.
2. `admin.component.ts` builds a request and calls `ApiService`.
3. `core/auth.interceptor.ts` attaches the in-memory JWT to our API request.
4. Express `gateway/src/controller/GatewayController.js` chooses transport-service.
5. Java `transport/controller/BusController.java` accepts `BusRequest`, validates it and checks ADMIN role.
6. `transport/service/BusService.java` applies business rules.
7. `transport/mapper/BusMapper.java` converts the request into a Bus entity, and the saved entity into a BusResponse.
8. `transport/repository/BusRepository.java` stores the entity in MongoDB.
9. `transport/exception/GlobalExceptionHandler.java` handles invalid input, duplicate registration and other errors.

Java package paths above are beneath `services/transport-service/src/main/java/tn/smartbus/`. Entity is the persisted domain object; DTO is the HTTP contract. Returning DTOs prevents accidental exposure of internal persistence fields.

## Example: report a bus position

Driver page → API client → gateway → tracking controller → JWT and DRIVER middleware → TrackingService ownership/state checks against trip-service → position DTO validation → TrackingRepository MongoDB update → TrackingMapper safe public response. Entity/TrackingReport describes the latest-report record. Position and passenger count update different fields atomically.

## Configuration and support files

- `compose.yaml`: seven containers, service URLs, ports and persistent volumes.
- `.env.example`: placeholder variable names; `init-env.ps1` creates the private real `.env`.
- `start.ps1`: builds and starts the Docker stack.
- Per-service `Dockerfile`: creates that service's runnable image.
- Java `application.properties`: environment-driven ports and MongoDB/service URLs.
- Per-Java-service `pom.xml`: Java dependencies and build; `services/pom.xml` builds all three.
- Express `package.json` and `package-lock.json`: Node dependencies, scripts and reproducible dependency versions.
- Angular `angular.json`, `tsconfig.json`, `package*.json`: frontend build/dependencies.
- `frontend/nginx.conf`: production static hosting and same-origin API proxy.
- `frontend/proxy.json`: development Angular API proxy.
- `scripts/check.ps1`: Java/Node tests and frontend build.
- `scripts/smoke.py`: opt-in integration checks against an isolated running app.
- `src/test` and Express `test`: source tests.
- `docs`: scope, architecture, API, demo, migration, results and submission checklist.

Generated `node_modules`, `target`, `dist`, `.angular`, local keys and `.env` are not part of the source archive and should not be committed.
