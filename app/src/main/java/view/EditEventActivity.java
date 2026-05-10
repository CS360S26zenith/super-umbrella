package com.example.campuseventstest.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campuseventstest.R;
import com.example.campuseventstest.model.Event;
import com.example.campuseventstest.model.Society;
import com.example.campuseventstest.service.FirestoreService;
import com.example.campuseventstest.service.NotificationService;
import com.example.campuseventstest.utils.Constants;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Activity for editing existing campus events.
 * Implements US-15 (Edit event) with pre-filled form,
 * capacity safety checks, and event deletion.
 */
public class EditEventActivity extends AppCompatActivity {

    private EditText titleInput;
    private EditText descriptionInput;
    private EditText venueInput;
    private EditText capacityInput;
    private Button datePickerButton;
    private Spinner categorySpinner;
    private Spinner societySpinner;
    private Button updateButton;
    private Button deleteButton;
    private ProgressBar loadingBar;

    private FirestoreService firestoreService;
    private String eventId;
    private Event currentEvent;
    private Calendar selectedDateTime;
    private SimpleDateFormat dateFormat;
    private NotificationService notificationService;
    private final List<Society> societyDirectory = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        firestoreService = new FirestoreService();
        notificationService = new NotificationService();
        selectedDateTime = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy 'at' hh:mm a", Locale.getDefault());

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
        enforceOrganizerAccess();
    }

    private void enforceOrganizerAccess() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Please login first.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadingBar.setVisibility(View.VISIBLE);
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance()
                .collection(Constants.COLLECTION_USERS)
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String role = documentSnapshot.getString("role");
                    if (Constants.ROLE_ORGANIZER.equalsIgnoreCase(role)) {
                        setupCategorySpinner();
                        setupDatePicker();
                        setupButtons();
                        loadSocietiesThenEvent();
                        updateButton.setEnabled(true);
                        deleteButton.setEnabled(true);
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
        titleInput = findViewById(R.id.edit_title_input);
        descriptionInput = findViewById(R.id.edit_description_input);
        venueInput = findViewById(R.id.edit_venue_input);
        capacityInput = findViewById(R.id.edit_capacity_input);
        datePickerButton = findViewById(R.id.edit_date_picker_button);
        societySpinner = findViewById(R.id.edit_society_spinner);
        categorySpinner = findViewById(R.id.edit_category_spinner);
        updateButton = findViewById(R.id.update_event_button);
        deleteButton = findViewById(R.id.delete_event_button);
        loadingBar = findViewById(R.id.edit_loading_bar);
    }

    /**
     * Sets up the category spinner with available event categories.
     */
    private void setupCategorySpinner() {
        String[] categories = {
                Constants.CATEGORY_TALKS,
                Constants.CATEGORY_SPORTS,
                Constants.CATEGORY_CLUBS,
                Constants.CATEGORY_PERFORMANCES
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
    }

    private void loadSocietiesThenEvent() {
        loadingBar.setVisibility(View.VISIBLE);
        firestoreService.getSocieties(new FirestoreService.SocietyListCallback() {
            @Override
            public void onSuccess(List<Society> societies) {
                societyDirectory.clear();
                if (societies != null) {
                    societyDirectory.addAll(societies);
                }
                List<String> labels = new ArrayList<>();
                for (Society s : societyDirectory) {
                    labels.add(s.getDisplayLabel());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        EditEventActivity.this,
                        R.layout.item_society_spinner_selected,
                        R.id.society_spinner_item_text,
                        labels
                );
                adapter.setDropDownViewResource(R.layout.item_society_spinner_dropdown);
                societySpinner.setAdapter(adapter);
                loadEventData();
            }

            @Override
            public void onFailure(String error) {
                loadingBar.setVisibility(View.GONE);
                Toast.makeText(EditEventActivity.this,
                        "Could not load societies: " + error,
                        Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    /**
     * Sets up the date and time picker button.
     */
    private void setupDatePicker() {
        datePickerButton.setOnClickListener(v -> showDatePicker());
    }

    /**
     * Shows the date picker dialog, then chains to time picker.
     */
    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    showTimePicker();
                },
                selectedDateTime.get(Calendar.YEAR),
                selectedDateTime.get(Calendar.MONTH),
                selectedDateTime.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    /**
     * Shows the time picker dialog and updates the button text.
     */
    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);
                    datePickerButton.setText(dateFormat.format(selectedDateTime.getTime()));
                },
                selectedDateTime.get(Calendar.HOUR_OF_DAY),
                selectedDateTime.get(Calendar.MINUTE),
                false
        );
        timePickerDialog.show();
    }

    /**
     * Sets up update and delete button click listeners.
     */
    private void setupButtons() {
        updateButton.setOnClickListener(v -> {
            if (validateInputs()) {
                updateEvent();
            }
        });

        deleteButton.setOnClickListener(v -> deleteEvent());
    }

    /**
     * Loads existing event data from Firestore and populates the form.
     */
    private void loadEventData() {
        loadingBar.setVisibility(View.VISIBLE);

        firestoreService.getEventById(eventId, new FirestoreService.EventCallback() {
            @Override
            public void onSuccess(Event event) {
                loadingBar.setVisibility(View.GONE);
                currentEvent = event;
                populateFields(event);
            }

            @Override
            public void onFailure(String error) {
                loadingBar.setVisibility(View.GONE);
                Toast.makeText(EditEventActivity.this,
                        "Failed to load event: " + error,
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    /**
     * Populates form fields with existing event data.
     *
     * @param event the event whose data to display
     */
    private void populateFields(Event event) {
        titleInput.setText(event.getTitle());
        descriptionInput.setText(event.getDescription());
        venueInput.setText(event.getVenue());
        capacityInput.setText(String.valueOf(event.getCapacity()));

        // Set date from Timestamp
        if (event.getDate() != null) {
            selectedDateTime.setTime(event.getDate().toDate());
            datePickerButton.setText(dateFormat.format(event.getDate().toDate()));
        }

        // Set category spinner selection
        String[] categories = {
                Constants.CATEGORY_TALKS,
                Constants.CATEGORY_SPORTS,
                Constants.CATEGORY_CLUBS,
                Constants.CATEGORY_PERFORMANCES
        };

        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equals(event.getCategory())) {
                categorySpinner.setSelection(i);
                break;
            }
        }

        if (event.getSocietyId() != null && !societyDirectory.isEmpty()) {
            for (int i = 0; i < societyDirectory.size(); i++) {
                if (event.getSocietyId().equals(societyDirectory.get(i).getId())) {
                    societySpinner.setSelection(i);
                    break;
                }
            }
        }
    }

    /**
     * Validates all form inputs before submission.
     *
     * @return true if all inputs are valid, false otherwise
     */
    private boolean validateInputs() {
        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String venue = venueInput.getText().toString().trim();
        String capacityStr = capacityInput.getText().toString().trim();

        if (title.isEmpty()) {
            titleInput.setError("Title is required");
            titleInput.requestFocus();
            return false;
        }

        if (description.isEmpty()) {
            descriptionInput.setError("Description is required");
            descriptionInput.requestFocus();
            return false;
        }

        if (venue.isEmpty()) {
            venueInput.setError("Venue is required");
            venueInput.requestFocus();
            return false;
        }

        if (capacityStr.isEmpty()) {
            capacityInput.setError("Capacity is required");
            capacityInput.requestFocus();
            return false;
        }

        if (!societyDirectory.isEmpty()) {
            int pos = societySpinner.getSelectedItemPosition();
            if (pos < 0 || pos >= societyDirectory.size()) {
                Toast.makeText(this, R.string.select_society_validation, Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        try {
            int capacity = Integer.parseInt(capacityStr);
            if (capacity <= 0) {
                capacityInput.setError("Capacity must be greater than 0");
                capacityInput.requestFocus();
                return false;
            }

            // Prevent reducing capacity below current RSVP count
            if (currentEvent != null && capacity < currentEvent.getRsvpCount()) {
                capacityInput.setError("Capacity cannot be less than current RSVPs (" +
                        currentEvent.getRsvpCount() + ")");
                capacityInput.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            capacityInput.setError("Invalid capacity number");
            capacityInput.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Updates the event in Firestore with the form data.
     */
    private void updateEvent() {
        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String venue = venueInput.getText().toString().trim();
        int capacity = Integer.parseInt(capacityInput.getText().toString().trim());
        String category = categorySpinner.getSelectedItem().toString();

        Date eventDate = selectedDateTime.getTime();
        Timestamp eventTimestamp = new Timestamp(eventDate);

        Event updatedEvent = new Event(
                eventId,
                title,
                description,
                eventTimestamp,
                venue,
                category,
                capacity,
                currentEvent.getRsvpCount(),
                currentEvent.getOrganizerId(),
                currentEvent.getStatus()
        );
        if (!societyDirectory.isEmpty()) {
            Society soc = societyDirectory.get(societySpinner.getSelectedItemPosition());
            updatedEvent.setSocietyId(soc.getId());
            updatedEvent.setSocietyName(soc.getName());
        } else {
            updatedEvent.setSocietyId(currentEvent.getSocietyId());
            updatedEvent.setSocietyName(currentEvent.getSocietyName());
        }

        loadingBar.setVisibility(View.VISIBLE);
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);

        firestoreService.updateEvent(updatedEvent, new FirestoreService.SimpleCallback() {
            @Override
            public void onSuccess() {
                loadingBar.setVisibility(View.GONE);
                firestoreService.getAttendeeIdsForEvent(eventId, new FirestoreService.UserIdListCallback() {
                    @Override
                    public void onSuccess(java.util.List<String> userIds) {
                        notificationService.notifyUsers(
                                userIds,
                                eventId,
                                "Event Updated",
                                updatedEvent.getTitle() + " has updated details.",
                                "event_update",
                                new NotificationService.NotificationDispatchCallback() {
                                    @Override
                                    public void onSuccess(int deliveredCount) {
                                        // Keep toast concise and avoid extra blocking UI work.
                                    }

                                    @Override
                                    public void onFailure(String error) {
                                        Toast.makeText(EditEventActivity.this,
                                                "Event updated, but notifications failed: " + error,
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(EditEventActivity.this,
                                "Event updated, but attendee lookup failed: " + error,
                                Toast.LENGTH_LONG).show();
                    }
                });
                Toast.makeText(EditEventActivity.this,
                        "Event updated successfully!",
                        Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(String error) {
                loadingBar.setVisibility(View.GONE);
                updateButton.setEnabled(true);
                deleteButton.setEnabled(true);
                Toast.makeText(EditEventActivity.this,
                        "Failed to update event: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Shows a confirmation dialog before deleting the event.
     */
    private void deleteEvent() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete this event? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> performDelete())
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Performs the actual event deletion in Firestore.
     */
    private void performDelete() {
        loadingBar.setVisibility(View.VISIBLE);
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);

        firestoreService.deleteEvent(eventId, new FirestoreService.SimpleCallback() {
            @Override
            public void onSuccess() {
                loadingBar.setVisibility(View.GONE);
                Toast.makeText(EditEventActivity.this,
                        "Event deleted successfully",
                        Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(String error) {
                loadingBar.setVisibility(View.GONE);
                updateButton.setEnabled(true);
                deleteButton.setEnabled(true);
                Toast.makeText(EditEventActivity.this,
                        "Failed to delete event: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}