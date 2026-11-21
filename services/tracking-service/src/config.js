export const config = {
  port: Number(process.env.PORT || 3003),
  auth: process.env.AUTH_URL || "http://localhost:8081",
  transport: process.env.TRANSPORT_URL || "http://localhost:8082",
  trip: process.env.TRIP_URL || "http://localhost:8083",
  tracking: process.env.TRACKING_URL || "http://localhost:3003",
  internalToken: process.env.INTERNAL_TOKEN,
};
if (!config.internalToken || config.internalToken.length < 32)
  throw new Error("INTERNAL_TOKEN must have at least 32 characters");
