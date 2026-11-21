# Demo walkthrough

## Prepare

Start Docker Desktop, initialize `.env`, run `start.ps1`, and open http://localhost:4200. Keep your chosen administrator credentials private. Verify containers with `docker compose ps`; follow logs if Java startup is still in progress.

## Sprint 1 demonstration

1. Public search: choose Tunis Marine → Manouba. Choose a time before 22:00 on a desired day. Show route 20, next departure, 35-minute arrival duration and 1.200 TND fare. Searching from Le Bardo starts with that station's +15-minute offset.
2. Search in reverse to demonstrate ordered routes and a clear no-results message. This seed contains one-way routes; return routes must be configured separately.
3. Sign in as administrator. Show Buses, Stations, Routes, Fares and Schedules.
4. Add a bus with a unique registration. Edit its capacity. Add an unreferenced station, edit its name, then delete it.
5. Show route stops and their elapsed-minute offsets. Schedule fields are clock times; interval means minutes between departures.
6. Edit route 20's fare and search again to observe the new price. Restore the demo value if desired.
7. Sign out and register a citizen. Explain that registration cannot grant admin or driver permissions. Signing out removes the browser token; the backend checks every protected request.

## Sprint 2 demonstration

1. Sign in as administrator → Drivers → Add driver. Choose a new email and private initial password.
2. Trips → Assign trip. Select route 20, TN-1001 and your driver. Choose a future departure. Overlapping driver/bus assignments are rejected.
3. Sign out and sign in as that driver. My trips shows only this driver's assignments.
4. Start the trip. Confirm the first stop when demonstrating arrival there. A second active trip on the same bus/driver is blocked.
5. Set passenger count to 18. Send a simulated stop position. This is visibly labelled SIMULATED; it is not real bus GPS.
6. Open Live buses to show the position, count, seats and arrival estimate. With TN-1001's 50 seats, 18 passengers means 32 available seats. Reporting stops when the driver leaves My trips. On a separate browser/tab/device, sign in as the driver to keep reporting while the citizen view is open.
7. Alternatively choose Share my GPS position and allow browser permission yourself. HTTPS or localhost is required by browsers; a desktop GPS location is the device's location, not a real bus sensor.
8. Return to My trips, confirm remaining stops in order. Explain how ETA uses actual start / the most recent confirmed stop. Finish the trip only after all stops are confirmed.
9. Live buses no longer lists the completed trip. Admin trip history retains its snapshots and status.

Location data is the latest report only. Waiting over 60 seconds without a new position marks it stale; sending only a new passenger count does not refresh it. Manual location and simulated stop positions are supported for a classroom demo without access to a real bus.

## Explain the architecture in one minute

“Angular is the web interface. It sends REST requests through an Express gateway. Spring Boot services manage accounts and JWT issuance, transport reference data and fares, and schedules/trips. An Express service manages live tracking and occupancy. Each business service has its own MongoDB database and separates controllers, DTOs, services, repositories, entities, mappers, exceptions and security. The auth service signs JWTs with its private RSA key; other services verify the public key and check roles and trip ownership. Docker Compose runs the stack and preserves MongoDB and signing keys in volumes.”
