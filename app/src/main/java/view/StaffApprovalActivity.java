package com.example.campuseventstest.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campuseventstest.R;
import com.example.campuseventstest.model.Event;
import com.example.campuseventstest.service.FirestoreService;
import com.example.campuseventstest.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Staff screen for approving or rejecting draft events.
 */
public class StaffApprovalActivity extends AppCompatActivity {
    private final List<Event> pendingEvents = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_approval);
        ListView listView = findViewById(R.id.pending_events_list);
        FirestoreService firestoreService = new FirestoreService();

        firestoreService.getPendingEvents(new FirestoreService.EventListCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                pendingEvents.clear();
                pendingEvents.addAll(events);
                List<String> labels = new ArrayList<>();
                for (Event event : events) {
                    labels.add(event.getTitle() + " (" + event.getCategory() + ")");
                }
                listView.setAdapter(new ArrayAdapter<>(StaffApprovalActivity.this,
                        android.R.layout.simple_list_item_1, labels));
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(StaffApprovalActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Event event = pendingEvents.get(position);
            firestoreService.updateEventStatus(event.getEventId(), Constants.STATUS_LIVE,
                    new FirestoreService.SimpleCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(StaffApprovalActivity.this,
                                    "Event approved", Toast.LENGTH_SHORT).show();
                            recreate();
                        }

                        @Override
                        public void onFailure(String error) {
                            Toast.makeText(StaffApprovalActivity.this,
                                    error, Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            Event event = pendingEvents.get(position);
            firestoreService.updateEventStatus(event.getEventId(), Constants.STATUS_CANCELLED,
                    new FirestoreService.SimpleCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(StaffApprovalActivity.this,
                                    "Event rejected", Toast.LENGTH_SHORT).show();
                            recreate();
                        }

                        @Override
                        public void onFailure(String error) {
                            Toast.makeText(StaffApprovalActivity.this,
                                    error, Toast.LENGTH_SHORT).show();
                        }
                    });
            return true;
        });

        findViewById(R.id.open_analytics_button).setOnClickListener(v ->
                startActivity(new Intent(StaffApprovalActivity.this, AnalyticsActivity.class)));
    }
}
