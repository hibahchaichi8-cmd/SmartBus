import test from "node:test";
import assert from "node:assert/strict";
process.env.INTERNAL_TOKEN = "test-only-internal-token-32-characters";
const { app } = await import("../src/app.js");
test("gateway rejects internal paths and missing bearer tokens", async () => {
  const server = app.listen(0, "127.0.0.1");
  await new Promise((r) => server.once("listening", r));
  const base = "http://127.0.0.1:" + server.address().port;
  try {
    assert.equal((await fetch(base + "/internal/trips/active")).status, 404);
    assert.equal((await fetch(base + "/api/v1/drivers")).status, 401);
    assert.equal(
      (
        await fetch(base + "/api/v1/buses", {
          method: "POST",
          headers: { "content-type": "application/json", "X-Role": "ADMIN" },
          body: "{}",
        })
      ).status,
      401,
    );
    assert.equal(
      (
        await fetch(base + "/api/v1/auth/login", {
          method: "POST",
          headers: { "content-type": "application/json" },
          body: "{",
        })
      ).status,
      400,
    );
  } finally {
    await new Promise((r) => server.close(r));
  }
});
