package com.example.campuseventstest.model;

/**
 * Represents a ticket generated for a confirmed RSVP.
 */
public class Ticket {
    private String id;
    private String rsvpId;
    private String eventId;
    private String studentId;
    private String qrCode;
    private boolean used;

    public Ticket() {
    }

    public Ticket(String rsvpId, String eventId, String studentId, String qrCode) {
        this.rsvpId = rsvpId;
        this.eventId = eventId;
        this.studentId = studentId;
        this.qrCode = qrCode;
        this.used = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRsvpId() {
        return rsvpId;
    }

    public void setRsvpId(String rsvpId) {
        this.rsvpId = rsvpId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
}
