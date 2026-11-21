import { ApiError } from "../exception/ApiError.js";
export function positionRequest(body) {
  const { latitude, longitude, observedAt, source } = body;
  if (
    !Number.isFinite(latitude) ||
    latitude < -90 ||
    latitude > 90 ||
    !Number.isFinite(longitude) ||
    longitude < -180 ||
    longitude > 180
  )
    throw new ApiError(400, "Invalid coordinates");
  const time = new Date(observedAt);
  if (
    !["GPS", "MANUAL", "SIMULATED"].includes(source) ||
    !Number.isFinite(time.getTime()) ||
    Date.now() - time.getTime() > 120000 ||
    time.getTime() - Date.now() > 30000
  )
    throw new ApiError(
      400,
      "Use a recent timestamp and GPS, MANUAL or SIMULATED source",
    );
  return { latitude, longitude, observedAt: time, source };
}
export function occupancyRequest(body, capacity) {
  if (
    !Number.isInteger(body.passengerCount) ||
    body.passengerCount < 0 ||
    body.passengerCount > capacity
  )
    throw new ApiError(
      400,
      `Passenger count must be between 0 and ${capacity}`,
    );
  return { passengerCount: body.passengerCount };
}
