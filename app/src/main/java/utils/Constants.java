package com.example.campuseventstest.utils;

/**
 * Application-wide constants for Firestore collection names, user roles,
 * event statuses, and event categories.
 */
public class Constants {

    // Firestore collection names
    public static final String COLLECTION_EVENTS = "events";
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_RSVPS = "rsvps";
    public static final String COLLECTION_WAITLIST = "waitlist";
    public static final String COLLECTION_TICKETS = "tickets";
    public static final String COLLECTION_NOTIFICATIONS = "notifications";
    public static final String COLLECTION_SOCIETIES = "societies";

    // User roles
    public static final String ROLE_STUDENT = "student";
    public static final String ROLE_ORGANIZER = "organizer";
    public static final String ROLE_STAFF = "staff";

    // Event statuses
    public static final String STATUS_DRAFT = "draft";
    public static final String STATUS_LIVE = "live";
    public static final String STATUS_CANCELLED = "cancelled";
    public static final String STATUS_CONFIRMED = "confirmed";

    // Event categories (individual constants)
    public static final String CATEGORY_TALKS = "Talks";
    public static final String CATEGORY_SPORTS = "Sports";
    public static final String CATEGORY_CLUBS = "Clubs";
    public static final String CATEGORY_PERFORMANCES = "Performances";

    // Event categories (array for convenience)
    public static final String[] CATEGORIES = {
            CATEGORY_TALKS, CATEGORY_SPORTS, CATEGORY_CLUBS, CATEGORY_PERFORMANCES
    };

    // Intent extras
    public static final String EXTRA_EVENT_ID = "eventId";
    public static final String EXTRA_USER_ROLE = "extra_user_role";
    public static final String EXTRA_EVENT_TITLE = "eventTitle";
    public static final String EXTRA_SOCIETY_ID = "societyId";
    public static final String EXTRA_SOCIETY_NAME = "societyName";

    private Constants() {
        // Prevent instantiation
    }
}