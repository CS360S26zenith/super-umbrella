# All user stories US-01–US-25: test plan and minimum accounts

This ties **Part 3** stories (`USER_STORY_BACKLOG.md`) and **Part 4** stories (`USER_STORY_BACKLOG_PART4.md`) into one ordered checklist. Use it when demoing or regressing the whole product.

---

## Story map (all 25)

| ID | Title | Part | Primary actor |
|----|--------|------|----------------|
| US-01 | Event browsing | 3 | Student |
| US-02 | Event search | 3 | Student |
| US-03 | Category filtering | 3 | Student |
| US-04 | Event details | 3 | Student |
| US-05 | Personalized recommendations | 4 | Student |
| US-06 | RSVP | 3 | Student |
| US-07 | Cancel RSVP | 3 | Student |
| US-08 | Capacity display | 3 | Student |
| US-09 | Waitlist (full events) | 4 | Student |
| US-10 | Add RSVPed event to device calendar | 4 | Student |
| US-11 | My Calendar (RSVP schedule) | 4 | Student |
| US-12 | Notifications on update/cancel | 4 | Student (+ Organizer) |
| US-13 | Reminder / RSVP confirmation flows | 4 | Student |
| US-14 | Create event | 3 | Organizer |
| US-15 | Edit / delete event | 3 | Organizer |
| US-16 | Organizer cancels + notifies attendees | 4 | Organizer (+ Student) |
| US-17 | Organizer dashboard | 3 | Organizer |
| US-18 | Export attendee list | 4 | Organizer |
| US-19 | Broadcast to attendees | 4 | Organizer |
| US-20 | Capacity enforcement | 3 | Student |
| US-21 | QR ticket | 4 | Student |
| US-22 | Staff approve / reject drafts | 4 | Staff |
| US-23 | Platform analytics | 4 | Staff |
| US-24 | Firebase Auth (login/register) | 3 | Any |
| US-25 | Role-based routing after login | 3 | Any |

---

## Bare minimum number of accounts

You need **three** distinct Firebase Auth users with matching **`users/{uid}`** documents (`role` field):

| Account | Role in Firestore | Why required |
|---------|-------------------|---------------|
| **A — Organizer** | `organizer` | US-14/15/16/17/18/19; event ownership; cannot RSVP own events |
| **B — Student** | `student` | US-01–08, US-05–13, US-20–21 (browse/RSVP/recs/ticket/notifications as attendee) |
| **C — Staff** | `staff` | US-22 (approve/reject drafts); US-23 opened from staff screen |

**You cannot reduce below three** if you must demo **staff approval** and **analytics** with the current app wiring (`StaffApprovalActivity` → `AnalyticsActivity`).

### Optional fourth account (only if you want extra proof)

| Account | Role | Optional use |
|---------|------|----------------|
| **D — Student 2** | `student` | US-20 race-ish scenarios (two devices RSVP same nearly-full event); larger attendee lists for export/broadcast demos |

One student (**B**) is enough for recommendations that use **prior RSVP categories**: RSVP to events in category X, then run recommendations—the app loads **your** past RSVPs and boosts matching categories.

### Organizer vs Student for RSVP popularity

Recommendation ranking also weights **`rsvpCount`** on events. That uses **counts stored on event documents**, not “how many accounts RSVP’d.” So:

- Seed or create events with **different `rsvpCount` values** (Console or `scripts/seed-events.js`).
- **Student B** does **not** add RSVP-count signal unless students actually RSVP (each RSVP increments count). For pure popularity sorting tests, **seeding counts** is the fastest approach.

---

## Shared test data (set once)

1. **Many live events** (`status: live`, future `date`, correct `category` strings). Use **`scripts/seed-events.js`** or Console — see `docs/TESTING_SEEDING_AND_STORY_CHECKLIST.md`.
2. For **US-05** specifically, prepare:
   - Several events per category (**Talks**, **Sports**, **Clubs**, **Performances**).
   - Mixed **`rsvpCount`** on events so popularity differs from category tie-break.
   - As student **B**: RSVP to **two Talks** events → later tap **Recommendations** with **no chip**: Talks should rank higher via history.
   - Tap **Sports** chip → tap **Recommendations**: Sports pool uses chip preference.
