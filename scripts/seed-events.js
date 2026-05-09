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

/** Matches Constants.java categories exactly */
function seedDocs(organizerId) {
  const categories = ["Talks", "Sports", "Clubs", "Performances"];
  const base = [
    ["Basketball Open Gym", "Drop-in basketball. Bring indoor shoes.", "Sports Complex", 40],
    ["Tech Talk: AI in Education", "Panel on responsible AI tools for learning.", "Room 101, Engineering Building", 50],
    ["Entrepreneurship Club Meetup", "Pitch night and networking.", "Redc", 50],
    ["Spring Concert", "Student bands and soloists.", "Main Auditorium", 200],
    ["Career Fair Prep", "Resume reviews and mock interviews.", "Career Center", 80],
    ["Film Night: Classic Cinema", "Screening + discussion.", "Media Lab", 60],
    ["Hackathon Kickoff", "48h build weekend opening ceremony.", "CS Building Atrium", 120],
    ["Yoga on the Lawn", "Beginner-friendly session; bring a mat.", "Central Quad", 35],
    ["Debate Society: Climate Policy", "Open audience Q&A.", "Law School Moot Court", 45],
    ["Robotics Demo Day", "See student-built robots.", "Maker Space", 70],
    ["Food Festival", "Campus vendors and student clubs.", "South Plaza", 300],
    ["Poetry Slam", "Open mic; sign-ups at door.", "Coffee House", 55],
    ["Women in STEM Panel", "Alumni stories and mentorship.", "Lecture Hall A", 90],
    ["Charity Run 5K", "Registration starts 7am.", "Athletics Track", 150],
    ["Chess Blitz Tournament", "Rapid rounds; prizes for top 3.", "Library Basement", 32],
    ["Photography Walk", "Campus architecture tour.", "Arts Building Steps", 25],
    ["Green Campus Workshop", "Sustainability projects on campus.", "Env Lab", 40],
    ["Jazz Ensemble", "Evening performance.", "Music Hall", 100],
    ["Study Skills Workshop", "Time management for finals.", "Student Success Center", 65],
    ["Startup Founder AMA", "Q&A with a local founder.", "Innovation Hub", 75],
    ["International Culture Night", "Performances and food stalls.", "Great Hall", 250],
    ["Esports Friendly", "Casual matches; all skill levels.", "Gaming Lounge", 48],
    ["Public Lecture: Astronomy", "Latest discoveries; telescope demo if clear.", "Observatory", 85],
    ["Volunteer Day: River Cleanup", "Meet at bus loop; gear provided.", "Bus Loop", 60],
  ];

  const docs = [];
  base.forEach((row, i) => {
    const [title, description, venue, capacity] = row;
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
    ? path.isAbsolute(key)
      ? key
      : path.join(__dirname, key)
    : path.join(__dirname, "serviceAccountKey.json");

  try {
    // eslint-disable-next-line import/no-dynamic-require, global-require
    const serviceAccount = require(keyPath);
    admin.initializeApp({ credential: admin.credential.cert(serviceAccount) });
  } catch (e) {
    console.error(
      "Could not load service account JSON from:",
      keyPath,
      "\nDownload from Firebase Console → Project settings → Service accounts.",
      "\nError:",
      e.message
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
  console.log("Seeded", docs.length, "events into collection \"events\" (status=live).");
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
