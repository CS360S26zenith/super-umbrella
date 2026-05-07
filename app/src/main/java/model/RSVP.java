package com.example.campuseventstest.model;

import com.google.firebase.Timestamp;

/**
 * Represents an RSVP record linking a student to an event.
 * Tracks the registration timestamp and current confirmation status.
 * Each RSVP is stored as a document in the "rsvps" Firestore collection.
 */
public class RSVP {

    private String id;
    private String studentId;
    private String eventId;
    private Timestamp timestamp;
    private String status;

    /**
     * Required empty constructor for Firestore deserialization.
     */
    public RSVP() {
    }

    /**
     * Constructs a confirmed RSVP with the current timestamp.
     *
     * @param studentId the UID of the student making the RSVP
     * @param eventId   the Firestore document ID of the event
     */
    public RSVP(String studentId, String eventId) {
        this.studentId = studentId;
        this.eventId = eventId;
        this.timestamp = Timestamp.now();
        this.status = "confirmed";
    }

    /**
     * Checks if this RSVP is still active (confirmed).
     *
     * @return true if status equals "confirmed"
     */
    public boolean isConfirmed() {
        return "confirmed".equals(status);
    }

    /**
     * Gets the Firestore document ID of this RSVP.
     *
     * @return the RSVP ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the Firestore document ID.
     *
     * @param id the RSVP ID to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the UID of the student who made this RSVP.
     *
     * @return the student UID
     */
    public String getStudentId() {
        return studentId;
    }

    /**
     * Sets the student UID.
     *
     * @param studentId the student UID to set
     */
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    /**
     * Gets the Firestore document ID of the event.
     *
     * @return the event ID
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Sets the event ID.
     *
     * @param eventId the event ID to set
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * Gets the timestamp when this RSVP was created.
     *
     * @return the Firestore Timestamp
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the RSVP timestamp.
     *
     * @param timestamp the Timestamp to set
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Gets the current RSVP status.
     *
     * @return the status string ("confirmed" or "cancelled")
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the RSVP status.
     *
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }
}