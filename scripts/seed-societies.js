/**
 * Seed the Firestore `societies` collection with real LUMS societies.
 *
 * Prerequisites:
 *   Place your Firebase service account JSON at scripts/serviceAccountKey.json
 *   OR set env: GOOGLE_APPLICATION_CREDENTIALS=full/path/to/key.json
 *
 * Run:
 *   cd scripts && npm install && npm run seed:societies
 * Or:
 *   node seed-societies.js --key ./serviceAccountKey.json
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
  { id: "soc_les",       name: "LUMS Entrepreneurial Society",                   acronym: "LES",       accentIndex: 0 },
  { id: "soc_lumun",     name: "LUMS Model United Nations Society",               acronym: "LUMUN",     accentIndex: 1 },
  { id: "soc_drums",     name: "Debates and Recitations at LUMS",                 acronym: "DRUMS",     accentIndex: 2 },
  { id: "soc_dramaline", name: "LUMS Dramatics Society",                          acronym: "Dramaline", accentIndex: 3 },
  { id: "soc_index",     name: "Design Innovation Society of LUMS",               acronym: "INDEX",     accentIndex: 4 },
  { id: "soc_lms",       name: "Music Society of LUMS",                           acronym: "LMS",       accentIndex: 5 },
  { id: "soc_lcss",      name: "LUMS Community Service Society",                  acronym: "LCSS",      accentIndex: 6 },
  { id: "soc_fintra",    name: "LUMS Finance Society",                             acronym: "FINTRA",    accentIndex: 7 },
  { id: "soc_spades",    name: "Society for Promotion and Development of Engineering and Sciences", acronym: "SPADES", accentIndex: 0 },
  { id: "soc_lwic",      name: "LUMS Women in Computing",                         acronym: "LWiC",      accentIndex: 1 },
  { id: "soc_lsms",      name: "LUMS Students Mathematics Society",               acronym: "LSMS",      accentIndex: 2 },
  { id: "soc_lma",       name: "LUMS Media Arts Society",                         acronym: "LMA",       accentIndex: 3 },
  { id: "soc_femsoc",    name: "Feminist Society LUMS",                           acronym: "FEMSOC",    accentIndex: 4 },
  { id: "soc_lcg",       name: "LUMS Consultancy Group",                          acronym: "LCG",       accentIndex: 5 },
  { id: "soc_lpri",      name: "LUMS Policy Research Institute",                  acronym: "LPRI",      accentIndex: 6 },
  { id: "soc_photolums", name: "LUMS Photography Society",                        acronym: "PhotoLUMS", accentIndex: 7 },
  { id: "soc_lspa",      name: "LUMS Society of Professional Accountancy",        acronym: "LSPA",      accentIndex: 0 },
  { id: "soc_lscse",     name: "LUMS Society of Chemical Sciences and Engineering", acronym: "LSCSE",   accentIndex: 1 },
  { id: "soc_lit",       name: "LUMS Literary Society",                           acronym: "",          accentIndex: 2 },
  { id: "soc_aiesec",    name: "AIESEC LUMS Chapter",                             acronym: "AIESEC",    accentIndex: 3 },
  { id: "soc_ieee",      name: "IEEE LUMS Student Chapter",                       acronym: "IEEE",      accentIndex: 4 },
  { id: "soc_dancelums", name: "DANCELUMS",                                       acronym: "",          accentIndex: 5 },
  { id: "soc_lems",      name: "LUMS Emergency Medical Services",                 acronym: "LEMS",      accentIndex: 6 },
  { id: "soc_ldss",      name: "LUMS Data Science Society",                       acronym: "LDSS",      accentIndex: 7 },
  { id: "soc_chess",     name: "LUMS Chess Club",                                 acronym: "",          accentIndex: 0 },
  { id: "soc_arts",      name: "LUMS Arts Society",                               acronym: "",          accentIndex: 1 },
  { id: "soc_rizq",      name: "Rizq LUMS Society",                               acronym: "Rizq",      accentIndex: 2 },
  { id: "soc_laps",      name: "LUMS Law and Politics Society",                   acronym: "LAPS",      accentIndex: 3 },
];

async function main() {
  const { key } = parseArgs();

  const keyPath = key
    ? path.isAbsolute(key) ? key : path.join(__dirname, key)
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
  console.log(`Seeded ${SOCIETIES.length} documents into collection "societies".`);
  SOCIETIES.forEach((s) => console.log(`  ${s.id.padEnd(16)} ${s.acronym ? "(" + s.acronym + ")" : "       "} ${s.name}`));
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