3. **Draft pipeline for US-22**: Organizer **A** creates a **new** event (submission goes **draft** if approval flow is enabled). Staff **C** approves → becomes **live** for students.

---

## Execution plan — test stories **one by one**

Do sessions **logged in as the right user**. Between stories you may reuse data unless noted (“fresh RSVP”, etc.).

### Session S — Student (B)

| Step | US | Action | Pass when |
|------|-----|--------|-----------|
| S1 | 01 | Open Campus Events | Live events load; list scrolls |
| S2 | 02 | Type search keyword | Filters by title/description/venue |
| S3 | 03 | Tap category chips + **All** | Filter toggles correctly |
| S4 | 04 | Open one event | Full detail + capacity strip visible |
| S5 | 06 | RSVP future, non-full, **not** created by B | Success; RSVP persists |
| S6 | 08 | Compare list vs detail capacity text/bar | Consistent X/Y and % |
| S7 | 21 | **View Ticket** on RSVPed event | QR + payload shown |
| S8 | 07 | **Cancel RSVP** | Back to RSVP state; counts decrease |
| S9 | 20 | Open **full** event (`rsvpCount >= capacity`) | Cannot RSVP; full UX |
| S10 | 09 | On full event: **Join waitlist** | Position toast |
| S11 | 10 | After RSVP: **Add to Calendar** | Calendar intent populated |
| S12 | 11 | **My Calendar** | RSVPed events listed; empty state if none |
| S13 | 05 | First RSVP only to **Talks** (two events). Clear search chips → **Recommendations** | Talks biased vs plain popularity |
| S14 | 05 | Select **Sports** chip → **Recommendations** | Ranking uses Sports pool + prefs |
| S15 | 13 | RSVP again | In-app reminder row + tray behavior where permitted |
| S16 | 12 | *(needs Organizer/O)* After organizer edits/cancels/broadcasts event B attends | New rows in **Notifications** inbox |

### Session O — Organizer (A)

| Step | US | Action | Pass when |
|------|-----|--------|-----------|
| O1 | 24 | *(if testing register)* Register organizer | Profile doc exists *(or skip if users pre-created)* |
| O2 | 17 | Dashboard lists **your** events | FAB / list OK |
| O3 | 14 | Create event (future date) | Saved; draft vs live matches your policy |
| O4 | 15 | Edit event (non-destructive field) | Saves; attendee notify if implemented |
| O5 | 15 | Delete event via edit flow | Removes / exits as designed |
| O6 | 19 | **Broadcast** (pick event with RSVPs) | Sent count / inbox rows for B |
| O7 | 18 | **Export IDs** (pick event with RSVPs) | CSV text includes attendees |
| O8 | 16 | **Cancel event** on card | Status cancelled; attendees notified |

### Session T — Staff (C)

| Step | US | Action | Pass when |
|------|-----|--------|-----------|
| T1 | 22 | Open pending drafts | List loads |
| T2 | 22 | Approve one draft | Event becomes **live** for students |
| T3 | 22 | Reject another draft | Status **cancelled** (or rejected policy you documented) |
| T4 | 23 | **Platform Analytics** | KPI text + category bars load |

### Session X — Auth / routing (US-24, US-25)

| Step | US | Action | Pass when |
|------|-----|--------|-----------|
| X1 | 24 | Login valid / invalid | Success vs error |
| X2 | 24 | Register student *(optional duplicate email)* | Validation works |
| X3 | 25 | Login **B** | Routes to event list |
| X4 | 25 | Login **A** | Routes to organizer dashboard |
| X5 | 25 | Login **C** | Routes to staff approvals |

---

## Minimal timeline (single tester)

1. Pre-seed Firestore events + ensure three accounts exist.  
2. **Student session** S1–S16 (batch organizer-triggered stuff for US-12 after O6/O8).  
3. **Organizer session** O1–O8 (coordinate with student for notifications).  
4. **Staff session** T1–T4.  
5. **Auth** X1–X5 if not covered during registration.

---

## Related docs

- Firestore fields + bulk seed + unit vs instrumented tests:  
  `docs/TESTING_SEEDING_AND_STORY_CHECKLIST.md`
