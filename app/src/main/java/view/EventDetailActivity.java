package com.example.campuseventstest.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.campuseventstest.R;
import com.example.campuseventstest.model.Event;
import com.example.campuseventstest.service.CalendarService;
import com.example.campuseventstest.service.FirestoreService;
import com.example.campuseventstest.service.NotificationService;
import com.example.campuseventstest.utils.Constants;
import com.example.campuseventstest.utils.NotificationHelper;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Activity for displaying detailed information about a specific event.
 * Implements US-04 (Event details), US-06 (RSVP to event),
 * US-07 (Cancel RSVP), US-08 (Capacity display),
 * and US-20 (Capacity limit enforcement).
 * <p>
 * Does not reload from {@code onResume}: a single load in {@code onCreate} plus explicit
 * refresh after RSVP/cancel avoids duplicate Firestore work and reduces UI jank.
 */
public class EventDetailActivity extends AppCompatActivity {

    private TextView titleText;
    private TextView descriptionText;
    private TextView dateText;
    private TextView venueText;
    private TextView organizingSocietyText;
    private TextView categoryText;
    private TextView capacityText;
    private ProgressBar capacityBar;
    private Button rsvpButton;
    private Button cancelRsvpButton;
    private Button joinWaitlistButton;
    private Button addToCalendarButton;
    private Button viewTicketButton;
    private ProgressBar loadingBar;

    private FirestoreService firestoreService;
    private FirebaseAuth auth;
    private String eventId;
    private Event currentEvent;
    private boolean hasRsvped = false;
    private CalendarService calendarService;
    private NotificationService notificationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        firestoreService = new FirestoreService();
        auth = FirebaseAuth.getInstance();
        calendarService = new CalendarService();
        notificationService = new NotificationService();

