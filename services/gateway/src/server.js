import { app } from "./app.js";
import { config } from "./config.js";
const server = app.listen(config.port, "0.0.0.0", () =>
  console.log(`Service listening on ${config.port}`),
);
process.on("SIGTERM", () =>
  server.close(async () => {
    process.exit(0);
  }),
);
