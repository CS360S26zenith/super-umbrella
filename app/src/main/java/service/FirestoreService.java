package com.example.campuseventstest.service;

import com.example.campuseventstest.model.AppNotification;
import com.example.campuseventstest.model.Event;
import com.example.campuseventstest.model.Society;
import com.example.campuseventstest.model.Ticket;
import com.example.campuseventstest.model.Waitlist;
import com.example.campuseventstest.utils.Constants;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles all Firestore read/write operations for events and RSVPs.
 * Provides asynchronous callbacks for all database operations.
 */
public class FirestoreService {

    /** Firestore caps {@code whereIn} / {@code in} filters (document ID lists included). */
    private static final int FIRESTORE_WHERE_IN_MAX = 30;

    private final FirebaseFirestore db;

    /**
     * Constructs a FirestoreService with the default Firestore instance.
     */
    public FirestoreService() {
        this.db = FirebaseFirestore.getInstance();
    }

    // ── Callback Interfaces ─────────────────────────────────────────────

    /**
     * Callback for operations that return a list of events.
     */
    public interface EventListCallback {
        /**
         * Called when the event list is successfully retrieved.
         *
         * @param events the list of events
         */
        void onSuccess(List<Event> events);

        /**
         * Called when the operation fails.
         *
         * @param error the error message
         */
        void onFailure(String error);
    }

    /**
     * Callback for operations that return a single event.
     */
    public interface EventCallback {
        /**
         * Called when the event is successfully retrieved.
         *
         * @param event the event
         */
        void onSuccess(Event event);

        /**
         * Called when the operation fails.
         *
         * @param error the error message
         */
        void onFailure(String error);
    }

    /**
     * Callback for simple success/failure operations.
     */
    public interface SimpleCallback {
        /**
         * Called when the operation succeeds.
         */
        void onSuccess();

        /**
         * Called when the operation fails.
         *
         * @param error the error message
         */
        void onFailure(String error);
    }

    /**
     * Callback for operations that return a boolean result.
     */
    public interface BooleanCallback {
        /**
         * Called with the boolean result.
         *
         * @param result the result value
         */
        void onResult(boolean result);

        /**
         * Called when the operation fails.
         *
         * @param error the error message
         */
        void onFailure(String error);
    }

    /**
     * Callback for operations that return a list of societies.
     */
    public interface SocietyListCallback {
        void onSuccess(List<Society> societies);
        void onFailure(String error);
    }

    /**
     * Callback for loading persisted in-app notifications for the signed-in user.
     */
    public interface NotificationListCallback {
        /**
         * @param notifications newest-first after sorting
         */
        void onSuccess(List<AppNotification> notifications);

        void onFailure(String error);
    }

    // ── Event Read Operations ───────────────────────────────────────────

