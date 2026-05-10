package com.example.campuseventstest.model;

/**
 * Campus society / club directory entry stored in Firestore {@code societies}.
 */
public class Society {

    private String id;
    private String name;
    private String acronym;
    /** Optional display hint (e.g. color index 0–7 for list icon tint). */
    private int accentIndex;

    public Society() {
    }

    public Society(String id, String name, String acronym, int accentIndex) {
        this.id = id;
        this.name = name;
        this.acronym = acronym;
        this.accentIndex = accentIndex;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }

    public int getAccentIndex() {
        return accentIndex;
    }

    public void setAccentIndex(int accentIndex) {
        this.accentIndex = accentIndex;
    }

    /** Spinner / list label: "Name (ACR)" */
    public String getDisplayLabel() {
        if (acronym != null && !acronym.trim().isEmpty()) {
            return name + " (" + acronym + ")";
        }
        return name != null ? name : "";
    }
}
