import { Router } from "express";

import { config } from "../config.js";
import { proxy } from "../service/ProxyService.js";
import { authenticate } from "../security/jwt.js";
import { ApiError } from "../exception/ApiError.js";

export function gatewayController() {
  const router = Router();

  router.use(async (req, res) => {
    const path = req.path;
    let target;

    if (
      /^\/auth\/(register|login|me)$/.test(path) ||
      /^\/drivers(?:\/|$)/.test(path)
    ) {
      target = config.auth;
    } else if (
      /^\/(buses|stations|routes|fares)(?:\/|$)/.test(path)
    ) {
      target = config.transport;
    } else if (
      /^\/(schedules|journeys|trips)(?:\/|$)/.test(path)
    ) {
      target = config.trip;
    } else if (
      /^\/tickets(?:\/|$)/.test(path) ||
      /^\/admin\/tickets(?:\/|$)/.test(path)
    ) {
      target = config.ticket;
    } else if (/^\/notifications(?:\/|$)/.test(path)) {
      target = config.notification;
    } else if (/^\/tracking(?:\/|$)/.test(path)) {
      target = config.tracking;
    } else {
      throw new ApiError(404, "Endpoint not found");
    }

    const publicRead =
      req.method === "GET" &&
      /^\/(buses|stations|routes|fares|schedules|journeys|tracking)(?:\/|$)/.test(
        path,
      );

    const publicAuth =
      req.method === "POST" &&
      /^\/auth\/(register|login)$/.test(path);

    if (!publicRead && !publicAuth) {
      await authenticate(req, res, () => {});
    }

    await proxy(req, res, target);
  });

  return router;
}