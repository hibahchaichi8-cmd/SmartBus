// Only the latest report is stored. Position and occupancy have independent clocks.
export function emptyReport(tripId) {
  return { tripId, passengerCount: null };
}
