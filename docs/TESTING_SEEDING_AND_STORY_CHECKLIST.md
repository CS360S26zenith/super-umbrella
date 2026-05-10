# Firestore bulk events, automated tests, and per–user-story manual plan

This doc explains how to **seed many events** in Firebase, what **tests exist** (unit vs instrumented), and a **step-by-step plan** to verify user stories **one at a time**.

For **all US-01–US-25** in one table plus **minimum accounts (organizer + student + staff)** and recommendation test data, see **[US01_US25_TEST_PLAN_AND_MIN_ACCOUNTS.md](US01_US25_TEST_PLAN_AND_MIN_ACCOUNTS.md)**.

---

## Part A — Event documents in Firestore (what the app expects)

Collection: **`events`** (auto-generated document IDs are fine).

Each document should have these fields (types matter in the Console):

| Field | Type | Notes |
|--------|------|--------|
| `title` | string | Shown on cards and detail |
| `description` | string | Detail screen |
| `venue` | string | Shown on cards |
| `category` | string | Must match app chips: **Talks**, **Sports**, **Clubs**, **Performances** |
| `capacity` | number | Max attendees |
| `rsvpCount` | number | Usually start at `0` unless you are simulating RSVPs |
| `organizerId` | string | **Real Firebase Auth UID** of a user whose role is **organizer** (Authentication → Users → UID) |
| `status` | string | **`live`** so students see it on Campus Events list (`EventListActivity` filters by `status == live`) |
| `date` | **timestamp** | **Future** date/time so RSVP is not blocked as “past event” |

**Important**

- Wrong `category` spelling breaks filters/chips.
- `organizerId` must be a valid organizer UID or Firestore rules may reject writes from the app.
- Students only load **live** events; **draft** events are for staff approval flow.

---

## Part B — Two ways to create many events

### Option 1 — Firebase Console (manual, good for learning)

1. Open **Firestore Database** → collection **`events`** → **Add document** → **Auto-ID**.
2. Add each field from the table above.
3. Repeat for as many events as you want (duplicate rows and edit title/date/category to exercise scroll + filters).

### Option 2 — Bulk seed script (recommended for many events)

Scripts live in **`scripts/`** in this repo.

1. **Service account key** (not your login password):  
   Firebase Console → **Project settings** → **Service accounts** → **Generate new private key**.  
   Save as **`scripts/serviceAccountKey.json`** (this path is gitignored; never commit it).

2. **Organizer UID**: Firebase Console → **Authentication** → **Users** → copy the **UID** of an organizer account.

3. From a terminal:

   ```bat
   cd scripts
   npm install
   node seed-events.js --organizer YOUR_ORGANIZER_UID_HERE
   ```

   If the key file is elsewhere:

   ```bat
   node seed-events.js --organizer YOUR_ORGANIZER_UID --key "C:\path\to\key.json"
   ```

The script creates multiple **`live`** events with varied categories for list scroll and chip testing.

---

## Part C — Automated tests in this project

### Unit tests (JVM, fast, no device)

- **Location:** `app/src/test/java/`
- **What they do:** Run on your computer’s JVM; test model/logic (e.g. `Event`, `User`, `RSVP`, `RecommendationService`).
- **Run:**

  ```bat
  .\gradlew.bat test
  ```

### Instrumented / Android tests (device or emulator)

- **Location:** `app/src/androidTest/java/`
- **What they do:** Install a debug build on a **connected device or emulator** and drive the UI or Firebase-backed flows.
- **Includes:**
  - **`AppSmokeTest`** — launches login screen; checks basic ticket/error-path UI without full Firebase happy path.
  - **`AuthenticatedRsvpFlowTest`** — signs in with **fixture credentials** and exercises RSVP-oriented flows **only if** you configure Gradle properties (see below).
- **Run:**

  ```bat
  adb devices
  .\gradlew.bat connectedAndroidTest
  ```

**Fixture credentials for `AuthenticatedRsvpFlowTest`**

Set these so the test does not skip (project reads them via `build.gradle` → `BuildConfig`):

- Prefer **user-level** `gradle.properties`: `C:\Users\<you>\.gradle\gradle.properties`

  ```properties
  TEST_STUDENT_EMAIL=student@example.com
  TEST_STUDENT_PASSWORD=yourpassword
  TEST_EVENT_ID=FirestoreDocumentIdOfALiveFutureEvent
  ```

