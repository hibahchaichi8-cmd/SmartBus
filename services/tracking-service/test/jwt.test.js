import test from "node:test";
import assert from "node:assert/strict";
import { createServer } from "node:http";
import { generateKeyPair, exportJWK, SignJWT } from "jose";
test("RS256 verification enforces signature, expiry, audience and role claims", async () => {
  const pair = await generateKeyPair("RS256");
  const jwk = await exportJWK(pair.publicKey);
  jwk.kid = "qa";
  const server = createServer((req, res) => {
    res.setHeader("content-type", "application/json");
    res.end(JSON.stringify({ keys: [jwk] }));
  });
  server.listen(0, "127.0.0.1");
  await new Promise((r) => server.once("listening", r));
  process.env.AUTH_URL = "http://127.0.0.1:" + server.address().port;
  const { authenticate } = await import("../src/security/jwt.js");
  const token = async (overrides = {}) =>
    new SignJWT({
      roles: ["DRIVER"],
      sub: "driver",
      iss: "smartbus-auth",
      aud: "smartbus-api",
      iat: Math.floor(Date.now() / 1000),
      exp: Math.floor(Date.now() / 1000) + 60,
      ...overrides,
    })
      .setProtectedHeader({ alg: "RS256", kid: "qa" })
      .sign(pair.privateKey);
  const req = (token) => ({ get: () => "Bearer " + token });
  try {
    let next = false;
    const request = req(await token());
    await authenticate(request, {}, () => (next = true));
    assert.equal(next, true);
    assert.equal(request.user.id, "driver");
    for (const claims of [
      { exp: Math.floor(Date.now() / 1000) - 120 },
      { aud: "wrong" },
      { roles: ["ROOT"] },
      { roles: undefined },
    ])
      await assert.rejects(
        () => token(claims).then((t) => authenticate(req(t), {}, () => {})),
        { status: 401 },
      );
    const good = await token();
    const altered =
      good.slice(0, -20) + (good.at(-20) === "A" ? "B" : "A") + good.slice(-19);
    await assert.rejects(() => authenticate(req(altered), {}, () => {}), {
      status: 401,
    });
  } finally {
    await new Promise((r) => server.close(r));
  }
});