        eventId = getIntent().getStringExtra(Constants.EXTRA_EVENT_ID);
        if (eventId == null || eventId.isEmpty()) {
            eventId = getIntent().getStringExtra("eventId");
        }
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Invalid event", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        loadEventDetails();
    }

    /**
     * Initializes all view components by binding to XML layout IDs.
     */
    private void initializeViews() {
        titleText = findViewById(R.id.detail_title);
        descriptionText = findViewById(R.id.detail_description);
        dateText = findViewById(R.id.detail_date);
        venueText = findViewById(R.id.detail_venue);
        organizingSocietyText = findViewById(R.id.detail_organizing_society);
        categoryText = findViewById(R.id.detail_category);
        capacityText = findViewById(R.id.detail_capacity);
        capacityBar = findViewById(R.id.detail_capacity_bar);
        rsvpButton = findViewById(R.id.rsvp_button);
        cancelRsvpButton = findViewById(R.id.cancel_rsvp_button);
        joinWaitlistButton = findViewById(R.id.join_waitlist_button);
        addToCalendarButton = findViewById(R.id.add_to_calendar_button);
        viewTicketButton = findViewById(R.id.view_ticket_button);
        loadingBar = findViewById(R.id.detail_loading_bar);

        rsvpButton.setOnClickListener(v -> handleRsvp());
        cancelRsvpButton.setOnClickListener(v -> handleCancelRsvp());
        joinWaitlistButton.setOnClickListener(v -> handleJoinWaitlist());
        addToCalendarButton.setOnClickListener(v -> handleAddToCalendar());
        viewTicketButton.setOnClickListener(v -> openTicketScreen());
    }

    /**
     * Loads event details from Firestore and displays them.
     */
    private void loadEventDetails() {
        loadingBar.setVisibility(View.VISIBLE);

        firestoreService.getEventById(eventId, new FirestoreService.EventCallback() {
            @Override
            public void onSuccess(Event event) {
                loadingBar.setVisibility(View.GONE);
                currentEvent = event;
                displayEventDetails(event);
                checkRsvpStatus();
            }

            @Override
            public void onFailure(String error) {
                loadingBar.setVisibility(View.GONE);
                Toast.makeText(EventDetailActivity.this,
                        "Failed to load event: " + error,
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    /**
     * Displays all event details in the UI.
     *
     * @param event the event to display
     */
    private void displayEventDetails(Event event) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy 'at' hh:mm a",
                Locale.getDefault());

        titleText.setText(event.getTitle());
        descriptionText.setText(event.getDescription());

        if (event.getDate() != null) {
            dateText.setText(dateFormat.format(event.getDate().toDate()));
        }

        venueText.setText(event.getVenue());

        String societyLine = event.getSocietyName();
        if (societyLine == null || societyLine.trim().isEmpty()) {
            societyLine = getString(R.string.detail_organized_by_unknown);
        }
        organizingSocietyText.setText(societyLine);

        categoryText.setText(event.getCategory());

        int fillPercentage = event.getFillPercentage();
        int spotsRemaining = event.getSpotsRemaining();

        capacityText.setText(event.getRsvpCount() + "/" + event.getCapacity() +
                " (" + fillPercentage + "% full, " +
                spotsRemaining + " spots remaining)");
        capacityBar.setProgress(fillPercentage);
    }

    /**
     * Checks if the current user has already RSVP'd to this event.
     * Uses BooleanCallback to get a true/false result.
     */
    private void checkRsvpStatus() {
        if (auth.getCurrentUser() == null) return;

        String userId = auth.getCurrentUser().getUid();

        firestoreService.hasUserRsvped(userId, eventId, new FirestoreService.BooleanCallback() {
            @Override
            public void onResult(boolean result) {
                hasRsvped = result;
                updateRsvpButtons();
            }

            @Override
            public void onFailure(String error) {
                // Default to showing RSVP button on error
                updateRsvpButtons();
            }
        });
    }

    /**
     * Updates RSVP button visibility based on current status and capacity.
     * Implements US-20 (Capacity limit enforcement).
     */
    private void updateRsvpButtons() {
        if (currentEvent == null) {
            rsvpButton.setVisibility(View.GONE);
            cancelRsvpButton.setVisibility(View.GONE);
            joinWaitlistButton.setVisibility(View.GONE);
            return;
        }

        if (auth.getCurrentUser() != null
                && auth.getCurrentUser().getUid().equals(currentEvent.getOrganizerId())) {
            rsvpButton.setVisibility(View.GONE);
            cancelRsvpButton.setVisibility(View.GONE);
            joinWaitlistButton.setVisibility(View.GONE);
            viewTicketButton.setVisibility(View.GONE);
            return;
        }

        boolean eventPassed = currentEvent.getDate() != null
                && currentEvent.getDate().toDate().before(new Date());

        if (hasRsvped) {
            rsvpButton.setVisibility(View.GONE);
            cancelRsvpButton.setVisibility(View.VISIBLE);
            joinWaitlistButton.setVisibility(View.GONE);
            viewTicketButton.setVisibility(View.VISIBLE);
        } else {
            cancelRsvpButton.setVisibility(View.GONE);
            viewTicketButton.setVisibility(View.GONE);

            if (eventPassed) {
                rsvpButton.setVisibility(View.VISIBLE);
                rsvpButton.setEnabled(false);
                rsvpButton.setText("Event Passed");
                joinWaitlistButton.setVisibility(View.GONE);
            } else if (currentEvent.isFull()) {
                rsvpButton.setVisibility(View.VISIBLE);
                rsvpButton.setEnabled(false);
                rsvpButton.setText("Event Full");
                joinWaitlistButton.setVisibility(View.VISIBLE);
                joinWaitlistButton.setEnabled(true);
                joinWaitlistButton.setText("Join Waitlist");
            } else {
                rsvpButton.setVisibility(View.VISIBLE);
                rsvpButton.setEnabled(true);
                rsvpButton.setText("RSVP to Event");
                // US-09: keep waitlist visible so testers know the feature exists; enabled only when full.
                joinWaitlistButton.setVisibility(View.VISIBLE);
                joinWaitlistButton.setEnabled(false);
                joinWaitlistButton.setText("Waitlist (when event is full)");
            }
        }
    }

    /**
     * Handles RSVP button click. Creates RSVP in Firestore.
     * Implements US-06 (RSVP to event).
     */
    private void handleRsvp() {
        if (currentEvent == null || currentEvent.isFull()) {
            Toast.makeText(this, "Event is full", Toast.LENGTH_SHORT).show();
            return;
        }

        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();
        rsvpButton.setEnabled(false);

        if (currentEvent.getDate() != null && currentEvent.getDate().toDate().before(new Date())) {
            rsvpButton.setEnabled(true);
            Toast.makeText(this, "Cannot RSVP to past events", Toast.LENGTH_SHORT).show();
            return;
        }
        if (userId.equals(currentEvent.getOrganizerId())) {
            rsvpButton.setEnabled(true);
            Toast.makeText(this, "Organizers cannot RSVP to their own event", Toast.LENGTH_SHORT).show();
            return;
        }

        firestoreService.rsvpToEventSafe(userId, currentEvent, new FirestoreService.SimpleCallback() {
            @Override
            public void onSuccess() {
                hasRsvped = true;
                Toast.makeText(EventDetailActivity.this,
                        "RSVP successful!",
                        Toast.LENGTH_SHORT).show();
                firestoreService.generateTicketForRsvp(userId, eventId, new FirestoreService.StringCallback() {
                    @Override
                    public void onSuccess(String value) {
                        // Ticket created silently; user can open it from button.
                    }

                    @Override
                    public void onFailure(String error) {
                        // Keep RSVP successful even if ticket generation fails.
                    }
                });
                notificationService.notifyUser(
                        userId,
                        eventId,
                        "Event Reminder Set",
                        "You will be reminded before " + currentEvent.getTitle(),
                        "event_reminder"
                );
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                        || ContextCompat.checkSelfPermission(EventDetailActivity.this,
                        Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    NotificationHelper.showTrayNotification(EventDetailActivity.this,
                            "RSVP confirmed",
                            "You're registered for " + currentEvent.getTitle());
                }
                loadEventDetails();
            }

            @Override
            public void onFailure(String error) {
                rsvpButton.setEnabled(true);
                Toast.makeText(EventDetailActivity.this,
                        "RSVP failed: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleJoinWaitlist() {
        if (auth.getCurrentUser() == null) {
            return;
        }
        if (currentEvent == null || !currentEvent.isFull()) {
            Toast.makeText(this, "Waitlist opens only after the event reaches full capacity.",
                    Toast.LENGTH_LONG).show();
            return;
        }
        String userId = auth.getCurrentUser().getUid();
        joinWaitlistButton.setEnabled(false);
        firestoreService.joinWaitlist(userId, eventId, new FirestoreService.WaitlistCallback() {
            @Override
            public void onSuccess(int position) {
                joinWaitlistButton.setEnabled(true);
                Toast.makeText(EventDetailActivity.this,
                        "Joined waitlist. Position: " + position,
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(String error) {
                joinWaitlistButton.setEnabled(true);
                Toast.makeText(EventDetailActivity.this,
                        "Waitlist failed: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleAddToCalendar() {
        if (currentEvent == null) {
            return;
        }
        startActivity(calendarService.buildCalendarInsertIntent(currentEvent));
    }

    private void openTicketScreen() {
        if (auth.getCurrentUser() == null) {
            return;
        }
        Intent intent = new Intent(this, TicketActivity.class);
        intent.putExtra(Constants.EXTRA_EVENT_ID, eventId);
        intent.putExtra(Constants.EXTRA_EVENT_TITLE, currentEvent != null ? currentEvent.getTitle() : "");
        startActivity(intent);
    }

    /**
     * Handles cancel RSVP button click. Updates RSVP status in Firestore.
     * Implements US-07 (Cancel RSVP).
     */
    private void handleCancelRsvp() {
        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();
        cancelRsvpButton.setEnabled(false);

        firestoreService.cancelRsvp(userId, eventId, new FirestoreService.SimpleCallback() {
            @Override
            public void onSuccess() {
                hasRsvped = false;
                Toast.makeText(EventDetailActivity.this,
                        "RSVP cancelled",
                        Toast.LENGTH_SHORT).show();
                loadEventDetails();
            }

            @Override
            public void onFailure(String error) {
                cancelRsvpButton.setEnabled(true);
                Toast.makeText(EventDetailActivity.this,
                        "Cancellation failed: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}