package com.example.campuseventstest.model;

import com.google.firebase.Timestamp;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * JUnit tests for the RSVP model class.
 * Tests RSVP status checking and properties.
 */
public class RSVPTest {

    private RSVP confirmedRsvp;
    private RSVP cancelledRsvp;
    private Timestamp testTimestamp;

    @Before
    public void setUp() {
        testTimestamp = new Timestamp(new Date());

        confirmedRsvp = new RSVP(
                "rsvp123",
                "student456",
                "event789",
                testTimestamp,
                "confirmed"
        );

        cancelledRsvp = new RSVP(
                "rsvp456",
                "student789",
                "event123",
                testTimestamp,
                "cancelled"
        );
    }

    @Test
    public void testIsConfirmed_WithConfirmedStatus_ReturnsTrue() {
        assertTrue("RSVP with confirmed status should return true",
                confirmedRsvp.isConfirmed());
    }

    @Test
    public void testIsConfirmed_WithCancelledStatus_ReturnsFalse() {
        assertFalse("RSVP with cancelled status should return false",
                cancelledRsvp.isConfirmed());
    }

    @Test
    public void testGetters() {
        assertEquals("RSVP ID should match", "rsvp123", confirmedRsvp.getRsvpId());
        assertEquals("Student ID should match", "student456", confirmedRsvp.getStudentId());
        assertEquals("Event ID should match", "event789", confirmedRsvp.getEventId());
        assertEquals("Status should match", "confirmed", confirmedRsvp.getStatus());
        assertNotNull("Timestamp should not be null", confirmedRsvp.getTimestamp());
    }

    @Test
    public void testSetters() {
        confirmedRsvp.setStatus("cancelled");

        assertEquals("Status should be updated", "cancelled", confirmedRsvp.getStatus());
        assertFalse("RSVP should no longer be confirmed", confirmedRsvp.isConfirmed());
    }

    @Test
    public void testStatusChange_ConfirmedToCancelled() {
        assertTrue("Initially should be confirmed", confirmedRsvp.isConfirmed());

        confirmedRsvp.setStatus("cancelled");

        assertFalse("After cancellation should not be confirmed", confirmedRsvp.isConfirmed());
        assertEquals("Status should be cancelled", "cancelled", confirmedRsvp.getStatus());
    }

    @Test
    public void testStatusChange_CancelledToConfirmed() {
        assertFalse("Initially should not be confirmed", cancelledRsvp.isConfirmed());

        cancelledRsvp.setStatus("confirmed");

        assertTrue("After re-confirmation should be confirmed", cancelledRsvp.isConfirmed());
        assertEquals("Status should be confirmed", "confirmed", cancelledRsvp.getStatus());
    }

    @Test
    public void testDefaultConstructor() {
        RSVP defaultRsvp = new RSVP();
        assertNotNull("Default constructor should create non-null object", defaultRsvp);
    }

    @Test
    public void testTimestamp() {
        Date now = new Date();
        Timestamp timestamp = new Timestamp(now);

        RSVP rsvp = new RSVP(
                "rsvp999",
                "student999",
                "event999",
                timestamp,
                "confirmed"
        );

        assertNotNull("Timestamp should not be null", rsvp.getTimestamp());
        assertEquals("Timestamp should match", timestamp, rsvp.getTimestamp());
    }

    @Test
    public void testStatusChecking_CaseInsensitive() {
        RSVP upperCaseRsvp = new RSVP(
                "rsvp777",
                "student777",
                "event777",
                testTimestamp,
                "CONFIRMED"
        );

        assertTrue("Status checking should be case-insensitive",
                upperCaseRsvp.isConfirmed());
    }

    @Test
    public void testStatusChecking_WithSpaces() {
        RSVP spacedRsvp = new RSVP(
                "rsvp888",
                "student888",
                "event888",
                testTimestamp,
                " confirmed "
        );

        assertTrue("Status checking should handle whitespace",
                spacedRsvp.isConfirmed());
    }

    @Test
    public void testMultipleRSVPs_SameStudent() {
        RSVP rsvp1 = new RSVP(
                "rsvp1",
                "student123",
                "event1",
                testTimestamp,
                "confirmed"
        );

        RSVP rsvp2 = new RSVP(
                "rsvp2",
                "student123",
                "event2",
                testTimestamp,
                "confirmed"
        );

        assertEquals("Student ID should be same",
                rsvp1.getStudentId(), rsvp2.getStudentId());
        assertNotEquals("Event ID should be different",
                rsvp1.getEventId(), rsvp2.getEventId());
        assertNotEquals("RSVP ID should be different",
                rsvp1.getRsvpId(), rsvp2.getRsvpId());
    }

    @Test
    public void testRSVPIdentifiers() {
        assertNotNull("RSVP ID should not be null", confirmedRsvp.getRsvpId());
        assertNotNull("Student ID should not be null", confirmedRsvp.getStudentId());
        assertNotNull("Event ID should not be null", confirmedRsvp.getEventId());

        assertFalse("RSVP ID should not be empty", confirmedRsvp.getRsvpId().isEmpty());
        assertFalse("Student ID should not be empty", confirmedRsvp.getStudentId().isEmpty());
        assertFalse("Event ID should not be empty", confirmedRsvp.getEventId().isEmpty());
    }
}