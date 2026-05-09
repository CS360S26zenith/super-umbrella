package com.example.campuseventstest.service;

import com.example.campuseventstest.model.AppNotification;
import com.example.campuseventstest.utils.Constants;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * Stores in-app notifications for reminders, updates, and organizer broadcasts.
 */
public class NotificationService {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface NotificationDispatchCallback {
        void onSuccess(int deliveredCount);
        void onFailure(String error);
    }

    public void notifyUser(String userId, String eventId, String title, String message, String type) {
        AppNotification notification = new AppNotification(userId, eventId, title, message, type);
        db.collection(Constants.COLLECTION_NOTIFICATIONS).add(notification);
    }

    public void notifyUsers(List<String> userIds, String eventId, String title, String message, String type) {
        for (String userId : userIds) {
            notifyUser(userId, eventId, title, message, type);
        }
    }

    public void notifyUsers(List<String> userIds, String eventId, String title,
            String message, String type, NotificationDispatchCallback callback) {
        if (userIds == null || userIds.isEmpty()) {
            callback.onSuccess(0);
            return;
        }
        WriteBatch batch = db.batch();
        for (String userId : userIds) {
            AppNotification notification = new AppNotification(userId, eventId, title, message, type);
            batch.set(db.collection(Constants.COLLECTION_NOTIFICATIONS).document(), notification);
        }
        batch.commit()
                .addOnSuccessListener(unused -> callback.onSuccess(userIds.size()))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}
