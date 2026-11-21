import { ApiError } from "../exception/ApiError.js";
import { positionRequest, occupancyRequest } from "../dto/trackingRequest.js";
import { toPublicReport } from "../mapper/TrackingMapper.js";
export class TrackingService {
  constructor(repository, trips) {
    this.repository = repository;
    this.trips = trips;
  }
  async list() {
    const active = await this.trips.active();
    return Promise.all(
      active.map(async (trip) =>
        toPublicReport(trip, await this.repository.get(trip.id)),
      ),
    );
  }
  async owned(id, user) {
    const trip = await this.trips.one(id);
    if (trip.driverId !== user.id)
      throw new ApiError(403, "This trip belongs to another driver");
    if (trip.status !== "IN_PROGRESS")
      throw new ApiError(409, "Start this trip first");
    return trip;
  }
  async position(id, user, body) {
    const trip = await this.owned(id, user);
    const report = await this.repository.position(id, positionRequest(body));
    if (!report)
      throw new ApiError(
        409,
        "Position timestamp must be newer than the last report",
      );
    return toPublicReport(trip, report);
  }
  async occupancy(id, user, body) {
    const trip = await this.owned(id, user);
    const { passengerCount } = occupancyRequest(body, trip.capacity);
    return toPublicReport(
      trip,
      await this.repository.occupancy(id, passengerCount),
    );
  }
}
