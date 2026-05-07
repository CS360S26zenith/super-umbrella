package com.example.campuseventstest.service;

import com.example.campuseventstest.model.AppNotification;
import com.example.campuseventstest.utils.Constants;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * Stores in-app notifications for reminders, updates, and organizer broadcasts.
 */
public class NotificationService {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void notifyUser(String userId, String eventId, String title, String message, String type) {
        AppNotification notification = new AppNotification(userId, eventId, title, message, type);
        db.collection(Constants.COLLECTION_NOTIFICATIONS).add(notification);
    }

    public void notifyUsers(List<String> userIds, String eventId, String title, String message, String type) {
        for (String userId : userIds) {
            notifyUser(userId, eventId, title, message, type);
        }
    }
}
