package com.example.campuseventstest.service;

import androidx.annotation.NonNull;

import com.example.campuseventstest.model.User;
import com.example.campuseventstest.utils.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles user authentication and user document management in Firestore.
 * Automatically creates and syncs user documents during registration.
 * <p>
 * Login requires a Firestore document at {@code users/{uid}} (same UID as Firebase Auth).
 * Users created only in the Firebase Authentication console need that document added manually,
 * or use {@link #register} so the profile is created automatically.
 */
public class AuthService {

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    public AuthService() {
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }

    public interface AuthCallback {
        void onSuccess(User user);
        void onFailure(String error);
    }

    /**
     * Registers a new user with email/password and creates Firestore user document.
     * @param email User's email address
     * @param password User's password (6+ chars)
     * @param name User's display name
     * @param role User's role ("student", "organizer", or "staff")
     * @param callback Called on success or failure
     */
    public void register(String email, String password, String name, String role,
                         final AuthCallback callback) {
        register(email, password, name, role, null, null, callback);
    }

    public void register(String email, String password, String name, String role,
            String organizationName, String organizationBio, final AuthCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful() && task.getResult().getUser() != null) {
                            FirebaseUser fbUser = task.getResult().getUser();
                            String uid = fbUser.getUid();

                            // Create User object
                            User user = new User(uid, name, email, role.toLowerCase());
                            user.setOrganizationName(organizationName);
                            user.setOrganizationBio(organizationBio);

                            // Save to Firestore using UID as document ID
                            saveUserToFirestore(user, callback);
                        } else {
                            String errorMsg = task.getException() != null
                                    ? task.getException().getMessage()
                                    : "Registration failed";
                            callback.onFailure(errorMsg);
                        }
                    }
                });
    }

    /**
     * Logs in an existing user and fetches their profile from Firestore.
     * <p>
     * Success requires a {@code users/{uid}} document (see {@link Constants#COLLECTION_USERS}).
     *
     * @param email User's email
     * @param password User's password
     * @param callback Called on success or failure
     */
    public void login(String email, String password, final AuthCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful() && task.getResult().getUser() != null) {
                            String uid = task.getResult().getUser().getUid();
                            // Fetch user profile from Firestore
                            fetchUserFromFirestore(uid, callback);
                        } else {
                            String errorMsg = task.getException() != null
                                    ? task.getException().getMessage()
                                    : "Login failed";
                            callback.onFailure(errorMsg);
                        }
                    }
                });
    }

    /**
     * Saves user document to Firestore using UID as document ID.
     * Document ID MUST be the user's UID for login to work.
     */
    private void saveUserToFirestore(final User user, final AuthCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("uid", user.getUid());
        data.put("name", user.getName());
        data.put("email", user.getEmail());
        data.put("role", user.getRole());
        data.put("organizationName", user.getOrganizationName());
        data.put("organizationBio", user.getOrganizationBio());

        // CRITICAL: Use uid as document ID
        db.collection(Constants.COLLECTION_USERS)
                .document(user.getUid())  // Document ID = UID
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    // Success: return the user object
                    callback.onSuccess(user);
                })
                .addOnFailureListener(e -> {
                    // Failed to save to Firestore
                    callback.onFailure("Failed to create profile: " + e.getMessage());
                });
    }

    /**
     * Fetches user document from Firestore by UID.
     * Document ID MUST match the UID from Firebase Auth.
     */
    private void fetchUserFromFirestore(String uid, final AuthCallback callback) {
        db.collection(Constants.COLLECTION_USERS)
                .document(uid)  // Fetch document where ID = uid
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        try {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null && user.getRole() != null) {
                                callback.onSuccess(user);
                            } else {
                                callback.onFailure("User profile incomplete (missing role)");
                            }
                        } catch (Exception e) {
                            callback.onFailure("Failed to parse user profile: " + e.getMessage());
                        }
                    } else {
                        // Document doesn't exist — user registered but profile wasn't created
                        callback.onFailure("User profile not found. Please try registering again.");
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onFailure("Failed to fetch profile: " + e.getMessage());
                });
    }

    /**
     * Returns the currently logged-in Firebase user, or null if not logged in.
     * @return FirebaseUser or null
     */
    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    /**
     * Logs out the current user from Firebase Auth.
     */
    public void logout() {
        auth.signOut();
    }

    public void syncPushToken() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            return;
        }
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> db.collection(Constants.COLLECTION_USERS)
                        .document(currentUser.getUid())
                        .update("fcmToken", token));
    }
}