// Small single-instance limiter. Use a shared store for multiple gateway replicas.
const clients = new Map();
export function rateLimit(req, res, next) {
  const now = Date.now();
  if (clients.size > 10000)
    for (const [key, value] of clients)
      if (value.until < now) clients.delete(key);
  const auth = req.path.includes("/auth/");
  const key = req.ip + (auth ? ":auth" : ":api");
  let current = clients.get(key);
  if (!current || current.until < now) {
    current = { count: 0, until: now + 60000 };
    clients.set(key, current);
  }
  if (++current.count > (auth ? 20 : 600))
    return res
      .status(429)
      .json({ status: 429, message: "Too many requests. Wait one minute." });
  next();
}
