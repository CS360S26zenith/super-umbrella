package com.example.campuseventstest.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuseventstest.R;
import com.example.campuseventstest.model.Event;
import com.example.campuseventstest.service.FirestoreService;
import com.example.campuseventstest.utils.Constants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

/**
 * Organizer home tab — manage events, broadcast, export (same capabilities as legacy dashboard).
 */
public class OrganizerHomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private OrganizerEventAdapter eventAdapter;
    private FloatingActionButton createEventFab;
    private ProgressBar loadingBar;
    private Button broadcastButton;
    private Button exportAttendeesButton;
    private FirestoreService firestoreService;
    private FirebaseAuth auth;
    private List<Event> organizerEvents = new ArrayList<>();
    private long lastOrganizerLoadElapsedMs;
    private static final long MIN_ORGANIZER_RESUME_RELOAD_MS = 2500L;
    private boolean organizerLoadInFlight;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_organizer_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);

        firestoreService = new FirestoreService();
        auth = FirebaseAuth.getInstance();

        recyclerView = root.findViewById(R.id.organizer_events_recycler_view);
        createEventFab = root.findViewById(R.id.create_event_fab);
        loadingBar = root.findViewById(R.id.organizer_loading_bar);
        broadcastButton = root.findViewById(R.id.broadcast_button);
        exportAttendeesButton = root.findViewById(R.id.export_attendees_button);

        setupRecyclerView();
        setupCreateButton();
        loadOrganizerEvents();
    }

    private void setupRecyclerView() {
        eventAdapter = new OrganizerEventAdapter(requireContext(), organizerEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(eventAdapter);
    }

    private void setupCreateButton() {
        createEventFab.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CreateEventActivity.class)));

        broadcastButton.setOnClickListener(v -> {
            if (organizerEvents.isEmpty()) {
                Toast.makeText(requireContext(), "No events available for broadcast.", Toast.LENGTH_SHORT).show();
                return;
            }
            showEventPicker("Broadcast message for which event?", event -> {
                Intent intent = new Intent(requireContext(), BroadcastMessageActivity.class);
                intent.putExtra(Constants.EXTRA_EVENT_ID, event.getEventId());
                intent.putExtra(Constants.EXTRA_EVENT_TITLE, event.getTitle());
                startActivity(intent);
            });
        });

        exportAttendeesButton.setOnClickListener(v -> exportAttendeesForSelectedEvent());
    }

    private void exportAttendeesForSelectedEvent() {
        if (organizerEvents.isEmpty()) {
            Toast.makeText(requireContext(), "No events to export", Toast.LENGTH_SHORT).show();
            return;
        }
        showEventPicker("Export attendees for which event?", event ->
                firestoreService.getAttendeeIdsForEvent(event.getEventId(),
                        new FirestoreService.UserIdListCallback() {
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
                                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
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
        new AlertDialog.Builder(requireContext())
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

    private void loadOrganizerEvents() {
        if (auth.getCurrentUser() == null) {
            return;
        }
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
                    Toast.makeText(requireContext(),
                            "No events yet. Tap + to create your first event!",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(String error) {
                organizerLoadInFlight = false;
                loadingBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                Toast.makeText(requireContext(),
                        "Failed to load events: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        long now = SystemClock.elapsedRealtime();
        if (now - lastOrganizerLoadElapsedMs < MIN_ORGANIZER_RESUME_RELOAD_MS) {
            return;
        }
        loadOrganizerEvents();
    }
}
