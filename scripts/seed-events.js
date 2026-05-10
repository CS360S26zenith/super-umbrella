/**
 * Bulk-create events in Firestore for UI / filter / scroll testing.
 *
 * Prerequisites:
 * 1. Firebase Console → Project settings → Service accounts → Generate new private key
 *    Save JSON as scripts/serviceAccountKey.json (never commit it; see .gitignore)
 *    OR set env: GOOGLE_APPLICATION_CREDENTIALS=full/path/to/key.json
 * 2. organizerId must be a real Firebase Auth UID for an organizer in your project
 *    (Firebase Console → Authentication → Users, copy UID)
 *
 * Run from repo root:
 *   cd scripts && npm install && ORGANIZER_UID=yourOrganizerUid npm run seed
 * Or:
 *   node seed-events.js --organizer YOUR_ORGANIZER_UID --key ./serviceAccountKey.json
 */

const path = require("path");
const admin = require("firebase-admin");

function parseArgs() {
  const args = process.argv.slice(2);
  const out = { organizer: process.env.ORGANIZER_UID, key: process.env.GOOGLE_APPLICATION_CREDENTIALS };
  for (let i = 0; i < args.length; i++) {
    if (args[i] === "--organizer" && args[i + 1]) out.organizer = args[++i];
    if (args[i] === "--key" && args[i + 1]) out.key = args[++i];
  }
  return out;
}

function ts(year, month0, day, hour, minute) {
  return admin.firestore.Timestamp.fromDate(new Date(year, month0, day, hour, minute, 0, 0));
}

/**
 * Society IDs must match documents seeded by seed-societies.js.
 * Each entry: [societyId, societyName]
 */
const SOCIETY_MAP = {
  soc_les:       "LUMS Entrepreneurial Society",
  soc_lumun:     "LUMS Model United Nations Society",
  soc_drums:     "Debates and Recitations at LUMS",
  soc_dramaline: "LUMS Dramatics Society",
  soc_index:     "Design Innovation Society of LUMS",
  soc_lms:       "Music Society of LUMS",
  soc_lcss:      "LUMS Community Service Society",
  soc_fintra:    "LUMS Finance Society",
  soc_spades:    "Society for Promotion and Development of Engineering and Sciences",
  soc_lwic:      "LUMS Women in Computing",
  soc_lsms:      "LUMS Students Mathematics Society",
  soc_lma:       "LUMS Media Arts Society",
  soc_femsoc:    "Feminist Society LUMS",
  soc_lcg:       "LUMS Consultancy Group",
  soc_lpri:      "LUMS Policy Research Institute",
  soc_photolums: "LUMS Photography Society",
  soc_lspa:      "LUMS Society of Professional Accountancy",
  soc_lscse:     "LUMS Society of Chemical Sciences and Engineering",
  soc_lit:       "LUMS Literary Society",
  soc_aiesec:    "AIESEC LUMS Chapter",
  soc_ieee:      "IEEE LUMS Student Chapter",
  soc_dancelums: "DANCELUMS",
  soc_lems:      "LUMS Emergency Medical Services",
  soc_ldss:      "LUMS Data Science Society",
  soc_chess:     "LUMS Chess Club",
  soc_arts:      "LUMS Arts Society",
  soc_rizq:      "Rizq LUMS Society",
  soc_laps:      "LUMS Law and Politics Society",
};

