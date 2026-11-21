# Sprint scope and acceptance criteria

This document records the implemented scope. Sprint 1 IDs come from the supplied requirements. Sprint 2 IDs below are local labels, not claims about the team's JIRA numbering. The teacher's Sprint 1 validation date was Monday, November 2, 2026; no Sprint 2 deadline is invented here.

## Sprint 1

| ID   | User story                                           | Acceptance criteria                                                                                                                                              |
| ---- | ---------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| US01 | As a citizen, I register and sign in.                | Valid registration creates CITIZEN only; duplicate email rejected; password hashed; login issues signed JWT; role restrictions enforced server-side.             |
| US17 | As an administrator, I manage buses.                 | Create/list/edit/delete registration and capacity; unique registration and valid capacity; references protected.                                                 |
| US18 | As an administrator, I manage stations and routes.   | Station names and optional coordinate pair; routes have ordered, existing stops with increasing minutes starting at zero; referenced stations cannot be deleted. |
| US20 | As an administrator, I configure route fares.        | One standard fare per route; non-negative integer millimes; citizen sees three-decimal TND fare.                                                                 |
| US03 | As a citizen, I search origin and destination.       | Existing distinct stations; only routes travelling in the selected order; clear empty result; future departures on selected date.                                |
| US04 | As a citizen, I see departure and estimated arrival. | Tunis local HH:mm display; daily first/last/interval timetable; boarding offset and duration applied; estimates labelled.                                        |
| US05 | As a citizen, I see the fare.                        | Journey result includes the configured route fare in TND.                                                                                                        |

## Sprint 2

| Local ID | User story                                                            | Acceptance criteria                                                                                                                                                                           |
| -------- | --------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| S2-01    | As an administrator, I create drivers and assign trips.               | Driver role created only by ADMIN; valid bus/route/driver; future departure; overlapping assignments rejected for the prototype's single trip instance.                                       |
| S2-02    | As an assigned driver, I operate my trip.                             | Only own PLANNED trip starts; one active trip per bus/driver; stop confirmations follow order; all stops confirmed before finish; admin can cancel PLANNED trips.                             |
| S2-03    | As a driver, I report the bus location.                               | GPS permission requested only on explicit action; valid recent coordinates; manual and simulation options labelled; another driver's report rejected; sharing stops when leaving driver page. |
| S2-04    | As a citizen, I view active buses on a map.                           | Active trips update every five seconds; source and last-position freshness shown; unknown/stale states explicit; completed trips excluded; no driver identity in public reports.              |
| S2-05    | As a driver, I update occupancy; as a citizen, I see available seats. | Whole count 0..capacity; unknown until reported; available seats = capacity minus count; last-count time shown.                                                                               |
| S2-06    | As a citizen, I see an updated arrival estimate.                      | Estimate uses actual start and latest confirmed stop; overdue estimates labelled; timetable search and live progress clearly distinguished.                                                   |

## Definition of done for classroom review

Source is organized by the requested responsibilities, all listed flows have usable English UI, sample data and launch documentation are included, and verification results distinguish automated checks, browser observations and unverified Docker execution. This is a classroom prototype, not production deployment approval.

Out of scope: real payments, QR ticketing, driver password reset, journey transfers, live traffic modelling, timetable exceptions, overnight schedules, deployment and Teams/JIRA/GitHub uploads.
