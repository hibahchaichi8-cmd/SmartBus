import express from "express";
import { gatewayController } from "./controller/GatewayController.js";
import { rateLimit } from "./security/rateLimit.js";
import { errorHandler, ApiError } from "./exception/ApiError.js";
export const app = express();
app.disable("x-powered-by");
app.use((req, res, next) => {
  res.setHeader("X-Content-Type-Options", "nosniff");
  res.setHeader("Referrer-Policy", "same-origin");
  next();
});
app.use(express.json({ limit: "32kb" }));
app.get("/health", (req, res) => res.json({ status: "UP" }));
app.use("/api/v1", rateLimit, gatewayController());
app.use((req, res, next) => next(new ApiError(404, "Endpoint not found")));
app.use(errorHandler);