/** Matches Constants.java categories exactly */
function seedDocs(organizerId) {
  const categories = ["Talks", "Sports", "Clubs", "Performances"];

  // [title, description, venue, capacity, societyId]
  const base = [
    ["Basketball Open Gym",           "Drop-in basketball. Bring indoor shoes.",                       "Sports Complex",           40,  "soc_lems"],
    ["Tech Talk: AI in Education",    "Panel on responsible AI tools for learning.",                   "Room 101, Engineering",    50,  "soc_spades"],
    ["Entrepreneurship Club Meetup",  "Pitch night and networking.",                                   "SDSB Seminar Room",        50,  "soc_les"],
    ["Spring Concert",                "Student bands and soloists perform live.",                      "Main Auditorium",         200,  "soc_lms"],
    ["Career Fair Prep Workshop",     "Resume reviews and mock interviews.",                           "Career Center",            80,  "soc_lcg"],
    ["Film Night: Classic Cinema",    "Screening and discussion of a classic film.",                   "Media Lab",                60,  "soc_lma"],
    ["Hackathon Kickoff",             "48-hour build weekend opening ceremony.",                       "CS Building Atrium",      120,  "soc_spades"],
    ["Yoga on the Lawn",              "Beginner-friendly session; bring a mat.",                       "Central Quad",             35,  "soc_lcss"],
    ["Debate Society: Climate Policy","Open audience Q&A on climate legislation.",                     "Law School Moot Court",    45,  "soc_drums"],
    ["Robotics Demo Day",             "See student-built robots in action.",                           "Maker Space",              70,  "soc_ieee"],
    ["Food Festival",                 "Campus vendors and student clubs serve food.",                  "South Plaza",             300,  "soc_aiesec"],
    ["Poetry Slam",                   "Open mic; sign-ups at the door.",                              "Coffee House",             55,  "soc_lit"],
    ["Women in STEM Panel",           "Alumni stories and mentorship opportunities.",                  "Lecture Hall A",           90,  "soc_lwic"],
    ["Charity Run 5K",                "Registration starts 7 AM at the athletics track.",             "Athletics Track",         150,  "soc_rizq"],
    ["Chess Blitz Tournament",        "Rapid rounds; prizes for top 3 players.",                      "Library Basement",         32,  "soc_chess"],
    ["Photography Walk",              "Campus architecture tour with tips from photographers.",        "Arts Building Steps",      25,  "soc_photolums"],
    ["Green Campus Workshop",         "Sustainability projects and environmental action on campus.",   "Env Lab",                  40,  "soc_lcss"],
    ["Jazz Ensemble",                 "An evening of live jazz performances.",                         "Music Hall",              100,  "soc_lms"],
    ["Study Skills Workshop",         "Time management and study strategies for finals.",              "Student Success Center",   65,  "soc_aiesec"],
    ["Startup Founder AMA",           "Q&A session with a local startup founder.",                    "Innovation Hub",           75,  "soc_les"],
    ["International Culture Night",   "Performances, dances, and food stalls from around the world.", "Great Hall",              250,  "soc_dancelums"],
    ["Esports Friendly",              "Casual matches across all skill levels.",                      "Gaming Lounge",            48,  "soc_ldss"],
    ["Public Lecture: Astronomy",     "Latest discoveries with a live telescope demo.",               "Observatory",              85,  "soc_lscse"],
    ["Volunteer Day: River Cleanup",  "Meet at bus loop; gear provided.",                             "Bus Loop",                 60,  "soc_rizq"],
  ];

  const docs = [];
  base.forEach((row, i) => {
    const [title, description, venue, capacity, societyId] = row;
    const category = categories[i % categories.length];
    const day = 5 + (i % 22);
    const hour = 10 + (i % 8);
    const minute = (i * 7) % 60;
    const month = 4 + Math.floor(i / 8); // May, Jun, Jul spread
    const date = ts(2026, Math.min(month, 11), day, hour, minute);
    docs.push({
      title,
      description,
      date,
      venue,
      category,
      capacity,
      rsvpCount: i % 5 === 0 ? Math.min(3, capacity - 1) : Math.min(i % 7, capacity - 1),
      organizerId,
      status: "live",
      societyId,
      societyName: SOCIETY_MAP[societyId],
    });
  });
  return docs;
}

async function main() {
  const { organizer, key } = parseArgs();
  if (!organizer || !organizer.trim()) {
    console.error("Missing organizer UID. Set ORGANIZER_UID or pass --organizer <uid>");
    process.exit(1);
  }

  const keyPath = key
    ? path.isAbsolute(key) ? key : path.join(__dirname, key)
    : path.join(__dirname, "serviceAccountKey.json");

  try {
    const serviceAccount = require(keyPath);
    admin.initializeApp({ credential: admin.credential.cert(serviceAccount) });
  } catch (e) {
    console.error(
      "Could not load service account JSON from:", keyPath,
      "\nDownload from Firebase Console → Project settings → Service accounts.",
      "\nError:", e.message
    );
    process.exit(1);
  }

  const db = admin.firestore();
  const batch = db.batch();
  const col = db.collection("events");
  const docs = seedDocs(organizer.trim());

  docs.forEach((data) => {
    const ref = col.doc();
    batch.set(ref, data);
  });

  await batch.commit();
  console.log(`Seeded ${docs.length} events into collection "events" (status=live).`);
  docs.forEach((d) => console.log(`  [${d.category.padEnd(12)}] ${d.title.padEnd(35)} → ${d.societyName}`));
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
