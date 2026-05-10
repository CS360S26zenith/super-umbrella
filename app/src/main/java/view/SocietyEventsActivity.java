package com.example.campuseventstest.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuseventstest.R;
import com.example.campuseventstest.model.Event;
import com.example.campuseventstest.service.FirestoreService;
import com.example.campuseventstest.utils.Constants;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Past and upcoming events for a single society (student view).
 */
public class SocietyEventsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar loadingBar;
    private TextView emptyText;
    private Button tabUpcoming;
    private Button tabPast;
    private EventAdapter eventAdapter;
    private FirestoreService firestoreService;

    private List<Event> upcoming = new ArrayList<>();
    private List<Event> past = new ArrayList<>();
    private boolean showUpcoming = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_society_events);

        String societyId = getIntent().getStringExtra(Constants.EXTRA_SOCIETY_ID);
        String societyName = getIntent().getStringExtra(Constants.EXTRA_SOCIETY_NAME);
        if (societyId == null || societyId.isEmpty()) {
            Toast.makeText(this, "Missing society", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        firestoreService = new FirestoreService();

        Toolbar toolbar = findViewById(R.id.society_events_toolbar);
        toolbar.setTitle(societyName != null ? societyName : "Society events");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.society_events_recycler);
        loadingBar = findViewById(R.id.society_events_loading);
        emptyText = findViewById(R.id.society_events_empty);
        tabUpcoming = findViewById(R.id.tab_upcoming);
        tabPast = findViewById(R.id.tab_past);

        eventAdapter = new EventAdapter(this, new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(eventAdapter);

        tabUpcoming.setOnClickListener(v -> switchSection(true));
        tabPast.setOnClickListener(v -> switchSection(false));

        loadingBar.setVisibility(View.VISIBLE);
        firestoreService.getEventsBySociety(societyId, new FirestoreService.EventListCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                loadingBar.setVisibility(View.GONE);
                partitionEvents(events);
                switchSection(true);
            }

            @Override
            public void onFailure(String error) {
                loadingBar.setVisibility(View.GONE);
                Toast.makeText(SocietyEventsActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isStudentVisible(Event e) {
        String st = e.getStatus();
        return st != null
                && !Constants.STATUS_DRAFT.equalsIgnoreCase(st.trim());
    }

    private void partitionEvents(List<Event> raw) {
        upcoming.clear();
        past.clear();
        Date now = new Date();
        for (Event e : raw) {
            if (!isStudentVisible(e)) {
                continue;
            }
            Timestamp ts = e.getDate();
            Date d = ts != null ? ts.toDate() : null;
            if (d == null) {
                continue;
            }
            if (d.getTime() >= now.getTime()) {
                upcoming.add(e);
            } else {
                past.add(e);
            }
        }
        Collections.sort(upcoming, (a, b) -> Long.compare(eventMillis(a), eventMillis(b)));
        Collections.sort(past, (a, b) -> Long.compare(eventMillis(b), eventMillis(a)));
    }

    private long eventMillis(Event e) {
        return e.getDate() != null ? e.getDate().toDate().getTime() : 0L;
    }

    private void switchSection(boolean upcomingSection) {
        showUpcoming = upcomingSection;
        highlightTabs();
        List<Event> show = showUpcoming ? upcoming : past;
        eventAdapter.updateEvents(show);
        emptyText.setVisibility(show.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void highlightTabs() {
        int selected = 0xFF1976D2;
        int muted = 0xFFE3F2FD;
        tabUpcoming.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                showUpcoming ? selected : muted));
        tabPast.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                showUpcoming ? muted : selected));
        tabUpcoming.setTextColor(showUpcoming ? 0xFFFFFFFF : 0xFF1565C0);
        tabPast.setTextColor(showUpcoming ? 0xFF1565C0 : 0xFFFFFFFF);
    }
}