    /**
     * Fetches all live events from Firestore.
     *
     * @param callback the callback to receive the event list
     */
    public void getLiveEvents(final EventListCallback callback) {
        db.collection(Constants.COLLECTION_EVENTS)
                .whereEqualTo("status", Constants.STATUS_LIVE)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Event> events = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Event e = doc.toObject(Event.class);
                        e.setEventId(doc.getId());
                        events.add(e);
                    }
                    callback.onSuccess(events);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Fetches live events filtered by category.
     *
     * @param category the category to filter by
     * @param callback the callback to receive the event list
     */
    public void getEventsByCategory(String category, final EventListCallback callback) {
        db.collection(Constants.COLLECTION_EVENTS)
                .whereEqualTo("status", Constants.STATUS_LIVE)
                .whereEqualTo("category", category)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Event> events = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Event e = doc.toObject(Event.class);
                        e.setEventId(doc.getId());
                        events.add(e);
                    }
                    callback.onSuccess(events);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Fetches a single event by its Firestore document ID.
     *
     * @param eventId  the event document ID
     * @param callback the callback to receive the event
     */
    public void getEventById(String eventId, final EventCallback callback) {
        db.collection(Constants.COLLECTION_EVENTS)
                .document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Event e = doc.toObject(Event.class);
                        if (e == null) {
                            callback.onFailure("Event data could not be loaded");
                            return;
                        }
                        e.setEventId(doc.getId());
                        callback.onSuccess(e);
                    } else {
                        callback.onFailure("Event not found");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Fetches all events created by a specific organizer.
     *
     * @param organizerId the organizer's UID
     * @param callback    the callback to receive the event list
     */
    public void getEventsByOrganizer(String organizerId, final EventListCallback callback) {
        db.collection(Constants.COLLECTION_EVENTS)
                .whereEqualTo("organizerId", organizerId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Event> events = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Event e = doc.toObject(Event.class);
                        e.setEventId(doc.getId());
                        events.add(e);
                    }
                    callback.onSuccess(events);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Loads public society directory entries (document ID becomes {@link Society#setId}).
     */
    public void getSocieties(final SocietyListCallback callback) {
        db.collection(Constants.COLLECTION_SOCIETIES)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Society> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Society s = doc.toObject(Society.class);
                        if (s != null) {
                            s.setId(doc.getId());
                            list.add(s);
                        }
                    }
                    Collections.sort(list, Comparator.comparing(Society::getName,
                            Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER)));
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Loads events tagged with {@code societyId}. Caller filters by role (e.g. hide drafts for students).
     */
    public void getEventsBySociety(String societyId, final EventListCallback callback) {
        if (societyId == null || societyId.isEmpty()) {
            callback.onFailure("Missing society id");
            return;
        }
        db.collection(Constants.COLLECTION_EVENTS)
                .whereEqualTo("societyId", societyId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Event> events = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Event e = doc.toObject(Event.class);
                        if (e != null) {
                            e.setEventId(doc.getId());
                            events.add(e);
                        }
                    }
                    callback.onSuccess(events);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /** Adds or removes a society id on the signed-in user's follow list. */
    public void setFollowingSociety(String userId, String societyId, boolean follow,
            final SimpleCallback callback) {
        if (userId == null || societyId == null) {
            callback.onFailure("Missing user or society id");
            return;
        }
        DocumentReference ref = db.collection(Constants.COLLECTION_USERS).document(userId);
        Object op = follow ? FieldValue.arrayUnion(societyId) : FieldValue.arrayRemove(societyId);
        ref.update("followedSocietyIds", op)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ── Event Write Operations ──────────────────────────────────────────

    /**
     * Creates a new event document in Firestore.
     *
     * @param event    the event to create
     * @param callback the callback for success/failure
     */
    public void createEvent(Event event, final SimpleCallback callback) {
        db.collection(Constants.COLLECTION_EVENTS)
                .add(event)
                .addOnSuccessListener(ref -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Creates a new event with explicit approval behavior.
     */
    public void createEvent(Event event, boolean requiresApproval, final SimpleCallback callback) {
        event.setStatus(requiresApproval ? Constants.STATUS_DRAFT : Constants.STATUS_LIVE);
        createEvent(event, callback);
    }

    /**
     * Updates an existing event in Firestore (uses the event's own ID).
     *
     * @param event    the event with updated fields (must have eventId set)
     * @param callback the callback for success/failure
     */
    public void updateEvent(Event event, final SimpleCallback callback) {
        if (event.getEventId() == null || event.getEventId().isEmpty()) {
            callback.onFailure("Event ID is required for update");
            return;
        }
        db.collection(Constants.COLLECTION_EVENTS)
                .document(event.getEventId())
                .set(event)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Updates an existing event in Firestore using an explicit event ID.
     *
     * @param eventId  the Firestore document ID
     * @param event    the event with updated fields
     * @param callback the callback for success/failure
     */
    public void updateEvent(String eventId, Event event, final SimpleCallback callback) {
        db.collection(Constants.COLLECTION_EVENTS)
                .document(eventId)
                .set(event)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Deletes an event document from Firestore.
     *
     * @param eventId  the Firestore document ID to delete
     * @param callback the callback for success/failure
     */
    public void deleteEvent(String eventId, final SimpleCallback callback) {
        db.collection(Constants.COLLECTION_EVENTS)
                .document(eventId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ── RSVP Operations ─────────────────────────────────────────────────

    /**
     * Creates an RSVP record and increments the event's rsvpCount atomically.
     *
     * @param studentId the student's UID
     * @param eventId   the event document ID
     * @param callback  the callback for success/failure
     */
    public void rsvpToEvent(String studentId, String eventId, final SimpleCallback callback) {
        WriteBatch batch = db.batch();

        DocumentReference rsvpRef = db.collection(Constants.COLLECTION_RSVPS).document();
        // Use explicit maps so Firestore field names always match rules (`studentId`, etc.).
        Map<String, Object> rsvpMap = new HashMap<>();
        rsvpMap.put("studentId", studentId);
        rsvpMap.put("eventId", eventId);
        rsvpMap.put("status", Constants.STATUS_CONFIRMED);
        rsvpMap.put("timestamp", com.google.firebase.Timestamp.now());
        batch.set(rsvpRef, rsvpMap);

        DocumentReference eventRef = db.collection(Constants.COLLECTION_EVENTS).document(eventId);
        batch.update(eventRef, "rsvpCount", FieldValue.increment(1));

        batch.commit()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Cancels an RSVP and decrements the event's rsvpCount atomically.
     *
     * @param studentId the student's UID
     * @param eventId   the event document ID
     * @param callback  the callback for success/failure
     */
    public void cancelRsvp(String studentId, String eventId, final SimpleCallback callback) {
        db.collection(Constants.COLLECTION_RSVPS)
                .whereEqualTo("studentId", studentId)
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("status", "confirmed")
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        callback.onFailure("RSVP not found");
                        return;
                    }
                    WriteBatch batch = db.batch();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        batch.update(doc.getReference(), "status", "cancelled");
                    }
                    DocumentReference eventRef =
                            db.collection(Constants.COLLECTION_EVENTS).document(eventId);
                    batch.update(eventRef, "rsvpCount", FieldValue.increment(-1));

                    batch.commit()
                            .addOnSuccessListener(aVoid -> callback.onSuccess())
                            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Checks if a student has an active (confirmed) RSVP for an event.
     *
     * @param studentId the student's UID
     * @param eventId   the event document ID
     * @param callback  the callback returning true if RSVP exists, false otherwise
     */
    public void hasUserRsvped(String studentId, String eventId, final BooleanCallback callback) {
        db.collection(Constants.COLLECTION_RSVPS)
                .whereEqualTo("studentId", studentId)
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("status", "confirmed")
                .get()
                .addOnSuccessListener(snapshot -> callback.onResult(!snapshot.isEmpty()))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Creates RSVP only if user has not already RSVP'd and event is not full.
     */
    public void rsvpToEventSafe(String studentId, Event event, final SimpleCallback callback) {
        if (event == null || event.getEventId() == null) {
            callback.onFailure("Invalid event");
            return;
        }
        hasUserRsvped(studentId, event.getEventId(), new BooleanCallback() {
            @Override
            public void onResult(boolean result) {
                if (result) {
                    callback.onFailure("You have already RSVP'd.");
                    return;
                }
                getEventById(event.getEventId(), new EventCallback() {
                    @Override
                    public void onSuccess(Event latest) {
                        if (latest.isFull()) {
                            callback.onFailure("Event is full.");
                            return;
                        }
                        rsvpToEvent(studentId, event.getEventId(), callback);
                    }

                    @Override
                    public void onFailure(String error) {
                        callback.onFailure(error);
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                callback.onFailure(error);
            }
        });
    }

    /**
     * Adds a student to the event waitlist and returns queue position.
     */
    public void joinWaitlist(String studentId, String eventId, final WaitlistCallback callback) {
        // Single-field equality only — avoids composite index (eventId + orderBy position).
        db.collection(Constants.COLLECTION_WAITLIST)
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    int maxPosition = 0;
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Long position = doc.getLong("position");
                        if (position != null && position > maxPosition) {
                            maxPosition = position.intValue();
                        }
                    }
                    final int nextPosition = maxPosition + 1;
                    Waitlist waitlist = new Waitlist(studentId, eventId, nextPosition);
                    db.collection(Constants.COLLECTION_WAITLIST)
                            .add(waitlist)
                            .addOnSuccessListener(ref -> callback.onSuccess(nextPosition))
                            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public interface WaitlistCallback {
        void onSuccess(int position);
        void onFailure(String error);
    }

    public interface StringCallback {
        void onSuccess(String value);
        void onFailure(String error);
    }

    /**
     * Returns an existing ticket QR payload or creates a new ticket document.
     * Uses only studentId+eventId on RSVPs (then filters status in code) so Firestore does not
     * require a 3-field composite index; also reuses an existing ticket if present.
     */
    public void generateTicketForRsvp(String studentId, String eventId, final StringCallback callback) {
        db.collection(Constants.COLLECTION_RSVPS)
                .whereEqualTo("studentId", studentId)
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    String rsvpId = null;
                    for (QueryDocumentSnapshot doc : snapshot) {
                        String status = doc.getString("status");
                        if (status != null && Constants.STATUS_CONFIRMED.equalsIgnoreCase(status)) {
                            rsvpId = doc.getId();
                            break;
                        }
                    }
                    if (rsvpId == null) {
                        callback.onFailure("No confirmed RSVP found for this event.");
                        return;
                    }
                    final String confirmedRsvpId = rsvpId;
                    String qrPayload = "ticket:" + eventId + ":" + studentId + ":" + confirmedRsvpId;

                    db.collection(Constants.COLLECTION_TICKETS)
                            .whereEqualTo("studentId", studentId)
                            .whereEqualTo("eventId", eventId)
                            .limit(1)
                            .get()
                            .addOnSuccessListener(ticketSnap -> {
                                if (!ticketSnap.isEmpty()) {
                                    String existing = ticketSnap.getDocuments().get(0).getString("qrCode");
                                    if (existing != null && !existing.isEmpty()) {
                                        callback.onSuccess(existing);
                                    } else {
                                        callback.onSuccess(qrPayload);
                                    }
                                    return;
                                }
                                Ticket ticket = new Ticket(confirmedRsvpId, eventId, studentId, qrPayload);
                                db.collection(Constants.COLLECTION_TICKETS)
                                        .add(ticket)
                                        .addOnSuccessListener(ref -> callback.onSuccess(qrPayload))
                                        .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                            })
                            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getRsvpedEventsByStudent(String studentId, final EventListCallback callback) {
        db.collection(Constants.COLLECTION_RSVPS)
                .whereEqualTo("studentId", studentId)
                .whereEqualTo("status", Constants.STATUS_CONFIRMED)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        callback.onSuccess(new ArrayList<>());
                        return;
                    }
                    List<String> eventIds = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        String eventId = doc.getString("eventId");
                        if (eventId != null) {
                            eventIds.add(eventId);
                        }
                    }
                    fetchEventsByDocumentIdsChunked(eventIds, 0, new ArrayList<>(), callback);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Loads events by document ID in chunks to satisfy Firestore {@code whereIn} limits.
     */
    private void fetchEventsByDocumentIdsChunked(List<String> eventIds, int offset,
            List<Event> accumulator, final EventListCallback callback) {
        if (offset >= eventIds.size()) {
            callback.onSuccess(accumulator);
            return;
        }
        int end = Math.min(offset + FIRESTORE_WHERE_IN_MAX, eventIds.size());
        List<String> chunk = new ArrayList<>(eventIds.subList(offset, end));
        db.collection(Constants.COLLECTION_EVENTS)
                .whereIn(FieldPath.documentId(), chunk)
                .get()
                .addOnSuccessListener(eventsSnapshot -> {
                    for (QueryDocumentSnapshot doc : eventsSnapshot) {
                        Event event = doc.toObject(Event.class);
                        if (event != null) {
                            event.setEventId(doc.getId());
                            accumulator.add(event);
                        }
                    }
                    fetchEventsByDocumentIdsChunked(eventIds, end, accumulator, callback);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getAttendeeIdsForEvent(String eventId, final UserIdListCallback callback) {
        db.collection(Constants.COLLECTION_RSVPS)
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("status", Constants.STATUS_CONFIRMED)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<String> userIds = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        String studentId = doc.getString("studentId");
                        if (studentId != null) {
                            userIds.add(studentId);
                        }
                    }
                    callback.onSuccess(userIds);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public interface UserIdListCallback {
        void onSuccess(List<String> userIds);
        void onFailure(String error);
    }

    public interface MapCallback {
        void onSuccess(Map<String, Integer> metrics);
        void onFailure(String error);
    }

    public interface AnalyticsCallback {
        void onSuccess(PlatformAnalytics analytics);
        void onFailure(String error);
    }

    public static class PlatformAnalytics {
        private int totalEvents;
        private int totalRsvps;
        private int liveEvents;
        private int draftEvents;
        private int cancelledEvents;
        private int averageRsvpsPerEvent;
        private Map<String, Integer> categoryCounts = new HashMap<>();

        public int getTotalEvents() { return totalEvents; }
        public int getTotalRsvps() { return totalRsvps; }
        public int getLiveEvents() { return liveEvents; }
        public int getDraftEvents() { return draftEvents; }
        public int getCancelledEvents() { return cancelledEvents; }
        public int getAverageRsvpsPerEvent() { return averageRsvpsPerEvent; }
        public Map<String, Integer> getCategoryCounts() { return categoryCounts; }
    }

    public void getPendingEvents(final EventListCallback callback) {
        db.collection(Constants.COLLECTION_EVENTS)
                .whereEqualTo("status", Constants.STATUS_DRAFT)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Event> events = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Event event = doc.toObject(Event.class);
                        event.setEventId(doc.getId());
                        events.add(event);
                    }
                    callback.onSuccess(events);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void updateEventStatus(String eventId, String status, final SimpleCallback callback) {
        db.collection(Constants.COLLECTION_EVENTS)
                .document(eventId)
                .update("status", status)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getPlatformAnalytics(final MapCallback callback) {
        Map<String, Integer> metrics = new HashMap<>();
        db.collection(Constants.COLLECTION_EVENTS).get()
                .addOnSuccessListener(eventSnapshot -> {
                    metrics.put("totalEvents", eventSnapshot.size());
                    db.collection(Constants.COLLECTION_RSVPS)
                            .whereEqualTo("status", Constants.STATUS_CONFIRMED)
                            .get()
                            .addOnSuccessListener(rsvpSnapshot -> {
                                metrics.put("totalRsvps", rsvpSnapshot.size());
                                Map<String, Integer> categoryCounts = new HashMap<>();
                                for (QueryDocumentSnapshot doc : eventSnapshot) {
                                    String category = doc.getString("category");
                                    if (category != null) {
                                        categoryCounts.put(category,
                                                categoryCounts.getOrDefault(category, 0) + 1);
                                    }
                                }
                                int topCategoryCount = 0;
                                for (int count : categoryCounts.values()) {
                                    topCategoryCount = Math.max(topCategoryCount, count);
                                }
                                metrics.put("topCategoryCount", topCategoryCount);
                                callback.onSuccess(metrics);
                            })
                            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getPlatformAnalyticsDetailed(final AnalyticsCallback callback) {
        db.collection(Constants.COLLECTION_EVENTS).get()
                .addOnSuccessListener(eventSnapshot -> db.collection(Constants.COLLECTION_RSVPS)
                        .whereEqualTo("status", Constants.STATUS_CONFIRMED)
                        .get()
                        .addOnSuccessListener(rsvpSnapshot -> {
                            PlatformAnalytics analytics = new PlatformAnalytics();
                            analytics.totalEvents = eventSnapshot.size();
                            analytics.totalRsvps = rsvpSnapshot.size();
                            analytics.averageRsvpsPerEvent = analytics.totalEvents == 0
                                    ? 0 : analytics.totalRsvps / analytics.totalEvents;

                            for (QueryDocumentSnapshot doc : eventSnapshot) {
                                String status = doc.getString("status");
                                if (Constants.STATUS_LIVE.equalsIgnoreCase(status)) {
                                    analytics.liveEvents++;
                                } else if (Constants.STATUS_DRAFT.equalsIgnoreCase(status)) {
                                    analytics.draftEvents++;
                                } else if (Constants.STATUS_CANCELLED.equalsIgnoreCase(status)) {
                                    analytics.cancelledEvents++;
                                }
                                String category = doc.getString("category");
                                if (category != null && !category.trim().isEmpty()) {
                                    analytics.categoryCounts.put(
                                            category,
                                            analytics.categoryCounts.getOrDefault(category, 0) + 1
                                    );
                                }
                            }
                            callback.onSuccess(analytics);
                        })
                        .addOnFailureListener(e -> callback.onFailure(e.getMessage())))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Loads notifications for {@code userId} from {@link Constants#COLLECTION_NOTIFICATIONS}.
     * Sorted newest-first on the client so no composite index is required.
     */
    public void getNotificationsForUser(String userId, final NotificationListCallback callback) {
        db.collection(Constants.COLLECTION_NOTIFICATIONS)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<AppNotification> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        AppNotification n = doc.toObject(AppNotification.class);
                        if (n != null) {
                            n.setId(doc.getId());
                            list.add(n);
                        }
                    }
                    list.sort((a, b) -> {
                        if (a.getTimestamp() == null && b.getTimestamp() == null) {
                            return 0;
                        }
                        if (a.getTimestamp() == null) {
                            return 1;
                        }
                        if (b.getTimestamp() == null) {
                            return -1;
                        }
                        return b.getTimestamp().compareTo(a.getTimestamp());
                    });
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}