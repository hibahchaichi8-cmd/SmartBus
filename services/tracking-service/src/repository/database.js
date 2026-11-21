import { MongoClient } from "mongodb";
const client = new MongoClient(
  process.env.MONGODB_URI || "mongodb://localhost:27019/smartbus_tracking",
  { serverSelectionTimeoutMS: 5000 },
);
export const db = client.db();
export async function connect() {
  await client.connect();
  await db.collection("reports").createIndex({ tripId: 1 }, { unique: true });
}
export async function close() {
  await client.close();
}