- **`TEST_EVENT_ID`**: Firestore → **`events`** → open a **live**, **future**, **not full** event → copy **document ID**.

**Test types summary**

| Kind | Folder | Needs device? | Typical use |
|------|--------|----------------|-------------|
| Unit | `app/src/test/` | No | Models, pure ranking logic |
| Instrumented | `app/src/androidTest/` | Yes | UI smoke, signed-in flows |

---

## Part D — Manual plan: test user stories **individually**

Use **three accounts** in Firebase Auth + matching **`users/{uid}`** docs with roles **`student`**, **`organizer`**, **`staff`**. Seed **several live events** (Console or script), then follow one story at a time.

Legend: **Prep** = data/setup before the step. **Pass** = expected result.

### Student — discovery & RSVP (Part 3 baseline, still core demo)

| Order | Story | Prep | Steps | Pass |
|-------|--------|------|--------|------|
| D1 | US-01 Browse | Several `live` events | Login as student → Campus Events list | List shows events; scroll works |
| D2 | US-02 Search | Events with distinct titles | Type keyword in search | List filters by title/description/venue |
| D3 | US-03 Filter | Mix of categories | Tap chips (Talks, Sports, …) | Only matching category |
| D4 | US-04 Detail | — | Tap one event card | Detail shows title, date, venue, capacity |
| D5 | US-06 RSVP | Future event, not full, not your own event | Tap RSVP | Success toast; capacity updates on revisit |
| D6 | US-07 Cancel RSVP | After D5 | Tap Cancel RSVP | RSVP cleared; capacity decreases |
| D7 | US-08 / US-20 Capacity | One full event (`rsvpCount >= capacity`) | Open full event | RSVP disabled / full messaging; waitlist visible per app behavior |

### Student — Part 4 behaviors

| Order | Story | Prep | Steps | Pass |
|-------|--------|------|--------|------|
| D8 | US-05 Recommendations | Mixed events; optional past RSVPs | Tap **Recommendations** | Order changes; category chip or history affects ranking |
| D9 | US-09 Waitlist | Full event | Open full event → Join waitlist | Toast with queue position |
| D10 | US-10 Calendar export | RSVPed event | **Add to Calendar** | Calendar intent opens with prefilled fields |
| D11 | US-11 My Calendar | At least one RSVP | **My Calendar** | Timeline lists RSVPed events; empty state if none |
| D12 | US-12 / US-13 Notifications | Organizer updates/cancels or you RSVP | **Notifications (Inbox)** | Rows appear; RSVP shows reminder-style row where implemented |
| D13 | US-21 Ticket / QR | Confirmed RSVP | On detail → **View Ticket** | QR image + payload text |

### Organizer

| Order | Story | Prep | Steps | Pass |
|-------|--------|------|--------|------|
| O1 | US-14 Create | Organizer login | Dashboard → create event | Event saved as draft if approval flow enabled; appears for organizer |
| O2 | US-15 Edit | Own event | Edit fields → save | Firestore updates; attendees notified if implemented |
| O3 | US-16 Cancel + notify | RSVPs on event | Cancel event from dashboard card | Attendees get inbox notifications |
| O4 | US-18 Export | RSVPs on event | **Export IDs** → pick event | Share sheet CSV text includes attendees |
| O5 | US-19 Broadcast | RSVPs on event | Broadcast (from card or dashboard flow) | Attendees receive inbox notifications |

### Staff

| Order | Story | Prep | Steps | Pass |
|-------|--------|------|--------|------|
| S1 | US-22 Approve/Reject | Draft events (`status: draft`) | Staff login → pending list | Approve → `live`; reject → `cancelled` (per app rules) |
| S2 | US-23 Analytics | Any mix of data | Open **Platform Analytics** | Summary + category bars load |

### Cross-cutting / registration

| Order | Story | Prep | Steps | Pass |
|-------|--------|------|--------|------|
| X1 | US-24 Register | — | Register student vs organizer | Profile row in `users`; login works |
| X2 | US-25 Role routing | Users with different roles | Login each | Routes to student list / organizer dashboard / staff |

---

## Part E — Suggested order for a demo rehearsal

1. Seed events (script or Console).  
2. Run **`.\gradlew.bat test`** (unit).  
3. Run **`connectedAndroidTest`** on device/emulator if fixtures configured.  
4. Walk the manual table **D1 → D13**, then **O1 → O5**, then **S1 → S2**.

This keeps each story isolated: reset data (new account or new event) when a story needs a clean slate.
