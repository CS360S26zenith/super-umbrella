# Campus Event Discovery Platform - User Story Backlog (Part 4)

**Project:** Campus Event Discovery and Management Platform  
**Sprint/Checkpoint:** Part 4 - Final Project Completion  
**Team:** zenith  
**Date:** [Current Date]

---

## Source Alignment

This file is aligned against:
- Original complete product backlog draft (`product_backlog_campus_events_part2.html`, US-01 to US-25).
- Submitted Part 3 backlog (`USER_STORY_BACKLOG.md`), which covered 13 stories.

Therefore, this Part 4 backlog contains the remaining product stories not covered in Part 3:
**US-05, US-09, US-10, US-11, US-12, US-13, US-16, US-18, US-19, US-21, US-22, US-23**.

---

## PART 4 USER STORIES (Remaining from Product Backlog)

### US-05: Personalized Recommendations
**Status:** PARTIAL (basic heuristic only)  
**Priority:** High  
**Story Points:** 13

**Story:**  
As a student, I want to see personalized event recommendations based on my interests and past RSVPs so that I discover events I would otherwise miss.

**Acceptance Criteria:**
- Recommendations rank events by relevance.
- Selected category chip drives preference.
- With no chip selected, past RSVP categories are used.
- With no prior RSVP history, ranking falls back to popularity (`rsvpCount`).

**Implementation Evidence:**
- `app/src/main/java/view/EventListActivity.java`
- `app/src/main/java/service/RecommendationService.java`

---

### US-09: Join Waitlist for Full Events
**Status:** COMPLETE  
**Priority:** Medium  
**Story Points:** 5

**Story:**  
As a student, I want to join a waitlist when an event is full so that I'm notified if a spot opens up.

**Acceptance Criteria:**
- Waitlist action is available when event is full.
- Joining waitlist writes a waitlist document with queue position.
- Student gets a success message containing position.

**Implementation Evidence:**
- `app/src/main/java/view/EventDetailActivity.java`
- `app/src/main/java/service/FirestoreService.java`
- `app/src/main/java/model/Waitlist.java`

---

### US-10: Add RSVPed Event to Personal Calendar
**Status:** COMPLETE  
**Priority:** High  
**Story Points:** 8

**Story:**  
As a student, I want to add an RSVPed event to my personal calendar so that it appears in my existing schedule.

**Acceptance Criteria:**
- Event detail provides add-to-calendar action.
- App opens calendar insert intent prefilled with event details.

**Implementation Evidence:**
- `app/src/main/java/view/EventDetailActivity.java`
- `app/src/main/java/service/CalendarService.java`

---

### US-11: Calendar View for My RSVPed Events
**Status:** PARTIAL (list view, not full calendar UI)  
**Priority:** Medium  
**Story Points:** 5

**Story:**  
As a student, I want to view a calendar view of all events I've RSVPed to so that I can manage my schedule within the platform.

**Acceptance Criteria:**
- User can open a dedicated "My Calendar" screen.
- Screen lists confirmed RSVPed events with date/venue.
- Empty state is shown when no events are RSVPed.

**Implementation Evidence:**
- `app/src/main/java/view/MyCalendarActivity.java`
- `app/src/main/java/service/FirestoreService.java`

---

### US-12: Notification on Event Update/Cancel
**Status:** PARTIAL (in-app notifications only)  
**Priority:** High  
**Story Points:** 8

**Story:**  
As a student, I want to receive a notification when an event I've RSVPed to is updated or cancelled so that I'm always informed.

**Acceptance Criteria:**
- Organizer broadcast/cancel flows write notifications for attendee IDs.
- Student can view notifications in inbox screen.

**Implementation Evidence:**
- `app/src/main/java/service/NotificationService.java`
- `app/src/main/java/view/BroadcastMessageActivity.java`
- `app/src/main/java/view/OrganizerEventAdapter.java`
- `app/src/main/java/view/NotificationsActivity.java`

---

