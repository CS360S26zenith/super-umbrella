package com.example.campuseventstest.view;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campuseventstest.R;
import com.example.campuseventstest.service.FirestoreService;
import com.example.campuseventstest.service.NotificationService;
import com.example.campuseventstest.utils.Constants;

import java.util.List;

/**
 * Allows organizers to send one broadcast message to all confirmed attendees.
 */
public class BroadcastMessageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast_message);

        EditText eventIdInput = findViewById(R.id.broadcast_event_id);
        EditText messageInput = findViewById(R.id.broadcast_message);
        Button sendButton = findViewById(R.id.send_broadcast_button);

        String prefilledId = getIntent().getStringExtra(Constants.EXTRA_EVENT_ID);
        if (prefilledId == null || prefilledId.isEmpty()) {
            prefilledId = getIntent().getStringExtra("eventId");
        }
        String prefilledTitle = getIntent().getStringExtra(Constants.EXTRA_EVENT_TITLE);
        if (prefilledTitle == null) {
            prefilledTitle = getIntent().getStringExtra("eventTitle");
        }
        if (prefilledId != null && !prefilledId.isEmpty()) {
            eventIdInput.setText(prefilledId);
            eventIdInput.setEnabled(false);
        }
        if (prefilledTitle != null && !prefilledTitle.isEmpty()) {
            setTitle("Broadcast — " + prefilledTitle);
        }

        FirestoreService firestoreService = new FirestoreService();
        NotificationService notificationService = new NotificationService();

        sendButton.setOnClickListener(v -> {
            String eventId = eventIdInput.getText().toString().trim();
            String message = messageInput.getText().toString().trim();
            if (eventId.isEmpty() || message.isEmpty()) {
                Toast.makeText(this, "Event ID and message are required", Toast.LENGTH_SHORT).show();
                return;
            }
            firestoreService.getAttendeeIdsForEvent(eventId, new FirestoreService.UserIdListCallback() {
                @Override
                public void onSuccess(List<String> userIds) {
                    notificationService.notifyUsers(userIds, eventId, "Organizer Broadcast", message, "broadcast");
                    Toast.makeText(BroadcastMessageActivity.this,
                            "Message sent to " + userIds.size() + " attendees",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(BroadcastMessageActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
