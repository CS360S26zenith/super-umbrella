package com.example.campuseventstest.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.campuseventstest.model.User;
import com.example.campuseventstest.service.AuthService;
import com.example.campuseventstest.service.FirestoreService;
import com.example.campuseventstest.service.RecommendationService;
import com.example.campuseventstest.utils.Constants;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Student Home tab — landing UI, notifications, upcoming feed, shortcuts.
 */
public class StudentHomeFragment extends Fragment {

    private TextView greetingView;
    private TextView statWeekView;
    private TextView statRegisteredView;
    private TextView statSavedView;
    private ProgressBar loadingBar;
    private TextView emptyHomeView;
    private RecyclerView recyclerView;
    private HomeUpcomingAdapter adapter;

    private FirestoreService firestoreService;
    private RecommendationService recommendationService;
    private AuthService authService;

    private boolean loadedUserStats;
    private boolean loadedRsvpStats;
    private int followedSocietyCount;
    private List<Event> myRsvpEvents = new ArrayList<>();

    private List<Event> allLiveEvents = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_student_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestoreService = new FirestoreService();
        recommendationService = new RecommendationService();
        authService = new AuthService();

        greetingView = view.findViewById(R.id.home_greeting);
        statWeekView = view.findViewById(R.id.stat_week_val);
        statRegisteredView = view.findViewById(R.id.stat_registered_val);
        statSavedView = view.findViewById(R.id.stat_saved_val);
        loadingBar = view.findViewById(R.id.home_loading_bar);
        emptyHomeView = view.findViewById(R.id.empty_home_events);
        recyclerView = view.findViewById(R.id.upcoming_recycler);

