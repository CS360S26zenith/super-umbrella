# Campus Events — Project Context & Operations Guide

Use this document to onboard teammates, resume work after a break, or prepare demos/submissions.

---

## 1. What This App Is

- **Platform:** Android (Java), minSdk 24, Material3 **DayNight** theme.
- **Backend:** Firebase **Authentication** (email/password) + **Cloud Firestore**.
- **Package:** `com.example.campuseventstest`
- **Users:** Students (browse/RSVP), Organizers (create/manage events), Staff (approval/analytics — role-dependent).

---

## 2. Repository Landmarks

| Topic | Location |
|--------|-----------|
| Firestore security rules (copy into Firebase Console) | `firestore.rules` (project root) |
| User-facing backlog / stories reference | `USER_STORY_BACKLOG.md`, full spec in your course doc |
| Student event list & recommendations | `EventListActivity.java`, `activity_event_list.xml` |
| Event detail, RSVP, waitlist, ticket entry | `EventDetailActivity.java`, `activity_event_detail.xml` |
| Organizer dashboard & per-event broadcast | `OrganizerDashboardActivity.java`, `OrganizerEventAdapter.java`, `item_organizer_event.xml` |
| Create / edit events (forms) | `CreateEventActivity.java`, `EditEventActivity.java`, `activity_create_event.xml`, `activity_edit_event.xml` |
| Notifications inbox & tray alerts | `NotificationsActivity.java`, `NotificationHelper.java`, `AndroidManifest.xml` (`POST_NOTIFICATIONS`) |
| Firestore API surface | `FirestoreService.java` |
| Constants (collections, roles, extras) | `utils/Constants.java` |

---

## 3. Firestore Collections (Conceptual Schema)

| Collection | Purpose |
|-------------|---------|
| `users/{uid}` | Profile: `uid`, `name`, `email`, `role` (`student` \| `organizer` \| `staff`) |
| `events/{eventId}` | Event fields incl. `organizerId`, `status` (`live` \| `draft` \| `cancelled`), `rsvpCount`, `capacity`, etc. |
| `rsvps/{rsvpId}` | `studentId`, `eventId`, `status` (`confirmed` \| `cancelled`), `timestamp` |
| `waitlist/{id}` | Part 4 — queue when full: `studentId`, `eventId`, `position`, `timestamp` |
| `tickets/{id}` | Part 4 — ticket row: `rsvpId`, `eventId`, `studentId`, `qrCode`, `used` |
| `notifications/{id}` | Part 4 — inbox rows: `userId`, `eventId`, `title`, `message`, `type`, `timestamp` |

**Student browse query** uses **`status == live`**. Events saved as **`draft`** do **not** appear on the student home list until someone sets them to **`live`**.

---

## 4. Security Rules — Where & How

1. Open [Firebase Console](https://console.firebase.google.com) → your project → **Firestore Database** → **Rules**.
2. Paste the contents of **`firestore.rules`** from this repo (or merge carefully with existing rules).
3. Click **Publish**.

Without rules for **`waitlist`**, **`tickets`**, and **`notifications`**, students will see **`PERMISSION_DENIED`** for those features.

**Course note:** `notifications` uses `allow create: if request.auth != null` — fine for a trusted class demo; tighten for production (e.g. Cloud Functions only).

---

## 5. Publishing New Events (Organizer)

- **Current behavior:** Creating an event uses the default **`Event`** constructor path so **`status` is `live`** after save (student list shows it).
- If older events were saved as **`draft`** (from an earlier “staff approval” experiment), either:
  - Change **`status`** to **`live`** in the Firestore console for those documents, or  
  - Re-save from the app after editing.

---

## 6. Broadcast Messaging — Avoid “0 attendees”

- Attendee lookup is keyed by **Firestore document ID** of the event (`events/{eventId}`), **not** by event title.
- **Recommended:** On the organizer dashboard, use **“Broadcast to attendees”** on the specific event card — it passes **`eventId`** (and title for the screen) into **`BroadcastMessageActivity`** automatically.
- The generic **Broadcast** button in the dashboard header still requires pasting the **correct document ID** manually.

---

## 7. Tickets — What Users See

- **View Ticket** shows a **text “QR payload”** (e.g. `ticket:<eventId>:<uid>:<rsvpId>`). That is intentional for coursework unless you add a barcode library to render a bitmap.

---

## 8. Notifications — Two Channels

1. **Firestore `notifications`** — persisted rows for an inbox (`NotificationsActivity`, opened from **“Notifications (Inbox)”** on the student list).
2. **Android tray** — short heads-up after RSVP if **`POST_NOTIFICATIONS`** is granted (Android 13+).

Organizer-driven updates do **not** automatically wake the student phone unless you add **FCM** later; students still see rows in the **inbox** after sync.

---

## 9. UI / Theme Gotchas

- Theme is **DayNight**. Do **not** rely on fixed colors like `#212121` on surfaces — use **`?attr/colorOnSurface`** / **`?attr/colorOnSurfaceVariant`** so text stays visible in light and dark mode.
- Create/edit forms use theme-aware background (`?android:attr/colorBackground`) and explicit **text + hint** colors.

---

## 10. Build & Run

- Place **`google-services.json`** in `app/` (not committed if `.gitignore` excludes it).
- Enable **Email/Password** in Firebase Authentication.
- Open project in **Android Studio**, sync Gradle, run on device/emulator.

---

## 11. Quick Troubleshooting

| Symptom | Likely cause |
|---------|----------------|
| `PERMISSION_DENIED` on waitlist/tickets/notifications | Rules not published or outdated — update from `firestore.rules` |
| New event not visible to students | Event **`status`** is not **`live`** |
| Broadcast → 0 attendees | Wrong id (title vs document id) — use **Broadcast on event card** |
| Invisible labels/hints on forms | Fixed in layouts using theme attrs — rebuild |
| Composite index popup (Firestore) | Follow link in Logcat to create index (e.g. waitlist ordering) |

---

## 12. Document Maintenance

- **Last consolidated:** course iteration / Part 4 implementation pass.
- Update this file when you change rules, default event status, or major flows.