### US-13: Reminder Notification Before Event
**Status:** PARTIAL (confirmation/inbox flow, not true scheduled reminder)  
**Priority:** Medium  
**Story Points:** 5

**Story:**  
As a student, I want to receive a reminder notification before an event I've RSVPed to so that I don't forget to attend.

**Acceptance Criteria:**
- RSVP success creates reminder record in notifications collection.
- Notification appears in student inbox.
- App can show tray confirmation when permission is available.

**Implementation Evidence:**
- `app/src/main/java/view/EventDetailActivity.java`
- `app/src/main/java/utils/NotificationHelper.java`
- `app/src/main/java/view/NotificationsActivity.java`

---

### US-16: Organizer Cancels Event and Notifies Attendees
**Status:** PARTIAL (core flow works, needs reliability/coverage checks)  
**Priority:** Medium  
**Story Points:** 5

**Story:**  
As a club leader, I want to cancel an event so that registered attendees are notified automatically.

**Acceptance Criteria:**
- Organizer can mark event cancelled.
- Cancel action notifies confirmed attendees through notification records.

**Implementation Evidence:**
- `app/src/main/java/view/OrganizerEventAdapter.java`
- `app/src/main/java/service/FirestoreService.java`
- `app/src/main/java/service/NotificationService.java`

---

### US-18: Export Attendee List
**Status:** PARTIAL (basic export/share flow)  
**Priority:** Low  
**Story Points:** 3

**Story:**  
As a club leader, I want to export the attendee list for an event so that I can use it for check-in or records.

**Acceptance Criteria:**
- Organizer can trigger attendee export.
- Export shares attendee list in CSV-like text format.

**Implementation Evidence:**
- `app/src/main/java/view/OrganizerDashboardActivity.java`
- `app/src/main/java/service/FirestoreService.java`

---

### US-19: Organizer Broadcast to RSVPed Attendees
**Status:** COMPLETE  
**Priority:** Medium  
**Story Points:** 5

**Story:**  
As a club leader, I want to send a broadcast message to all RSVPed attendees so that I can communicate important updates.

**Acceptance Criteria:**
- Organizer opens broadcast screen.
- Attendee IDs are resolved for event.
- Notifications are written for each attendee.

**Implementation Evidence:**
- `app/src/main/java/view/BroadcastMessageActivity.java`
- `app/src/main/java/service/NotificationService.java`
- `app/src/main/java/service/FirestoreService.java`

---

### US-21: QR Ticket Generation for Attendees
**Status:** PARTIAL (payload ticket exists, real QR workflow pending)  
**Priority:** High  
**Story Points:** 8

**Story:**  
As a club leader, I want to generate QR-code tickets for attendees so that check-in at the door is fast and accurate.

**Acceptance Criteria:**
- Ticket payload is generated and persisted for confirmed RSVPs.
- Student can open ticket screen and view payload.
- Existing ticket is reused if already present.

**Implementation Evidence:**
- `app/src/main/java/service/FirestoreService.java`
- `app/src/main/java/view/TicketActivity.java`
- `app/src/main/java/model/Ticket.java`

---

### US-22: Staff Approval/Rejection Before Events Go Live
**Status:** PARTIAL (workflow exists, release policy still inconsistent)  
**Priority:** Medium  
**Story Points:** 5

**Story:**  
As LUMS staff, I want to approve or reject event submissions before they go live so that only appropriate events are listed.

**Acceptance Criteria:**
- Staff can view draft events.
- Staff can approve (status live) or reject (status cancelled).

**Implementation Evidence:**
- `app/src/main/java/view/StaffApprovalActivity.java`
- `app/src/main/java/service/FirestoreService.java`

---

### US-23: Platform Analytics for Staff
**Status:** PARTIAL (basic metrics only)  
**Priority:** Medium  
**Story Points:** 8

**Story:**  
As LUMS staff, I want to view platform-wide analytics (total events, RSVPs, popular categories) so that I can monitor campus activity.

