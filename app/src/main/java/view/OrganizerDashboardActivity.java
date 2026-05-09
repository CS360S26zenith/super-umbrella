package com.example.campuseventstest.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuseventstest.R;
import com.example.campuseventstest.model.Event;
import com.example.campuseventstest.service.FirestoreService;
import com.example.campuseventstest.utils.Constants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for organizers to view and manage their events.
 * Implements US-17 (Organizer dashboard) with event list,
 * RSVP count display, and FAB for creating new events.
 */
public class OrganizerDashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private OrganizerEventAdapter eventAdapter;
    private FloatingActionButton createEventFab;
    private ProgressBar loadingBar;
    private Button broadcastButton;
    private Button exportAttendeesButton;
    private FirestoreService firestoreService;
    private FirebaseAuth auth;
    private List<Event> organizerEvents;
    private boolean isAuthorizedOrganizer;
    private long lastOrganizerLoadElapsedMs = 0L;
    private static final long MIN_ORGANIZER_RESUME_RELOAD_MS = 2500L;
    private boolean organizerLoadInFlight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_dashboard);

        firestoreService = new FirestoreService();
        auth = FirebaseAuth.getInstance();
        organizerEvents = new ArrayList<>();

        initializeViews();
        enforceOrganizerAccess();
    }

    private void enforceOrganizerAccess() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login first.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadingBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        createEventFab.setEnabled(false);

        String uid = auth.getCurrentUser().getUid();
        FirebaseFirestore.getInstance()
                .collection(Constants.COLLECTION_USERS)
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String role = documentSnapshot.getString("role");
                    if (Constants.ROLE_ORGANIZER.equalsIgnoreCase(role)) {
                        isAuthorizedOrganizer = true;
                        setupRecyclerView();
                        setupCreateButton();
                        createEventFab.setEnabled(true);
                        loadOrganizerEvents();
                    } else {
                        loadingBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Organizer access required.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    loadingBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Unable to verify role: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    /**
     * Initializes all view components by binding to XML layout IDs.
     */
    private void initializeViews() {
        recyclerView = findViewById(R.id.organizer_events_recycler_view);
        createEventFab = findViewById(R.id.create_event_fab);
        loadingBar = findViewById(R.id.organizer_loading_bar);
        broadcastButton = findViewById(R.id.broadcast_button);
        exportAttendeesButton = findViewById(R.id.export_attendees_button);
    }

    /**
     * Sets up the RecyclerView with adapter and layout manager.
     */
    private void setupRecyclerView() {
        eventAdapter = new OrganizerEventAdapter(this, organizerEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(eventAdapter);
    }

    /**
     * Sets up the FAB for navigating to the Create Event screen.
     */
    private void setupCreateButton() {
        createEventFab.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerDashboardActivity.this,
                    CreateEventActivity.class);
            startActivity(intent);
        });

        broadcastButton.setOnClickListener(v -> {
            if (organizerEvents.isEmpty()) {
                Toast.makeText(this, "No events available for broadcast.", Toast.LENGTH_SHORT).show();
                return;
            }
            showEventPicker("Broadcast message for which event?", event -> {
                Intent intent = new Intent(OrganizerDashboardActivity.this, BroadcastMessageActivity.class);
                intent.putExtra(Constants.EXTRA_EVENT_ID, event.getEventId());
                intent.putExtra(Constants.EXTRA_EVENT_TITLE, event.getTitle());
                startActivity(intent);
            });
        });

        exportAttendeesButton.setOnClickListener(v -> exportAttendeesForSelectedEvent());
    }

    private void exportAttendeesForSelectedEvent() {
        if (organizerEvents.isEmpty()) {
            Toast.makeText(this, "No events to export", Toast.LENGTH_SHORT).show();
            return;
        }
        showEventPicker("Export attendees for which event?", event ->
                firestoreService.getAttendeeIdsForEvent(event.getEventId(), new FirestoreService.UserIdListCallback() {
                    @Override
                    public void onSuccess(List<String> userIds) {
                        String csv = "eventId,eventTitle,studentId\n";
                        StringBuilder builder = new StringBuilder(csv);
                        for (String userId : userIds) {
                            builder.append(event.getEventId()).append(",")
                                    .append(escapeCsv(event.getTitle())).append(",")
                                    .append(userId).append("\n");
                        }
                        Intent share = new Intent(Intent.ACTION_SEND);
                        share.setType("text/csv");
                        share.putExtra(Intent.EXTRA_SUBJECT, "Attendees - " + event.getTitle());
                        share.putExtra(Intent.EXTRA_TEXT, builder.toString());
                        startActivity(Intent.createChooser(share, "Export attendee list"));
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(OrganizerDashboardActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    private interface EventSelectionCallback {
        void onSelected(Event event);
    }

    private void showEventPicker(String title, EventSelectionCallback callback) {
        CharSequence[] labels = new CharSequence[organizerEvents.size()];
        for (int i = 0; i < organizerEvents.size(); i++) {
            Event event = organizerEvents.get(i);
            labels[i] = event.getTitle() + " (" + event.getCategory() + ")";
        }
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setItems(labels, (dialog, which) -> callback.onSelected(organizerEvents.get(which)))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
                }

    /**
     * Loads all events created by the current organizer from Firestore.
     */
    private void loadOrganizerEvents() {
        if (auth.getCurrentUser() == null) return;
        if (organizerLoadInFlight) {
            return;
        }
        organizerLoadInFlight = true;

        String organizerId = auth.getCurrentUser().getUid();
        loadingBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        firestoreService.getEventsByOrganizer(organizerId, new FirestoreService.EventListCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                organizerLoadInFlight = false;
                loadingBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                lastOrganizerLoadElapsedMs = SystemClock.elapsedRealtime();
                organizerEvents = events;
                eventAdapter.updateEvents(events);

                if (events.isEmpty()) {
                    Toast.makeText(OrganizerDashboardActivity.this,
                            "No events yet. Tap + to create your first event!",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(String error) {
                organizerLoadInFlight = false;
                loadingBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                Toast.makeText(OrganizerDashboardActivity.this,
                        "Failed to load events: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isAuthorizedOrganizer) {
            return;
        }
        long now = SystemClock.elapsedRealtime();
        if (now - lastOrganizerLoadElapsedMs < MIN_ORGANIZER_RESUME_RELOAD_MS) {
            return;
        }
        loadOrganizerEvents();
    }
}