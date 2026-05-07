package com.example.campuseventstest.model;

import com.google.firebase.Timestamp;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * JUnit tests for the Event model class.
 * Tests capacity calculations, fill percentage, and business logic.
 */
public class EventTest {

    private Event emptyEvent;
    private Event partialEvent;
    private Event fullEvent;
    private Timestamp testTimestamp;

    @Before
    public void setUp() {
        testTimestamp = new Timestamp(new Date());

        // Event with no RSVPs
        emptyEvent = new Event(
                "event1",
                "Tech Talk: AI and Machine Learning",
                "Join us for an exciting talk on the latest trends in AI",
                testTimestamp,
                "Main Auditorium",
                "Talks",
                100,
                0,
                "org123",
                "live"
        );

        // Event with 60% capacity
        partialEvent = new Event(
                "event2",
                "Basketball Tournament Finals",
                "Watch the championship game",
                testTimestamp,
                "Sports Complex",
                "Sports",
                150,
                90,
                "org456",
                "live"
        );

        // Event at full capacity
        fullEvent = new Event(
                "event3",
                "Drama Club Performance",
                "Annual theater production",
                testTimestamp,
                "University Theater",
                "Performances",
                50,
                50,
                "org789",
                "live"
        );
    }

    @Test
    public void testIsFull_WhenEmpty_ReturnsFalse() {
        assertFalse("Empty event should not be full", emptyEvent.isFull());
    }

    @Test
    public void testIsFull_WhenPartial_ReturnsFalse() {
        assertFalse("Partially filled event should not be full", partialEvent.isFull());
    }

    @Test
    public void testIsFull_WhenFull_ReturnsTrue() {
        assertTrue("Event at capacity should be full", fullEvent.isFull());
    }

    @Test
    public void testGetSpotsRemaining_EmptyEvent() {
        assertEquals("Empty event should have all spots available",
                100, emptyEvent.getSpotsRemaining());
    }

    @Test
    public void testGetSpotsRemaining_PartialEvent() {
        assertEquals("Partial event should have correct spots remaining",
                60, partialEvent.getSpotsRemaining());
    }

    @Test
    public void testGetSpotsRemaining_FullEvent() {
        assertEquals("Full event should have zero spots remaining",
                0, fullEvent.getSpotsRemaining());
    }

    @Test
    public void testGetFillPercentage_EmptyEvent() {
        assertEquals("Empty event should show 0% fill",
                0, emptyEvent.getFillPercentage());
    }

    @Test
    public void testGetFillPercentage_PartialEvent() {
        assertEquals("Partial event should show 60% fill",
                60, partialEvent.getFillPercentage());
    }

    @Test
    public void testGetFillPercentage_FullEvent() {
        assertEquals("Full event should show 100% fill",
                100, fullEvent.getFillPercentage());
    }

    @Test
    public void testGetFillPercentage_WithDecimal() {
        Event decimalEvent = new Event(
                "event4",
                "Workshop",
                "Coding workshop",
                testTimestamp,
                "Lab A",
                "Talks",
                30,
                7, // 7/30 = 23.33%
                "org999",
                "live"
        );

        assertEquals("Fill percentage should round correctly",
                23, decimalEvent.getFillPercentage());
    }

    @Test
    public void testGetters() {
        assertEquals("Event ID should match", "event1", emptyEvent.getEventId());
        assertEquals("Title should match", "Tech Talk: AI and Machine Learning", emptyEvent.getTitle());
        assertEquals("Description should match",
                "Join us for an exciting talk on the latest trends in AI",
                emptyEvent.getDescription());
        assertEquals("Venue should match", "Main Auditorium", emptyEvent.getVenue());
        assertEquals("Category should match", "Talks", emptyEvent.getCategory());
        assertEquals("Capacity should match", 100, emptyEvent.getCapacity());
        assertEquals("RSVP count should match", 0, emptyEvent.getRsvpCount());
        assertEquals("Organizer ID should match", "org123", emptyEvent.getOrganizerId());
        assertEquals("Status should match", "live", emptyEvent.getStatus());
    }

    @Test
    public void testSetters() {
        emptyEvent.setTitle("Updated Title");
        emptyEvent.setDescription("Updated Description");
        emptyEvent.setVenue("New Venue");
        emptyEvent.setCapacity(200);
        emptyEvent.setRsvpCount(50);
        emptyEvent.setStatus("cancelled");

        assertEquals("Title should be updated", "Updated Title", emptyEvent.getTitle());
        assertEquals("Description should be updated", "Updated Description", emptyEvent.getDescription());
        assertEquals("Venue should be updated", "New Venue", emptyEvent.getVenue());
        assertEquals("Capacity should be updated", 200, emptyEvent.getCapacity());
        assertEquals("RSVP count should be updated", 50, emptyEvent.getRsvpCount());
        assertEquals("Status should be updated", "cancelled", emptyEvent.getStatus());
    }

    @Test
    public void testCapacityLogic_OverRsvp() {
        Event overEvent = new Event(
                "event5",
                "Test Event",
                "Test",
                testTimestamp,
                "Test Venue",
                "Talks",
                100,
                120, // More RSVPs than capacity (data integrity issue)
                "org999",
                "live"
        );

        assertTrue("Event with RSVPs exceeding capacity should be marked as full",
                overEvent.isFull());
        assertTrue("Spots remaining should not be negative",
                overEvent.getSpotsRemaining() <= 0);
        assertTrue("Fill percentage should be at least 100%",
                overEvent.getFillPercentage() >= 100);
    }

    @Test
    public void testDefaultConstructor() {
        Event defaultEvent = new Event();
        assertNotNull("Default constructor should create non-null object", defaultEvent);
    }
}