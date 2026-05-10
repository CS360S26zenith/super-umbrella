/**
 * Seed the Firestore `societies` collection (directory for Campus Societies UI).
 *
 * Same credentials setup as seed-events.js (serviceAccountKey.json or GOOGLE_APPLICATION_CREDENTIALS).
 *
 * Run: cd scripts && npm install && npm run seed:societies
 */

const path = require("path");
const admin = require("firebase-admin");

function parseArgs() {
  const args = process.argv.slice(2);
  const out = { key: process.env.GOOGLE_APPLICATION_CREDENTIALS };
  for (let i = 0; i < args.length; i++) {
    if (args[i] === "--key" && args[i + 1]) out.key = args[++i];
  }
  return out;
}

/** Fixed document IDs keep URLs/bookmarks stable when re-running the script. */
const SOCIETIES = [
  { id: "soc_les", name: "LUMS Entrepreneurial Society", acronym: "LES", accentIndex: 0 },
  { id: "soc_lms", name: "LUMS Music Society", acronym: "LMS", accentIndex: 1 },
  { id: "soc_lcss", name: "LUMS Community Service Society", acronym: "LCSS", accentIndex: 2 },
  { id: "soc_photolums", name: "LUMS Photography Society", acronym: "PhotoLUMS", accentIndex: 3 },
  { id: "soc_lmas", name: "LUMS Media Arts Society", acronym: "", accentIndex: 4 },
  { id: "soc_lumun", name: "LUMS Model United Nations Society", acronym: "LUMUN", accentIndex: 5 },
  { id: "soc_lit", name: "LUMS Literary Society", acronym: "", accentIndex: 6 },
  { id: "soc_drums", name: "Dramatics Society of LUMS", acronym: "DRUMS", accentIndex: 7 },
  { id: "soc_index", name: "Design Innovation Society of LUMS", acronym: "INDEX", accentIndex: 0 },
  { id: "soc_religious", name: "LUMS Religious Society", acronym: "", accentIndex: 2 },
];

async function main() {
  const { key } = parseArgs();

  const keyPath = key
    ? path.isAbsolute(key)
      ? key
      : path.join(__dirname, key)
    : path.join(__dirname, "serviceAccountKey.json");

  try {
    const serviceAccount = require(keyPath);
    admin.initializeApp({ credential: admin.credential.cert(serviceAccount) });
  } catch (e) {
    console.error("Could not load service account JSON from:", keyPath, "\n", e.message);
    process.exit(1);
  }

  const db = admin.firestore();
  const batch = db.batch();
  const col = db.collection("societies");

  SOCIETIES.forEach((s) => {
    const ref = col.doc(s.id);
    batch.set(ref, {
      name: s.name,
      acronym: s.acronym || "",
      accentIndex: s.accentIndex,
    });
  });

  await batch.commit();
  console.log("Seeded", SOCIETIES.length, "documents into collection \"societies\".");
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
