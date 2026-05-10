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
 * Activity for creating new campus events.
 * Implements US-14 (Create event) with full form validation,
 * date/time picker, and category selection.
 */
public class CreateEventActivity extends AppCompatActivity {

    private EditText titleInput;
    private EditText descriptionInput;
    private EditText venueInput;
    private EditText capacityInput;
    private Button datePickerButton;
    private Spinner categorySpinner;
    private Spinner societySpinner;
    private Button submitButton;
    private ProgressBar loadingBar;

    private FirestoreService firestoreService;
    private final List<Society> societyDirectory = new ArrayList<>();
    private Calendar selectedDateTime;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        firestoreService = new FirestoreService();
        selectedDateTime = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy 'at' hh:mm a", Locale.getDefault());

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
        submitButton.setEnabled(false);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance()
                .collection(Constants.COLLECTION_USERS)
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String role = documentSnapshot.getString("role");
                    if (Constants.ROLE_ORGANIZER.equalsIgnoreCase(role)) {
                        loadingBar.setVisibility(View.GONE);
                        setupCategorySpinner();
                        setupDatePicker();
                        setupSubmitButton();
                        loadSocietyDirectory();
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
        titleInput = findViewById(R.id.create_title_input);
        descriptionInput = findViewById(R.id.create_description_input);
        venueInput = findViewById(R.id.create_venue_input);
        capacityInput = findViewById(R.id.create_capacity_input);
        datePickerButton = findViewById(R.id.date_picker_button);
        societySpinner = findViewById(R.id.society_spinner);
        categorySpinner = findViewById(R.id.category_spinner);
        submitButton = findViewById(R.id.submit_event_button);
        loadingBar = findViewById(R.id.create_loading_bar);
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

    private void loadSocietyDirectory() {
        loadingBar.setVisibility(View.VISIBLE);
        submitButton.setEnabled(false);
        firestoreService.getSocieties(new FirestoreService.SocietyListCallback() {
            @Override
            public void onSuccess(List<Society> societies) {
                loadingBar.setVisibility(View.GONE);
                societyDirectory.clear();
                if (societies != null) {
                    societyDirectory.addAll(societies);
                }
                List<String> labels = new ArrayList<>();
                for (Society s : societyDirectory) {
                    labels.add(s.getDisplayLabel());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        CreateEventActivity.this,
                        R.layout.item_society_spinner_selected,
                        R.id.society_spinner_item_text,
                        labels
                );
                adapter.setDropDownViewResource(R.layout.item_society_spinner_dropdown);
                societySpinner.setAdapter(adapter);
                if (societyDirectory.isEmpty()) {
                    Toast.makeText(CreateEventActivity.this,
                            "No societies found. Seed the societies collection in Firebase first.",
                            Toast.LENGTH_LONG).show();
                    submitButton.setEnabled(false);
                } else {
                    submitButton.setEnabled(true);
                }
            }

            @Override
            public void onFailure(String error) {
                loadingBar.setVisibility(View.GONE);
                Toast.makeText(CreateEventActivity.this,
                        "Could not load societies: " + error,
                        Toast.LENGTH_LONG).show();
                submitButton.setEnabled(false);
            }
        });
    }

    /**
     * Sets up the date and time picker button.
     */
    private void setupDatePicker() {
        datePickerButton.setText(dateFormat.format(selectedDateTime.getTime()));
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
     * Sets up the submit button click listener.
     */
    private void setupSubmitButton() {
        submitButton.setOnClickListener(v -> {
            if (validateInputs()) {
                createEvent();
            }
        });
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

        if (societyDirectory.isEmpty() || societySpinner.getSelectedItemPosition() < 0
                || societySpinner.getSelectedItemPosition() >= societyDirectory.size()) {
            Toast.makeText(this, R.string.select_society_validation, Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            int capacity = Integer.parseInt(capacityStr);
            if (capacity <= 0) {
                capacityInput.setError("Capacity must be greater than 0");
                capacityInput.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            capacityInput.setError("Invalid capacity number");
            capacityInput.requestFocus();
            return false;
        }

        if (selectedDateTime.getTimeInMillis() <= System.currentTimeMillis()) {
            Toast.makeText(this, "Event date must be in the future", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * Creates a new event in Firestore. Status is set to {@link Constants#STATUS_LIVE}
     * (published immediately; staff approval flow not enforced here).
     */
    private void createEvent() {
        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String venue = venueInput.getText().toString().trim();
        int capacity = Integer.parseInt(capacityInput.getText().toString().trim());
        String category = categorySpinner.getSelectedItem().toString();
        String organizerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Date eventDate = selectedDateTime.getTime();
        Timestamp eventTimestamp = new Timestamp(eventDate);

        Event event = new Event(title, description, eventTimestamp, venue,
                category, capacity, organizerId);
        Society soc = societyDirectory.get(societySpinner.getSelectedItemPosition());
        event.setSocietyId(soc.getId());
        event.setSocietyName(soc.getName());

        loadingBar.setVisibility(View.VISIBLE);
        submitButton.setEnabled(false);

        firestoreService.createEvent(event, false, new FirestoreService.SimpleCallback() {
            @Override
            public void onSuccess() {
                loadingBar.setVisibility(View.GONE);
                Toast.makeText(CreateEventActivity.this,
                        R.string.event_created_live,
                        Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(String error) {
                loadingBar.setVisibility(View.GONE);
                submitButton.setEnabled(true);
                Toast.makeText(CreateEventActivity.this,
                        "Failed to create event: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}