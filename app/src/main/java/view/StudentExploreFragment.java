package com.example.campuseventstest.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
 * Explore tab — browse/search/filter events (same behavior as legacy Event List screen).
 */
public class StudentExploreFragment extends Fragment {

    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private EditText searchBar;
    private ChipGroup categoryChips;
    private ProgressBar loadingBar;
    private TextView emptyStateText;
    private View recommendationsButton;
    private View myCalendarButton;
    private View notificationsInboxButton;
    private View helpAssistantButton;
    private View campusSocietiesButton;

    private FirestoreService firestoreService;
    private RecommendationService recommendationService;
    private List<Event> allEvents;
    private String currentCategory = "";
    private String currentSearchQuery = "";
    private boolean skipNextResumeReload = true;
    private long lastEventsLoadElapsedMs = 0L;
    private static final long MIN_RESUME_RELOAD_INTERVAL_MS = 2500L;
    private static final long SEARCH_DEBOUNCE_MS = 200L;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchDebounceRunnable;
    private boolean eventsLoadInFlight;
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_student_explore, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestoreService = new FirestoreService();
        recommendationService = new RecommendationService();
        allEvents = new ArrayList<>();

        recyclerView = view.findViewById(R.id.events_recycler_view);
        searchBar = view.findViewById(R.id.search_bar);
        categoryChips = view.findViewById(R.id.category_chips);
        loadingBar = view.findViewById(R.id.loading_bar);
        emptyStateText = view.findViewById(R.id.empty_state_text);
        recommendationsButton = view.findViewById(R.id.recommendations_button);
        myCalendarButton = view.findViewById(R.id.my_calendar_button);
        notificationsInboxButton = view.findViewById(R.id.notifications_inbox_button);
        helpAssistantButton = view.findViewById(R.id.help_assistant_button);
        campusSocietiesButton = view.findViewById(R.id.campus_societies_button);

        setupRecyclerView();
        setupSearchBar();
        setupCategoryFilters();
        bindShortcuts();

        loadEvents();
    }

    private void bindShortcuts() {
        recommendationsButton.setOnClickListener(v -> showRecommendations());
        myCalendarButton.setOnClickListener(v ->
                startActivity(new android.content.Intent(requireContext(), MyCalendarActivity.class)));
        notificationsInboxButton.setOnClickListener(v ->
                startActivity(new android.content.Intent(requireContext(), NotificationsActivity.class)));
        helpAssistantButton.setOnClickListener(v ->
                startActivity(new android.content.Intent(requireContext(), HelpAssistantActivity.class)));
        campusSocietiesButton.setOnClickListener(v ->
                startActivity(new android.content.Intent(requireContext(), SocietiesListActivity.class)));
    }

    private void setupRecyclerView() {
        eventAdapter = new EventAdapter(requireContext(), allEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(eventAdapter);
    }

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
                searchDebounceRunnable = StudentExploreFragment.this::applyFilters;
                searchHandler.postDelayed(searchDebounceRunnable, SEARCH_DEBOUNCE_MS);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupCategoryFilters() {
        categoryChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentCategory = "";
            } else {
                int checkedId = checkedIds.get(0);
                Chip selectedChip = rootView.findViewById(checkedId);
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
                Toast.makeText(requireContext(),
                        "Failed to load events: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<Event> buildFilteredEventList() {
        List<Event> filteredEvents = new ArrayList<>(allEvents);

        if (!currentCategory.isEmpty()) {
            List<Event> categoryFiltered = new ArrayList<>();
            for (Event event : filteredEvents) {
                if (event.getCategory() != null
                        && event.getCategory().equalsIgnoreCase(currentCategory)) {
                    categoryFiltered.add(event);
                }
            }
            filteredEvents = categoryFiltered;
        }

        if (!currentSearchQuery.isEmpty()) {
            List<Event> searchFiltered = new ArrayList<>();
            String lowerQuery = currentSearchQuery.toLowerCase();
            for (Event event : filteredEvents) {
                boolean matchesTitle = event.getTitle() != null
                        && event.getTitle().toLowerCase().contains(lowerQuery);
                boolean matchesDesc = event.getDescription() != null
                        && event.getDescription().toLowerCase().contains(lowerQuery);
                boolean matchesVenue = event.getVenue() != null
                        && event.getVenue().toLowerCase().contains(lowerQuery);

                if (matchesTitle || matchesDesc || matchesVenue) {
                    searchFiltered.add(event);
                }
            }
            filteredEvents = searchFiltered;
        }

        return filteredEvents;
    }

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
            Toast.makeText(requireContext(),
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
        Toast.makeText(requireContext(),
                "Trending picks: " + ranked.size() + " event(s)",
                Toast.LENGTH_SHORT).show();
    }

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
    public void onResume() {
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
    public void onDestroyView() {
        super.onDestroyView();
        if (searchDebounceRunnable != null) {
            searchHandler.removeCallbacks(searchDebounceRunnable);
        }
        rootView = null;
    }
}
