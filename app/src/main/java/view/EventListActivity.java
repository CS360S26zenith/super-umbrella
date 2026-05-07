package com.example.campuseventstest.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuseventstest.R;
import com.example.campuseventstest.model.Event;
import com.example.campuseventstest.service.FirestoreService;
import com.example.campuseventstest.service.RecommendationService;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Activity for browsing and discovering campus events.
 * Implements US-01 (Event browsing), US-02 (Event search),
 * and US-03 (Event filtering by category).
 */
public class EventListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private EditText searchBar;
    private ChipGroup categoryChips;
    private ProgressBar loadingBar;
    private TextView emptyStateText;
    private View recommendationsButton;
    private View myCalendarButton;
    private View notificationsInboxButton;

    private static final int REQ_POST_NOTIFICATIONS = 9101;
    private FirestoreService firestoreService;
    private RecommendationService recommendationService;
    private List<Event> allEvents;
    private String currentCategory = "";
    private String currentSearchQuery = "";
    private boolean skipNextResumeReload = true;
    /** Throttle resume reloads so rapid list ↔ detail navigation does not stack Firestore work. */
    private long lastEventsLoadElapsedMs = 0L;
    private static final long MIN_RESUME_RELOAD_INTERVAL_MS = 2500L;
    private static final long SEARCH_DEBOUNCE_MS = 200L;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchDebounceRunnable;
    private boolean eventsLoadInFlight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        firestoreService = new FirestoreService();
        recommendationService = new RecommendationService();
        allEvents = new ArrayList<>();

        initializeViews();
        setupRecyclerView();
        setupSearchBar();
        setupCategoryFilters();
        requestNotificationPermissionIfNeeded();
        loadEvents();
    }

    /** Android 13+: tray notifications need runtime permission. */
    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{ Manifest.permission.POST_NOTIFICATIONS },
                        REQ_POST_NOTIFICATIONS);
            }
        }
    }

    /**
     * Initializes all view components by binding to XML layout IDs.
     */
    private void initializeViews() {
        recyclerView = findViewById(R.id.events_recycler_view);
        searchBar = findViewById(R.id.search_bar);
        categoryChips = findViewById(R.id.category_chips);
        loadingBar = findViewById(R.id.loading_bar);
        emptyStateText = findViewById(R.id.empty_state_text);
        recommendationsButton = findViewById(R.id.recommendations_button);
        myCalendarButton = findViewById(R.id.my_calendar_button);
        notificationsInboxButton = findViewById(R.id.notifications_inbox_button);
        recommendationsButton.setOnClickListener(v -> showRecommendations());
        myCalendarButton.setOnClickListener(v ->
                startActivity(new Intent(EventListActivity.this, MyCalendarActivity.class)));
        notificationsInboxButton.setOnClickListener(v ->
                startActivity(new Intent(EventListActivity.this, NotificationsActivity.class)));
    }

    /**
     * Sets up the RecyclerView with adapter and layout manager.
     */
    private void setupRecyclerView() {
        eventAdapter = new EventAdapter(this, allEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(eventAdapter);
    }

    /**
     * Sets up the search bar with real-time text change listener.
     * Implements US-02 (Event search).
     */
    private void setupSearchBar() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString();
                if (searchDebounceRunnable != null) {
                    searchHandler.removeCallbacks(searchDebounceRunnable);
                }
                searchDebounceRunnable = EventListActivity.this::applyFilters;
                searchHandler.postDelayed(searchDebounceRunnable, SEARCH_DEBOUNCE_MS);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     * Sets up category filter chips with selection listener.
     * Implements US-03 (Event filtering by category).
     */
    private void setupCategoryFilters() {
        categoryChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentCategory = "";
            } else {
                int checkedId = checkedIds.get(0);
                Chip selectedChip = findViewById(checkedId);
                if (selectedChip != null) {
                    String chipText = selectedChip.getText().toString();
                    if ("All".equalsIgnoreCase(chipText)) {
                        currentCategory = "";
                    } else {
                        currentCategory = chipText;
                    }
                }
            }
            applyFilters();
        });
    }

    /**
     * Loads all live events from Firestore.
     * Implements US-01 (Event browsing).
     */
    private void loadEvents() {
        if (eventsLoadInFlight) {
            return;
        }
        eventsLoadInFlight = true;
        loadingBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.GONE);

        firestoreService.getLiveEvents(new FirestoreService.EventListCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                eventsLoadInFlight = false;
                loadingBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                lastEventsLoadElapsedMs = SystemClock.elapsedRealtime();
                allEvents = events;
                eventAdapter.updateEvents(events);
                applyFilters();
            }

            @Override
            public void onFailure(String error) {
                eventsLoadInFlight = false;
                loadingBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                emptyStateText.setVisibility(View.GONE);
                Toast.makeText(EventListActivity.this,
                        "Failed to load events: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Builds the event list after applying category + search filters (same rules as browse).
     */
    private List<Event> buildFilteredEventList() {
        List<Event> filteredEvents = new ArrayList<>(allEvents);

        if (!currentCategory.isEmpty()) {
            List<Event> categoryFiltered = new ArrayList<>();
            for (Event event : filteredEvents) {
                if (event.getCategory() != null &&
                        event.getCategory().equalsIgnoreCase(currentCategory)) {
                    categoryFiltered.add(event);
                }
            }
            filteredEvents = categoryFiltered;
        }

        if (!currentSearchQuery.isEmpty()) {
            List<Event> searchFiltered = new ArrayList<>();
            String lowerQuery = currentSearchQuery.toLowerCase();
            for (Event event : filteredEvents) {
                boolean matchesTitle = event.getTitle() != null &&
                        event.getTitle().toLowerCase().contains(lowerQuery);
                boolean matchesDesc = event.getDescription() != null &&
                        event.getDescription().toLowerCase().contains(lowerQuery);
                boolean matchesVenue = event.getVenue() != null &&
                        event.getVenue().toLowerCase().contains(lowerQuery);

                if (matchesTitle || matchesDesc || matchesVenue) {
                    searchFiltered.add(event);
                }
            }
            filteredEvents = searchFiltered;
        }

        return filteredEvents;
    }

    /**
     * Applies both search and category filters to the event list.
     * Combines US-02 (search) and US-03 (category filter) functionality.
     */
    private void applyFilters() {
        List<Event> filteredEvents = buildFilteredEventList();
        eventAdapter.updateEvents(filteredEvents);
        emptyStateText.setVisibility(filteredEvents.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showRecommendations() {
        if (searchDebounceRunnable != null) {
            searchHandler.removeCallbacks(searchDebounceRunnable);
            searchDebounceRunnable = null;
        }

        List<Event> pool = buildFilteredEventList();
        if (pool.isEmpty()) {
            Toast.makeText(this,
                    allEvents.isEmpty()
                            ? "Events are still loading or none are live yet."
                            : "No events match your search or category filters.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (!currentCategory.isEmpty()) {
            applyRankedRecommendations(pool, Collections.singletonList(currentCategory));
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            applyRankedRecommendations(pool, new ArrayList<>());
            return;
        }

        firestoreService.getRsvpedEventsByStudent(user.getUid(), new FirestoreService.EventListCallback() {
            @Override
            public void onSuccess(List<Event> pastRsvped) {
                applyRankedRecommendations(pool, categoriesFromPastRsvps(pastRsvped));
            }

            @Override
            public void onFailure(String error) {
                applyRankedRecommendations(pool, new ArrayList<>());
            }
        });
    }

    private void applyRankedRecommendations(List<Event> pool, List<String> preferredCategories) {
        List<Event> ranked = recommendationService.rankEvents(pool, preferredCategories);
        loadingBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        eventAdapter.updateEvents(ranked);
        emptyStateText.setVisibility(View.GONE);
        Toast.makeText(this, "Recommendations: " + ranked.size() + " event(s)", Toast.LENGTH_SHORT).show();
    }

    /**
     * Unique categories from events the user has already RSVPed to (order preserved for stable ranking).
     */
    private static List<String> categoriesFromPastRsvps(List<Event> pastRsvped) {
        LinkedHashSet<String> seen = new LinkedHashSet<>();
        if (pastRsvped != null) {
            for (Event e : pastRsvped) {
                if (e.getCategory() != null && !e.getCategory().trim().isEmpty()) {
                    seen.add(e.getCategory().trim());
                }
            }
        }
        return new ArrayList<>(seen);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (skipNextResumeReload) {
            skipNextResumeReload = false;
            return;
        }
        long now = SystemClock.elapsedRealtime();
        if (now - lastEventsLoadElapsedMs < MIN_RESUME_RELOAD_INTERVAL_MS) {
            return;
        }
        loadEvents();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (searchDebounceRunnable != null) {
            searchHandler.removeCallbacks(searchDebounceRunnable);
        }
    }
}