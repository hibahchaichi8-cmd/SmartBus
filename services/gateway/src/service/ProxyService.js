import { ApiError } from "../exception/ApiError.js";
export async function proxy(req, res, target) {
  const headers = { Accept: "application/json" };
  if (req.get("authorization"))
    headers.Authorization = req.get("authorization");
  // Deliberately do not forward cookies, X-Service-Token, role or identity headers.
  const options = {
    method: req.method,
    headers,
    signal: AbortSignal.timeout(10000),
  };
  if (!["GET", "HEAD"].includes(req.method) && req.body !== undefined) {
    headers["Content-Type"] = "application/json";
    options.body = JSON.stringify(req.body);
  }
  let upstream;
  try {
    upstream = await fetch(target + req.originalUrl, options);
  } catch {
    throw new ApiError(503, "The requested service is unavailable");
  }
  res.status(upstream.status);
  if (upstream.headers.has("location"))
    res.setHeader("Location", upstream.headers.get("location"));
  res.setHeader("Cache-Control", "no-store");
  res.setHeader("Content-Type", "application/json");
  res.send(Buffer.from(await upstream.arrayBuffer()));
}
