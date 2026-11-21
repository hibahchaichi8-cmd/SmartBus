export function toPublicReport(trip, report) {
  const received = report?.positionReceivedAt;
  return {
    tripId: trip.id,
    routeName: trip.routeName,
    registrationNumber: trip.registrationNumber,
    capacity: trip.capacity,
    status: trip.status,
    estimatedArrival: trip.estimatedArrival,
    overdue: trip.overdue,
    stops: trip.stops,
    latitude: report?.latitude ?? null,
    longitude: report?.longitude ?? null,
    source: report?.source ?? null,
    observedAt: report?.observedAt ?? null,
    positionReceivedAt: received ?? null,
    stale: !received || Date.now() - new Date(received).getTime() > 60000,
    passengerCount: report?.passengerCount ?? null,
    seatsAvailable:
      report?.passengerCount == null
        ? null
        : Math.max(0, trip.capacity - report.passengerCount),
    occupancyReceivedAt: report?.occupancyReceivedAt ?? null,
  };
}