        adapter = new HomeUpcomingAdapter(requireContext(), new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.home_notif_button).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), NotificationsActivity.class)));

        view.findViewById(R.id.home_sign_out_button).setOnClickListener(v -> {
            authService.logout();
            Intent i = new Intent(requireContext(), LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            requireActivity().finish();
        });

        view.findViewById(R.id.card_my_payments).setOnClickListener(v ->
                Toast.makeText(requireContext(), R.string.my_payments_coming_soon, Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.quick_trending_card).setOnClickListener(v -> applyTrendingToHome());
        view.findViewById(R.id.quick_my_rsvps_card).setOnClickListener(v -> openTicketsTab());
        view.findViewById(R.id.btn_explore_all).setOnClickListener(v -> openExploreTab());

        setGreetingFromTime();
        loadFollowedCountAndRsvps();
        loadLiveEventsForHome();
    }

    private void setGreetingFromTime() {
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        String first = u != null && u.getDisplayName() != null && !u.getDisplayName().trim().isEmpty()
                ? u.getDisplayName().trim().split("\\s+")[0]
                : "";

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String salute;
        if (hour < 12) {
            salute = getString(R.string.greeting_morning);
        } else if (hour < 17) {
            salute = getString(R.string.greeting_afternoon);
        } else {
            salute = getString(R.string.greeting_evening);
        }

        if (first.isEmpty()) {
            greetingView.setText(getString(R.string.greeting_plain, salute));
        } else {
            greetingView.setText(getString(R.string.greeting_named, salute, first));
        }
    }

    private void loadFollowedCountAndRsvps() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }
        loadedUserStats = false;
        loadedRsvpStats = false;

        FirebaseFirestore.getInstance()
                .collection(Constants.COLLECTION_USERS)
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    User usr = doc.toObject(User.class);
                    followedSocietyCount = usr != null ? usr.getFollowedSocietyIds().size() : 0;
                    loadedUserStats = true;
                    refreshStatTiles();
                })
                .addOnFailureListener(e -> {
                    followedSocietyCount = 0;
                    loadedUserStats = true;
                    refreshStatTiles();
                });

        firestoreService.getRsvpedEventsByStudent(user.getUid(), new FirestoreService.EventListCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                myRsvpEvents = events != null ? events : new ArrayList<>();
                loadedRsvpStats = true;
                refreshStatTiles();
            }

            @Override
            public void onFailure(String error) {
                myRsvpEvents = new ArrayList<>();
                loadedRsvpStats = true;
                refreshStatTiles();
            }
        });
    }

    private void refreshStatTiles() {
        if (!loadedUserStats || !loadedRsvpStats) {
            return;
        }
        statRegisteredView.setText(String.valueOf(myRsvpEvents.size()));
        statSavedView.setText(String.valueOf(followedSocietyCount));
        statWeekView.setText(String.valueOf(countRsvpsInNextSevenDays(myRsvpEvents)));
    }

    private static int countRsvpsInNextSevenDays(List<Event> rsvped) {
        long now = System.currentTimeMillis();
        long horizon = now + 7L * 24 * 60 * 60 * 1000;
        int n = 0;
        for (Event e : rsvped) {
            Timestamp ts = e.getDate();
            if (ts == null) {
                continue;
            }
            long t = ts.toDate().getTime();
            if (t >= now && t <= horizon) {
                n++;
            }
        }
        return n;
    }

    private void loadLiveEventsForHome() {
        loadingBar.setVisibility(View.VISIBLE);
        emptyHomeView.setVisibility(View.GONE);
        firestoreService.getLiveEvents(new FirestoreService.EventListCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                loadingBar.setVisibility(View.GONE);
                allLiveEvents = events != null ? events : new ArrayList<>();
                List<HomeUpcomingAdapter.FeedRow> feed = buildHomeFeed(allLiveEvents);
                adapter.setFeedRows(feed);
                emptyHomeView.setVisibility(feed.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onFailure(String error) {
                loadingBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<HomeUpcomingAdapter.FeedRow> buildHomeFeed(List<Event> live) {
        long now = System.currentTimeMillis();
        List<Event> upcoming = new ArrayList<>();
        List<Event> past = new ArrayList<>();
        for (Event e : live) {
            if (e.getDate() == null) {
                continue;
            }
            long t = e.getDate().toDate().getTime();
            if (t < now) {
                past.add(e);
            } else {
                upcoming.add(e);
            }
        }
        Comparator<Event> byDateDesc = (a, b) -> Long.compare(
                b.getDate().toDate().getTime(), a.getDate().toDate().getTime());
        Collections.sort(upcoming, byDateDesc);
        Collections.sort(past, byDateDesc);

        List<HomeUpcomingAdapter.FeedRow> rows = new ArrayList<>();
        if (!upcoming.isEmpty()) {
            rows.add(HomeUpcomingAdapter.FeedRow.header(getString(R.string.home_section_upcoming)));
            for (Event e : upcoming) {
                rows.add(HomeUpcomingAdapter.FeedRow.event(e, false));
            }
        }
        if (!past.isEmpty()) {
            rows.add(HomeUpcomingAdapter.FeedRow.header(getString(R.string.home_section_past)));
            for (Event e : past) {
                rows.add(HomeUpcomingAdapter.FeedRow.event(e, true));
            }
        }
        return rows;
    }

    private List<Event> upcomingEventsSortedForTrending(List<Event> events) {
        long now = System.currentTimeMillis();
        List<Event> upcoming = new ArrayList<>();
        for (Event e : events) {
            if (e.getDate() == null) {
                continue;
            }
            if (e.getDate().toDate().getTime() >= now) {
                upcoming.add(e);
            }
        }
        Collections.sort(upcoming, (a, b) -> Long.compare(
                b.getDate().toDate().getTime(), a.getDate().toDate().getTime()));
        return upcoming;
    }

    private List<Event> pastEventsSortedDesc(List<Event> events) {
        long now = System.currentTimeMillis();
        List<Event> past = new ArrayList<>();
        for (Event e : events) {
            if (e.getDate() == null) {
                continue;
            }
            if (e.getDate().toDate().getTime() < now) {
                past.add(e);
            }
        }
        Collections.sort(past, (a, b) -> Long.compare(
                b.getDate().toDate().getTime(), a.getDate().toDate().getTime()));
        return past;
    }

    private List<HomeUpcomingAdapter.FeedRow> buildHomeFeedFromUpcomingAndPast(
            List<Event> upcomingRanked, List<Event> pastSorted) {
        List<HomeUpcomingAdapter.FeedRow> rows = new ArrayList<>();
        if (!upcomingRanked.isEmpty()) {
            rows.add(HomeUpcomingAdapter.FeedRow.header(getString(R.string.home_section_upcoming)));
            for (Event e : upcomingRanked) {
                rows.add(HomeUpcomingAdapter.FeedRow.event(e, false));
            }
        }
        if (!pastSorted.isEmpty()) {
            rows.add(HomeUpcomingAdapter.FeedRow.header(getString(R.string.home_section_past)));
            for (Event e : pastSorted) {
                rows.add(HomeUpcomingAdapter.FeedRow.event(e, true));
            }
        }
        return rows;
    }

    private void applyTrendingToHome() {
        List<Event> pool = upcomingEventsSortedForTrending(allLiveEvents);
        if (pool.isEmpty()) {
            Toast.makeText(requireContext(), R.string.home_no_events_trending, Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            rankAndShow(pool, new ArrayList<>());
            return;
        }
        firestoreService.getRsvpedEventsByStudent(user.getUid(), new FirestoreService.EventListCallback() {
            @Override
            public void onSuccess(List<Event> pastRsvped) {
                List<String> prefs = categoriesFromRsvps(pastRsvped);
                rankAndShow(pool, prefs);
            }

            @Override
            public void onFailure(String error) {
                rankAndShow(pool, new ArrayList<>());
            }
        });
    }

    private void rankAndShow(List<Event> pool, List<String> preferredCategories) {
        List<Event> ranked = recommendationService.rankEvents(pool, preferredCategories);
        List<Event> pastSorted = pastEventsSortedDesc(allLiveEvents);
        List<HomeUpcomingAdapter.FeedRow> feed = buildHomeFeedFromUpcomingAndPast(ranked, pastSorted);
        adapter.setFeedRows(feed);
        emptyHomeView.setVisibility(feed.isEmpty() ? View.VISIBLE : View.GONE);
        Toast.makeText(requireContext(),
                getString(R.string.home_trending_applied, ranked.size()),
                Toast.LENGTH_SHORT).show();
    }

    private static List<String> categoriesFromRsvps(List<Event> past) {
        List<String> out = new ArrayList<>();
        if (past == null) {
            return out;
        }
        for (Event e : past) {
            if (e.getCategory() != null && !e.getCategory().trim().isEmpty()) {
                out.add(e.getCategory().trim());
            }
        }
        return out;
    }

    private void openExploreTab() {
        if (getActivity() instanceof StudentMainActivity) {
            ((StudentMainActivity) requireActivity()).selectNav(R.id.nav_student_explore);
        }
    }

    private void openTicketsTab() {
        if (getActivity() instanceof StudentMainActivity) {
            ((StudentMainActivity) requireActivity()).selectNav(R.id.nav_student_tickets);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setGreetingFromTime();
        loadFollowedCountAndRsvps();
        loadLiveEventsForHome();
    }
}
