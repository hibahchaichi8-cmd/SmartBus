import express from "express";
import { TrackingRepository } from "./repository/TrackingRepository.js";
import { TrackingService } from "./service/TrackingService.js";
import { TripClient } from "./service/TripClient.js";
import { trackingController } from "./controller/TrackingController.js";
import { errorHandler, ApiError } from "./exception/ApiError.js";
export const app = express();
app.disable("x-powered-by");
app.use(express.json({ limit: "16kb" }));
app.get("/health", (req, res) => res.json({ status: "UP" }));
app.use(
  "/api/v1/tracking",
  trackingController(
    new TrackingService(new TrackingRepository(), new TripClient()),
  ),
);
app.use((req, res, next) => next(new ApiError(404, "Endpoint not found")));
app.use(errorHandler);
