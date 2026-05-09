package com.example.campuseventstest.view;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuseventstest.R;
import com.example.campuseventstest.model.Event;
import com.example.campuseventstest.service.FirestoreService;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Comparator;
import java.util.List;

/**
 * Displays the current student's RSVPed events as a personal schedule.
 */
public class MyCalendarActivity extends AppCompatActivity {
    private android.widget.TextView emptyText;
    private CalendarEventAdapter adapter;
    private FirestoreService firestoreService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_calendar);
        emptyText = findViewById(R.id.calendar_empty_text);
        RecyclerView recyclerView = findViewById(R.id.calendar_recycler);
        adapter = new CalendarEventAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        firestoreService = new FirestoreService();

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            finish();
            return;
        }
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firestoreService.getRsvpedEventsByStudent(userId, new FirestoreService.EventListCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                if (events.isEmpty()) {
                    emptyText.setText("No RSVPed events yet.");
                    return;
                }
                events.sort(Comparator.comparing(Event::getDateAsDate, Comparator.nullsLast(Comparator.naturalOrder())));
                emptyText.setText("Upcoming RSVPed events");
                adapter.submit(events);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(MyCalendarActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
