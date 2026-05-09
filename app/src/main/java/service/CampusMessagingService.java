package com.example.campuseventstest.service;

import com.example.campuseventstest.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.firestore.FirebaseFirestore;

public class CampusMessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return;
        }
        FirebaseFirestore.getInstance().collection(Constants.COLLECTION_USERS)
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .update("fcmToken", token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        // In-app persistence already uses NotificationService/Firestore documents.
    }
}
