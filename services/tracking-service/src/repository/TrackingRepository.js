import { db } from "./database.js";
import { emptyReport } from "../entity/TrackingReport.js";
export class TrackingRepository {
  collection = db.collection("reports");
  async get(tripId) {
    return this.collection.findOne({ tripId }, { projection: { _id: 0 } });
  }
  async ensure(tripId) {
    await this.collection.updateOne(
      { tripId },
      { $setOnInsert: emptyReport(tripId) },
      { upsert: true },
    );
  }
  async position(tripId, position) {
    await this.ensure(tripId);
    return this.collection.findOneAndUpdate(
      {
        tripId,
        $or: [
          { observedAt: { $lt: position.observedAt } },
          { observedAt: { $exists: false } },
        ],
      },
      { $set: { ...position, positionReceivedAt: new Date() } },
      { returnDocument: "after", projection: { _id: 0 } },
    );
  }
  async occupancy(tripId, passengerCount) {
    await this.ensure(tripId);
    return this.collection.findOneAndUpdate(
      { tripId },
      { $set: { passengerCount, occupancyReceivedAt: new Date() } },
      { returnDocument: "after", projection: { _id: 0 } },
    );
  }
}