**Acceptance Criteria:**
- Analytics screen loads platform metrics.
- Metrics include total events, total RSVPs, and top-category signal.

**Implementation Evidence:**
- `app/src/main/java/view/AnalyticsActivity.java`
- `app/src/main/java/service/FirestoreService.java`

---

## Part 3 Improvement Carryover (Room for Final Enhancements)

This section preserves improvement room from earlier planning and README notes.

### I-01: Stronger recommendation quality
**Status:** PARTIAL  
**Source:** Part 3 planned enhancements

Scope examples:
- Add richer preference model beyond category + count.
- Include recency/time and user profile signals.

### I-02: True push reminder delivery
**Status:** NOT STARTED  
**Source:** Part 3 planned enhancements

Scope examples:
- Add FCM push for event reminders/updates rather than inbox-only storage.

### I-03: Real QR image rendering / scan flow
**Status:** PARTIAL  
**Source:** Part 3 planned enhancements

Scope examples:
- Replace text payload-only ticket display with real QR bitmap and scan/check-in flow.

### I-04: Staff moderation hardening
**Status:** PARTIAL  
**Source:** Part 3 planned enhancements + context notes

Scope examples:
- Tighten notification/ticket creation rules for production-grade security.
- Enforce one consistent publish policy (direct live vs mandatory staff approval).

---

## Future-Facing Enhancements (Optional After Core US Completion)

These are intentionally separated from core backlog completion. Work on these only after required Part 4 user stories are stable for final demo.

Priority checklist:
- Finish all core Part 4 user stories first (US-05, US-09, US-10, US-11, US-12, US-13, US-16, US-18, US-19, US-21, US-22, US-23).
- Then pick optional enhancements in this order: I-05 -> I-06 -> I-08 -> I-07.

### I-05: Role selection and role-based onboarding hardening
**Status:** NOT STARTED  
**Source:** Team improvement idea

Scope examples:
- Add explicit and consistent role selection/handling for student, organizer, and staff.
- Improve first-login flow and role validation UX.

### I-06: Society profiles and event publisher identity
**Status:** PARTIAL (MVP shipped — directory, follow, society-scoped timelines, society tagging on create/edit)  
**Source:** Team improvement idea

Scope examples:
- Add society/club profile pages with details and posted events.
- Show publisher identity consistently on event cards/details.

**Delivered in-tree (iterative improvement):**
- `societies` collection + student **Campus Societies** list/search/follow and society **Past / Upcoming** event screens (`SocietiesListActivity`, `SocietyEventsActivity`, `CampusSocietyAdapter`).
- Events carry `societyId` / `societyName`; create/edit forms require an organizing society when the directory is loaded (`CreateEventActivity`, `EditEventActivity`).
- Seed script: `scripts/seed-societies.js` (`npm run seed:societies`).

### I-07: In-app assistant/chatbot for event help
**Status:** NOT STARTED  
**Source:** Team improvement idea

Scope examples:
- Add chatbot/help assistant for event discovery and FAQs.
- Connect assistant responses to event data and filters.

### I-08: Organizer shell navigation (Home / My Events / Profile)
**Status:** PARTIAL (MVP shipped — tab shell + profile shortcuts; polish vs student nav patterns remains optional)  
**Source:** Team improvement idea (split out from society work for grading/traceability)

Scope examples:
- Give organizers a stable primary navigation pattern aligned with multi-area workflows (dashboard vs history vs account).
- Keep legacy entry paths predictable during rollout.

**Delivered in-tree (iterative improvement):**
- Bottom-navigation host activity (`OrganizerMainActivity`) with **Home**, **My Events**, and **Profile** (`organizer_bottom_nav.xml`, `activity_organizer_main.xml`).
- **Home** tab fragment carries organizer dashboard-style flows (`OrganizerHomeFragment`, including list/FAB/broadcast/export patterns previously centered on the dashboard screen).
- **My Events** summary/history tab (`OrganizerMyEventsFragment`, `fragment_organizer_my_events.xml`).
- **Profile** tab entry points + sign-out (`OrganizerProfileFragment`, `fragment_organizer_profile.xml`).
- Organizer login routes to `OrganizerMainActivity`; `OrganizerDashboardActivity` remains as a thin redirect for older references.

