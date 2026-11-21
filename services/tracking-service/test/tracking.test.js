import test from "node:test";
import assert from "node:assert/strict";
import {
  positionRequest,
  occupancyRequest,
} from "../src/dto/trackingRequest.js";
import { toPublicReport } from "../src/mapper/TrackingMapper.js";
import { TrackingService } from "../src/service/TrackingService.js";
test("position age, bounds and occupancy are validated", () => {
  assert.throws(() =>
    positionRequest({
      latitude: 91,
      longitude: 10,
      source: "GPS",
      observedAt: new Date().toISOString(),
    }),
  );
  assert.throws(() =>
    positionRequest({
      latitude: 36,
      longitude: 10,
      source: "GPS",
      observedAt: new Date(Date.now() - 180000).toISOString(),
    }),
  );
  assert.throws(() => occupancyRequest({ passengerCount: 51 }, 50));
  assert.throws(() => occupancyRequest({ passengerCount: 1.5 }, 50));
  assert.equal(occupancyRequest({ passengerCount: 0 }, 50).passengerCount, 0);
});
test("occupancy does not make old GPS fresh or expose driver identity", () => {
  const result = toPublicReport(
    {
      id: "t",
      capacity: 50,
      driverId: "private",
      driverName: "Private",
      stops: [],
    },
    {
      passengerCount: 10,
      positionReceivedAt: new Date(Date.now() - 61000),
      occupancyReceivedAt: new Date(),
    },
  );
  assert.equal(result.stale, true);
  assert.equal(result.seatsAvailable, 40);
  assert.equal("driverId" in result, false);
  assert.equal("driverName" in result, false);
});
test("only the assigned driver on an active trip may report", async () => {
  const service = new TrackingService(
    {},
    { one: async () => ({ driverId: "d", status: "PLANNED" }) },
  );
  await assert.rejects(() => service.owned("t", { id: "other" }), {
    status: 403,
  });
  await assert.rejects(() => service.owned("t", { id: "d" }), { status: 409 });
});
