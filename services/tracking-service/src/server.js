import { app } from "./app.js";
import { config } from "./config.js";
import { connect, close } from "./repository/database.js";
await connect();
const server = app.listen(config.port, "0.0.0.0", () =>
  console.log(`Service listening on ${config.port}`),
);
process.on("SIGTERM", () =>
  server.close(async () => {
    await close();
    process.exit(0);
  }),
);
