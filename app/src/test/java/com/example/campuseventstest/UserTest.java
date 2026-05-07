package com.example.campuseventstest.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * JUnit tests for the User model class.
 * Tests role checking and user properties.
 */
public class UserTest {

    private User studentUser;
    private User organizerUser;
    private User staffUser;

    @Before
    public void setUp() {
        studentUser = new User(
                "user123",
                "Alice Johnson",
                "alice.johnson@university.edu",
                "student"
        );

        organizerUser = new User(
                "user456",
                "Bob Smith",
                "bob.smith@university.edu",
                "organizer"
        );

        staffUser = new User(
                "user789",
                "Carol Davis",
                "carol.davis@university.edu",
                "staff"
        );
    }

    @Test
    public void testIsStudent_WithStudentRole_ReturnsTrue() {
        assertTrue("Student user should return true for isStudent()",
                studentUser.isStudent());
    }

    @Test
    public void testIsStudent_WithOrganizerRole_ReturnsFalse() {
        assertFalse("Organizer user should return false for isStudent()",
                organizerUser.isStudent());
    }

    @Test
    public void testIsStudent_WithStaffRole_ReturnsFalse() {
        assertFalse("Staff user should return false for isStudent()",
                staffUser.isStudent());
    }

    @Test
    public void testIsOrganizer_WithOrganizerRole_ReturnsTrue() {
        assertTrue("Organizer user should return true for isOrganizer()",
                organizerUser.isOrganizer());
    }

    @Test
    public void testIsOrganizer_WithStudentRole_ReturnsFalse() {
        assertFalse("Student user should return false for isOrganizer()",
                studentUser.isOrganizer());
    }

    @Test
    public void testIsOrganizer_WithStaffRole_ReturnsFalse() {
        assertFalse("Staff user should return false for isOrganizer()",
                staffUser.isOrganizer());
    }

    @Test
    public void testGetters() {
        assertEquals("UID should match", "user123", studentUser.getUid());
        assertEquals("Name should match", "Alice Johnson", studentUser.getName());
        assertEquals("Email should match", "alice.johnson@university.edu", studentUser.getEmail());
        assertEquals("Role should match", "student", studentUser.getRole());
    }

    @Test
    public void testSetters() {
        studentUser.setName("Alice Williams");
        studentUser.setEmail("alice.williams@university.edu");
        studentUser.setRole("organizer");

        assertEquals("Name should be updated", "Alice Williams", studentUser.getName());
        assertEquals("Email should be updated", "alice.williams@university.edu", studentUser.getEmail());
        assertEquals("Role should be updated", "organizer", studentUser.getRole());
        assertTrue("User with organizer role should return true for isOrganizer()",
                studentUser.isOrganizer());
    }

    @Test
    public void testRoleChecking_CaseInsensitive() {
        User upperCaseStudent = new User(
                "user999",
                "Test User",
                "test@university.edu",
                "STUDENT"
        );

        assertTrue("Role checking should be case-insensitive",
                upperCaseStudent.isStudent());
    }

    @Test
    public void testRoleChecking_WithSpaces() {
        User spacedStudent = new User(
                "user888",
                "Test User",
                "test@university.edu",
                " student "
        );

        assertTrue("Role checking should handle whitespace",
                spacedStudent.isStudent());
    }

    @Test
    public void testDefaultConstructor() {
        User defaultUser = new User();
        assertNotNull("Default constructor should create non-null object", defaultUser);
    }

    @Test
    public void testMultipleRoleTransitions() {
        User user = new User("test", "Test", "test@test.com", "student");

        assertTrue("Initially should be student", user.isStudent());
        assertFalse("Initially should not be organizer", user.isOrganizer());

        user.setRole("organizer");
        assertFalse("After change should not be student", user.isStudent());
        assertTrue("After change should be organizer", user.isOrganizer());

        user.setRole("staff");
        assertFalse("After second change should not be student", user.isStudent());
        assertFalse("After second change should not be organizer", user.isOrganizer());
    }

    @Test
    public void testEmailValidation() {
        assertEquals("Email should contain @",
                true, studentUser.getEmail().contains("@"));
        assertEquals("Email should contain university domain",
                true, studentUser.getEmail().contains("university.edu"));
    }
}