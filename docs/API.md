# REST API — v1

Base: `http://localhost:3000/api/v1` (or configured API_PORT). Frontend uses same-origin `/api/v1` through Nginx. JSON bodies; `Content-Type: application/json`. Protected requests use `Authorization: Bearer <accessToken>`. There is no Basic authentication.

## Roles and endpoints

| Method | Path relative to base                                               | Access          | Behavior                                                |
| ------ | ------------------------------------------------------------------- | --------------- | ------------------------------------------------------- |
| POST   | /auth/register                                                      | Public          | Register CITIZEN, return session, 201                   |
| POST   | /auth/login                                                         | Public          | Verify BCrypt password, return session, 200             |
| GET    | /auth/me                                                            | Signed in       | Safe current account DTO                                |
| GET    | /drivers                                                            | ADMIN           | Driver accounts                                         |
| POST   | /drivers                                                            | ADMIN           | Create DRIVER, 201                                      |
| GET    | /buses, /stations, /routes, /fares                                  | Public          | Array; optional `limit` default 200, max 500            |
| GET    | /buses/{id}, /stations/{id}, /routes/{id}, /fares/{id}              | Public          | One resource                                            |
| POST   | /buses, /stations, /routes, /fares                                  | ADMIN           | Create, 201 and Location header                         |
| PUT    | /buses/{id}, /stations/{id}, /routes/{id}, /fares/{id}              | ADMIN           | Replace editable fields, 200                            |
| DELETE | /buses/{id}, /stations/{id}, /routes/{id}, /fares/{id}              | ADMIN           | Delete, 204; referenced records return 409              |
| GET    | /schedules                                                          | Public          | Daily schedules                                         |
| PUT    | /schedules/{routeId}                                                | ADMIN           | Create/replace recurring schedule, 200                  |
| DELETE | /schedules/{routeId}                                                | ADMIN           | Remove schedule, 204                                    |
| GET    | /journeys?originId=...&destinationId=...&departure=2026-11-20T06:01 | Public          | Next direct departures for the supplied Tunis date/time |
| GET    | /trips                                                              | ADMIN           | Latest 200 assigned trips                               |
| POST   | /trips                                                              | ADMIN           | Create assignment, 201                                  |
| GET    | /trips/mine                                                         | DRIVER          | Own latest 200 assigned trips                           |
| POST   | /trips/{id}/start                                                   | Assigned DRIVER | PLANNED → IN_PROGRESS                                   |
| POST   | /trips/{id}/checkpoints                                             | Assigned DRIVER | Confirm next stop in route order                        |
| POST   | /trips/{id}/finish                                                  | Assigned DRIVER | IN_PROGRESS → COMPLETED after all stops                 |
| POST   | /trips/{id}/cancel                                                  | ADMIN           | PLANNED → CANCELLED                                     |
| GET    | /tracking                                                           | Public          | Active trips with latest report, seats and progress ETA |
| PUT    | /tracking/{tripId}/position                                         | Assigned DRIVER | Recent GPS, MANUAL or SIMULATED position on active trip |
| PUT    | /tracking/{tripId}/occupancy                                        | Assigned DRIVER | Integer count within active bus capacity                |

No request may choose its own ID, passwordHash, admin role, trip status or actual start/end timestamps. Role supplied to public registration is ignored; the server always assigns CITIZEN. Driver accounts cannot be created by citizens. PUT replaces the shown fields; PATCH is not implemented.

## Body examples

Registration / driver creation:

```json
{
  "fullName": "Example Citizen",
  "email": "citizen@example.test",
  "password": "ChooseYourOwnPassword"
}
```

Login:

```json
{ "email": "citizen@example.test", "password": "ChooseYourOwnPassword" }
```

Session response:

```json
{
  "accessToken": "<JWT>",
  "tokenType": "Bearer",
  "expiresAt": "2026-11-20T07:30:00Z",
  "user": {
    "id": "<id>",
    "fullName": "Example Citizen",
    "email": "citizen@example.test",
    "role": "CITIZEN"
  }
}
```

Bus:

```json
{ "registrationNumber": "TN-1003", "capacity": 50 }
```

Station (coordinates optional as a pair):

```json
{ "name": "Example Station", "latitude": 36.81, "longitude": 10.16 }
```

Route:

```json
{
  "code": "20",
  "name": "Tunis – Manouba",
  "stops": [
    { "stationId": "station-tunis", "offsetMinutes": 0 },
    { "stationId": "station-bardo", "offsetMinutes": 15 },
    { "stationId": "station-manouba", "offsetMinutes": 35 }
  ]
}
```

Fare (integer millimes, one fare per route):

```json
{ "routeId": "route-20", "amountMillimes": 1200 }
```

Schedule:

```json
{ "firstDeparture": "06:00", "lastDeparture": "22:00", "intervalMinutes": 20 }
```

Trip assignment (use real IDs and a future time, explicit timezone):

```json
{
  "routeId": "route-20",
  "busId": "bus-1001",
  "driverId": "<driver-id>",
  "plannedDeparture": "2026-11-20T08:00:00+01:00"
}
```

Checkpoint (zero-based; the next index equals the number of already-confirmed stops):

```json
{ "stopIndex": 0 }
```

Location (replace observedAt with the current timestamp):

```json
{
  "latitude": 36.8008,
  "longitude": 10.1914,
  "source": "SIMULATED",
  "observedAt": "2026-11-20T07:00:00Z"
}
```

Occupancy:

```json
{ "passengerCount": 18 }
```

Start, finish and cancel accept an empty JSON object. Journey departure is local Tunisia time, while response instants use UTC `Z`. Convert for display. Search uses daily schedules and the offset at the selected boarding stop; it does not return transfers or automatically reserve a seat.

## Errors

Typical business/validation response:

```json
{
  "timestamp": "2026-11-20T07:00:00Z",
  "status": 400,
  "code": "HTTP_400",
  "message": "Please check the form",
  "path": "/api/v1/buses",
  "errors": { "capacity": "must be greater than or equal to 1" }
}
```

Security and request-limit responses may contain only `status` and `message`. Clients should always read those two fields and optionally `errors`. 401 means sign in again; 403 means the role/ownership is insufficient; 409 means duplicate, referenced, overlapping or invalid state. Dependency outages return 503. Do not expose stack traces.

## Internal API

These routes exist only on worker ports, are not published by Compose, and require `X-Service-Token`:

- Auth: GET `/internal/drivers/{id}`; public GET `/.well-known/jwks.json` shares **public** RSA data only.
- Transport: GET `/internal/buses/{id}`, `/internal/stations/{id}`, `/internal/routes/{id}`, `/internal/fares/{routeId}`.
- Trip: GET `/internal/trips/active`, `/internal/trips/{id}`, `/internal/references/{routes|buses}/{id}`.
- Every process offers GET `/health` (liveness only, not a guarantee that all dependencies are ready).

The gateway never exposes `/internal` or forwards client-provided internal tokens. Each service independently validates authentication and authorization.

Routes appear in journey search once both their schedule and standard fare are configured. A route without a fare is omitted; service outages still return an error.
