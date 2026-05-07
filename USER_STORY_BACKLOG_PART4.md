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
**Status:** Implemented (simple heuristic)  
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
**Status:** Implemented  
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
**Status:** Implemented  
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
**Status:** Implemented (list-style view)  
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
**Status:** Implemented (in-app inbox notifications)  
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
**Status:** Implemented (in-app + tray at RSVP time)  
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
**Status:** Implemented  
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
**Status:** Implemented  
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
**Status:** Implemented  
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
**Status:** Implemented (text payload ticket)  
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
**Status:** Implemented (workflow available; optional in current release policy)  
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
**Status:** Implemented (basic analytics)  
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
**Status:** In progress  
**Source:** Part 3 planned enhancements

Scope examples:
- Add richer preference model beyond category + count.
- Include recency/time and user profile signals.

### I-02: True push reminder delivery
**Status:** Not started  
**Source:** Part 3 planned enhancements

Scope examples:
- Add FCM push for event reminders/updates rather than inbox-only storage.

### I-03: Real QR image rendering / scan flow
**Status:** Partial  
**Source:** Part 3 planned enhancements

Scope examples:
- Replace text payload-only ticket display with real QR bitmap and scan/check-in flow.

### I-04: Staff moderation hardening
**Status:** Partial  
**Source:** Part 3 planned enhancements + context notes

Scope examples:
- Tighten notification/ticket creation rules for production-grade security.
- Enforce one consistent publish policy (direct live vs mandatory staff approval).

---

## Final Notes

- Part 3 stories remain documented in `USER_STORY_BACKLOG.md`.
- This file captures the **remaining product backlog stories** and improvement carryover for final delivery alignment.
