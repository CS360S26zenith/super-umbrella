package com.example.campuseventstest.model;

import com.google.firebase.Timestamp;

/**
 * Represents a waitlist entry when an event has no remaining capacity.
 */
public class Waitlist {
    private String id;
    private String studentId;
    private String eventId;
    private int position;
    private Timestamp timestamp;

    public Waitlist() {
    }

    public Waitlist(String studentId, String eventId, int position) {
        this.studentId = studentId;
        this.eventId = eventId;
        this.position = position;
        this.timestamp = Timestamp.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
