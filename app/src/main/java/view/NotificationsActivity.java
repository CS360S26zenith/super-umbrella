package com.example.campuseventstest.view;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuseventstest.R;
import com.example.campuseventstest.model.AppNotification;
import com.example.campuseventstest.service.FirestoreService;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

/**
 * In-app inbox for Firestore notifications saved for the current user.
 */
public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar loadingBar;
    private TextView emptyText;
    private NotificationsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        recyclerView = findViewById(R.id.notifications_recycler);
        loadingBar = findViewById(R.id.notifications_loading);
        emptyText = findViewById(R.id.notifications_empty);

        adapter = new NotificationsAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Please sign in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadNotifications();
    }

    private void loadNotifications() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        loadingBar.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);

        new FirestoreService().getNotificationsForUser(uid, new FirestoreService.NotificationListCallback() {
            @Override
            public void onSuccess(List<AppNotification> notifications) {
                loadingBar.setVisibility(View.GONE);
                adapter.setItems(notifications);
                emptyText.setVisibility(notifications.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onFailure(String error) {
                loadingBar.setVisibility(View.GONE);
                Toast.makeText(NotificationsActivity.this, error, Toast.LENGTH_LONG).show();
                emptyText.setVisibility(View.VISIBLE);
                emptyText.setText("Could not load notifications.\n\n" + error);
            }
        });
    }
}
