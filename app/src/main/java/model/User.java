package com.example.campuseventstest.model;

/**
 * Represents a user of the Campus Event Discovery platform.
 * The role field determines whether the user is a student, organizer, or staff member,
 * which controls navigation routing and feature access.
 */
public class User {

    private String uid;
    private String name;
    private String email;
    private String role;
    private String organizationName;
    private String organizationBio;
    private String fcmToken;

    /**
     * Required empty constructor for Firestore deserialization.
     */
    public User() {
    }

    /**
     * Constructs a User with all required fields.
     *
     * @param uid   the Firebase Authentication UID
     * @param name  the user's display name
     * @param email the user's email address
     * @param role  the user's role (student, organizer, or staff)
     */
    public User(String uid, String name, String email, String role) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    /**
     * Checks if this user has the organizer role.
     *
     * @return true if role equals "organizer"
     */
    public boolean isOrganizer() {
        return role != null && "organizer".equalsIgnoreCase(role.trim());
    }

    /**
     * Checks if this user has the student role.
     *
     * @return true if role equals "student"
     */
    public boolean isStudent() {
        return role != null && "student".equalsIgnoreCase(role.trim());
    }

    /**
     * Gets the Firebase Authentication UID.
     *
     * @return the UID
     */
    public String getUid() {
        return uid;
    }

    /**
     * Sets the Firebase Authentication UID.
     *
     * @param uid the UID to set
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /**
     * Gets the user's display name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the user's display name.
     *
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the user's email address.
     *
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address.
     *
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the user's role string.
     *
     * @return the role (student, organizer, or staff)
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the user's role.
     *
     * @param role the role to set
     */
    public void setRole(String role) {
        this.role = role;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getOrganizationBio() {
        return organizationBio;
    }

    public void setOrganizationBio(String organizationBio) {
        this.organizationBio = organizationBio;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}