package com.example.campuseventstest.view;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuseventstest.R;
import com.example.campuseventstest.model.Event;
import com.example.campuseventstest.service.FirestoreService;
import com.example.campuseventstest.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

/**
 * Displays the current student's RSVPed events as a personal schedule.
 */
public class MyCalendarActivity extends AppCompatActivity {
    private android.widget.TextView emptyText;
    private CalendarEventAdapter adapter;
    private FirestoreService firestoreService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_calendar);
        emptyText = findViewById(R.id.calendar_empty_text);
        RecyclerView recyclerView = findViewById(R.id.calendar_recycler);
        adapter = new CalendarEventAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        firestoreService = new FirestoreService();

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            finish();
            return;
        }
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final boolean thisMonthUpcomingOnly = getIntent().getBooleanExtra(
                Constants.EXTRA_CALENDAR_THIS_MONTH_UPCOMING, false);
        firestoreService.getRsvpedEventsByStudent(userId, new FirestoreService.EventListCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                if (events == null || events.isEmpty()) {
                    emptyText.setText(R.string.calendar_empty_no_rsvps);
                    adapter.submit(new ArrayList<>());
                    return;
                }
                List<Event> list = new ArrayList<>(events);
                if (thisMonthUpcomingOnly) {
                    list = filterUpcomingInCurrentCalendarMonth(list);
                    if (list.isEmpty()) {
                        emptyText.setText(R.string.calendar_empty_this_month_upcoming);
                        adapter.submit(new ArrayList<>());
                        return;
                    }
                    emptyText.setText(R.string.calendar_header_this_month_upcoming);
                } else {
                    list.sort(Comparator.comparing(Event::getDateAsDate, Comparator.nullsLast(Comparator.naturalOrder())));
                    emptyText.setText(R.string.calendar_header_default);
                }
                adapter.submit(list);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(MyCalendarActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static List<Event> filterUpcomingInCurrentCalendarMonth(List<Event> events) {
        long now = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        List<Event> out = new ArrayList<>();
        for (Event e : events) {
            if (e.getDate() == null) {
                continue;
            }
            long t = e.getDate().toDate().getTime();
            if (t < now) {
                continue;
            }
            cal.setTimeInMillis(t);
            if (cal.get(Calendar.MONTH) == month && cal.get(Calendar.YEAR) == year) {
                out.add(e);
            }
        }
        out.sort(Comparator.comparing(Event::getDateAsDate, Comparator.nullsLast(Comparator.naturalOrder())));
        return out;
    }
}
