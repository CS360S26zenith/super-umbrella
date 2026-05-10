package com.example.campuseventstest.model;

import com.google.firebase.Timestamp;

import java.util.Date;

/**
 * Represents a campus event in the system.
 * Contains all event details including title, description, capacity, and RSVP tracking.
 * Provides methods for capacity management and fill percentage calculations.
 */
public class Event {

    private String eventId;
    private String title;
    private String description;
    private Timestamp date;
    private String venue;
    private String category;
    private int capacity;
    private int rsvpCount;
    private String organizerId;
    private String status;
    /** Firestore document ID of hosting society in {@code societies}. */
    private String societyId;
    /** Denormalized society display name for cards (optional). */
    private String societyName;
    /** Optional ticket price in PKR for student Explore filters (defaults treated as free). */
    private Double ticketPricePkr;

    /**
     * Required empty constructor for Firestore deserialization.
     */
    public Event() {
    }

    /**
     * Constructs an Event with basic fields (rsvpCount defaults to 0, status to "live").
     *
     * @param title       the event title
     * @param description the event description
     * @param date        the event date as a Firestore Timestamp
     * @param venue       the event venue
     * @param category    the event category
     * @param capacity    the maximum attendee capacity
     * @param organizerId the UID of the organizer who created this event
     */
    public Event(String title, String description, Timestamp date, String venue,
                 String category, int capacity, String organizerId) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.venue = venue;
        this.category = category;
        this.capacity = capacity;
        this.rsvpCount = 0;
        this.organizerId = organizerId;
        this.status = "live";
    }

    /**
     * Constructs an Event with all fields specified explicitly.
     *
     * @param eventId     the Firestore document ID
     * @param title       the event title
     * @param description the event description
     * @param date        the event date as a Firestore Timestamp
     * @param venue       the event venue
     * @param category    the event category
     * @param capacity    the maximum attendee capacity
     * @param rsvpCount   the current number of confirmed RSVPs
     * @param organizerId the UID of the organizer who created this event
     * @param status      the event status (live, draft, or cancelled)
     */
    public Event(String eventId, String title, String description, Timestamp date,
                 String venue, String category, int capacity, int rsvpCount,
                 String organizerId, String status) {
        this.eventId = eventId;
        this.title = title;
        this.description = description;
        this.date = date;
        this.venue = venue;
        this.category = category;
        this.capacity = capacity;
        this.rsvpCount = rsvpCount;
        this.organizerId = organizerId;
        this.status = status;
    }

    /**
     * Checks if the event has reached maximum capacity.
     *
     * @return true if rsvpCount &gt;= capacity, false otherwise
     */
    public boolean isFull() {
        return rsvpCount >= capacity;
    }

    /**
     * Gets the number of available spots remaining.
     *
     * @return number of spots left (capacity minus rsvpCount), minimum 0
     */
    public int getSpotsRemaining() {
        return Math.max(0, capacity - rsvpCount);
    }

    /**
     * Calculates the fill percentage for UI display.
     *
     * @return percentage filled (0 to 100)
     */
    public int getFillPercentage() {
        if (capacity == 0) return 0;
        return (int) ((rsvpCount * 100.0) / capacity);
    }

    // ── Getters and Setters ─────────────────────────────────────────────

    /**
     * Gets the Firestore document ID.
     *
     * @return the event ID
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Sets the Firestore document ID.
     *
     * @param eventId the event ID to set
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * Alias for getEventId() for compatibility.
     *
     * @return the event ID
     */
    public String getId() {
        return eventId;
    }

    /**
     * Alias for setEventId() for compatibility.
     *
     * @param id the event ID to set
     */
    public void setId(String id) {
        this.eventId = id;
    }

    /**
     * Gets the event title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the event title.
     *
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the event description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the event description.
     *
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the event date as a Firestore Timestamp.
     *
     * @return the date Timestamp
     */
    public Timestamp getDate() {
        return date;
    }

    /**
     * Sets the event date.
     *
     * @param date the Timestamp to set
     */
    public void setDate(Timestamp date) {
        this.date = date;
    }

    /**
     * Convenience method to get the event date as a java.util.Date.
     *
     * @return the date as a Date object, or null if date is null
     */
    public Date getDateAsDate() {
        return date != null ? date.toDate() : null;
    }

    /**
     * Gets the event venue.
     *
     * @return the venue
     */
    public String getVenue() {
        return venue;
    }

    /**
     * Sets the event venue.
     *
     * @param venue the venue to set
     */
    public void setVenue(String venue) {
        this.venue = venue;
    }

    /**
     * Gets the event category.
     *
     * @return the category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the event category.
     *
     * @param category the category to set
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Gets the maximum attendee capacity.
     *
     * @return the capacity
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Sets the maximum attendee capacity.
     *
     * @param capacity the capacity to set
     */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    /**
     * Gets the current number of confirmed RSVPs.
     *
     * @return the RSVP count
     */
    public int getRsvpCount() {
        return rsvpCount;
    }

    /**
     * Sets the current RSVP count.
     *
     * @param rsvpCount the count to set
     */
    public void setRsvpCount(int rsvpCount) {
        this.rsvpCount = rsvpCount;
    }

    /**
     * Gets the UID of the organizer who created this event.
     *
     * @return the organizer ID
     */
    public String getOrganizerId() {
        return organizerId;
    }

    /**
     * Sets the organizer ID.
     *
     * @param organizerId the organizer UID to set
     */
    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    /**
     * Gets the event status (live, draft, or cancelled).
     *
     * @return the status string
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the event status.
     *
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    public String getSocietyId() {
        return societyId;
    }

    public void setSocietyId(String societyId) {
        this.societyId = societyId;
    }

    public String getSocietyName() {
        return societyName;
    }

    public void setSocietyName(String societyName) {
        this.societyName = societyName;
    }

    public Double getTicketPricePkr() {
        return ticketPricePkr;
    }

    public void setTicketPricePkr(Double ticketPricePkr) {
        this.ticketPricePkr = ticketPricePkr;
    }

    /** Effective price for filtering (null or negative → free). */
    public double getEffectiveTicketPricePkr() {
        if (ticketPricePkr == null || ticketPricePkr < 0) {
            return 0;
        }
        return ticketPricePkr;
    }
}