---

## Category B — Bonus / end-of-build enhancements

Use this section for work added **after** the originally completed user stories, to track grading-related “bonus” scope (UX polish, optional fields, and extra screens that were not part of the baseline Part 3/Part 4 story definitions).

| ID | Enhancement | Notes |
|----|-------------|--------|
| B-01 | **Student landing (Home)** | Teal-style hero, top notification bell, stats (This week / Registered / Saved), **My Payments** entry (UI placeholder), quick tiles, upcoming-only feed with dedicated row layout. |
| B-02 | **Explore redesign** | Search Events header, category chips (All / Academic / Sports / Cultural mapped to existing categories), date range + PKR price presets + sort dialogs, result count, availability badges on cards. |
| B-03 | **Optional `ticketPricePkr` on events** | Supports realistic price filtering when populated; falls back when absent. |
| B-04 | **Attendance history screen** | `StudentEventHistoryActivity` — detailed RSVP history with tap-through to event detail. |
| B-05 | **Followed societies hub** | `StudentFollowedSocietiesActivity` — lists followed societies and opens `SocietyEventsActivity` for current/past events per society. |
| B-06 | **Profile quick-access polish** | Icon tiles on rows; navigation wired to history and followed societies; payments remains placeholder toast aligned with Home. |

---

## Final Notes

- Part 3 stories remain documented in `USER_STORY_BACKLOG.md`.
- This file captures the **remaining product backlog stories** and improvement carryover for final delivery alignment.

---

## Definition of Done (Final Submission + 3-Min Demo)

### Global DoD (applies to all US-01 to US-25)
- Every story in the original backlog (US-01..US-25) is demoable live in one coherent 3-minute flow without blockers.
- Core paths complete without crashes, permission errors, or broken navigation.
- Firestore/Auth/rules configuration supports all demonstrated flows on device.
- UI is readable and usable enough for evaluator walkthrough (not just backend-complete).

### DoD Targets for PARTIAL / NOT STARTED Items

- **US-05 (PARTIAL):** Recommendations reflect selected chip and prior RSVP history with stable ranking behavior in live testing.
- **US-11 (PARTIAL):** "My Calendar" experience is clearly schedule-oriented and reliably shows all confirmed RSVPs.
- **US-12 (PARTIAL):** Event update/cancel notifications consistently appear for affected attendees in inbox flow.
- **US-13 (PARTIAL):** Reminder behavior is explicitly triggered and visible to user (inbox/tray) for RSVP events.
- **US-16 (PARTIAL):** Event cancel path consistently updates status and notifies attendees in end-to-end test.
- **US-18 (PARTIAL):** Attendee export is usable in demo (clear output, sharable, event-scoped correctness).
- **US-21 (PARTIAL):** Ticket flow is end-to-end reliable; if QR image is not implemented, payload behavior is explicitly documented and consistently shown.
- **US-22 (PARTIAL):** Staff approval/rejection works with one enforced and documented publish policy for final demo.
- **US-23 (PARTIAL):** Analytics screen shows meaningful, correct metrics loaded from current data.
- **I-02 (NOT STARTED):** Decide and document whether true push reminders are in-scope for final; if out-of-scope, keep backlog item deferred.
- **I-05 (NOT STARTED):** Role onboarding UX is explicit and consistent for student/organizer/staff.
- **I-06 (PARTIAL):** Society directory and society-scoped event browsing work end-to-end once `societies` is seeded; surface society on shared student event cards/details as a follow-up polish item.
- **I-08 (PARTIAL):** Organizer bottom navigation is usable end-to-end (Home / My Events / Profile) without blocking organizer workflows that previously lived on the single dashboard activity.
- **I-07 (NOT STARTED):** Chatbot is either implemented as MVP or explicitly deferred with rationale in final backlog.
