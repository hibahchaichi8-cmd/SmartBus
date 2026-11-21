import { Router } from "express";
import { authenticate, role } from "../security/jwt.js";
export function trackingController(service) {
  const router = Router();
  router.get("/", async (req, res) => res.json(await service.list()));
  router.put("/:id/position", authenticate, role("DRIVER"), async (req, res) =>
    res.json(await service.position(req.params.id, req.user, req.body)),
  );
  router.put("/:id/occupancy", authenticate, role("DRIVER"), async (req, res) =>
    res.json(await service.occupancy(req.params.id, req.user, req.body)),
  );
  return router;
}
