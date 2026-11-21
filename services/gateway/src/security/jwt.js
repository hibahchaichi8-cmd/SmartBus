import { createRemoteJWKSet, jwtVerify } from "jose";
import { ApiError } from "../exception/ApiError.js";
const jwks = createRemoteJWKSet(
  new URL(
    (process.env.AUTH_URL || "http://localhost:8081") +
      "/.well-known/jwks.json",
  ),
);
export async function authenticate(req, res, next) {
  const match = /^Bearer (\S+)$/.exec(req.get("authorization") || "");
  if (!match) throw new ApiError(401, "Please sign in");
  try {
    const { payload } = await jwtVerify(match[1], jwks, {
      issuer: "smartbus-auth",
      audience: "smartbus-api",
      algorithms: ["RS256"],
      requiredClaims: ["exp", "iat", "sub", "roles"],
    });
    if (
      typeof payload.sub !== "string" ||
      !payload.sub.trim() ||
      !Array.isArray(payload.roles) ||
      !payload.roles.length ||
      !payload.roles.every((r) => ["ADMIN", "CITIZEN", "DRIVER"].includes(r))
    )
      throw new Error("Invalid claims");
    req.user = { id: payload.sub, roles: payload.roles };
    next();
  } catch (error) {
    if (
      error.code === "ERR_JWKS_TIMEOUT" ||
      error.code === "ERR_JOSE_GENERIC" ||
      error instanceof TypeError
    )
      throw new ApiError(503, "Authentication service unavailable");
    throw new ApiError(401, "Session expired or invalid");
  }
}
export function role(required) {
  return (req, res, next) => {
    if (!req.user?.roles.includes(required))
      throw new ApiError(403, "Access denied");
    next();
  };
}
