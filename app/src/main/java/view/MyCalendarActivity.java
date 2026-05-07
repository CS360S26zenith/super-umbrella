package com.example.campuseventstest.view;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campuseventstest.R;
import com.example.campuseventstest.model.Event;
import com.example.campuseventstest.service.FirestoreService;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Displays the current student's RSVPed events as a personal schedule.
 */
public class MyCalendarActivity extends AppCompatActivity {
    private TextView calendarContent;
    private FirestoreService firestoreService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_calendar);
        calendarContent = findViewById(R.id.calendar_content);
        firestoreService = new FirestoreService();

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            finish();
            return;
        }
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        SimpleDateFormat format = new SimpleDateFormat("EEE, MMM dd hh:mm a", Locale.getDefault());
        firestoreService.getRsvpedEventsByStudent(userId, new FirestoreService.EventListCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                if (events.isEmpty()) {
                    calendarContent.setText("No RSVPed events yet.");
                    return;
                }
                StringBuilder builder = new StringBuilder();
                for (Event event : events) {
                    builder.append("- ").append(event.getTitle()).append("\n");
                    if (event.getDate() != null) {
                        builder.append("  ").append(format.format(event.getDate().toDate())).append("\n");
                    }
                    builder.append("  ").append(event.getVenue()).append("\n\n");
                }
                calendarContent.setText(builder.toString());
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(MyCalendarActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
