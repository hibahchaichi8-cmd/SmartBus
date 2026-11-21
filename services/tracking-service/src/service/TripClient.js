import { config } from "../config.js";
import { ApiError } from "../exception/ApiError.js";
export class TripClient {
  async get(path) {
    let response;
    try {
      response = await fetch(config.trip + path, {
        headers: { "X-Service-Token": config.internalToken },
        signal: AbortSignal.timeout(5000),
      });
    } catch {
      throw new ApiError(503, "Trip service unavailable");
    }
    if (response.status === 404) throw new ApiError(404, "Trip not found");
    if (!response.ok) throw new ApiError(503, "Trip service unavailable");
    return response.json();
  }
  active() {
    return this.get("/internal/trips/active");
  }
  one(id) {
    if (!/^[a-zA-Z0-9-]{1,80}$/.test(id))
      throw new ApiError(400, "Invalid trip identifier");
    return this.get("/internal/trips/" + id);
  }
}
