package com.example.campuseventstest.view;

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
import com.example.campuseventstest.service.FirestoreService;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Organizer "My Events" tab — summary counts and recent event history list.
 */
public class OrganizerMyEventsFragment extends Fragment {

    private TextView managedCountView;
    private TextView monthCountView;
    private RecyclerView recyclerView;
    private ProgressBar loadingBar;
    private OrganizerEventAdapter adapter;
    private FirestoreService firestoreService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_organizer_my_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);

        firestoreService = new FirestoreService();
        managedCountView = root.findViewById(R.id.stat_managed_count);
        monthCountView = root.findViewById(R.id.stat_month_count);
        recyclerView = root.findViewById(R.id.my_events_recycler);
        loadingBar = root.findViewById(R.id.my_events_loading);

        adapter = new OrganizerEventAdapter(requireContext(), new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        loadEvents();
    }

    private void loadEvents() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return;
        }
        loadingBar.setVisibility(View.VISIBLE);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firestoreService.getEventsByOrganizer(uid, new FirestoreService.EventListCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                loadingBar.setVisibility(View.GONE);
                managedCountView.setText(String.valueOf(events.size()));
                monthCountView.setText(String.valueOf(countThisMonth(events)));
                adapter.updateEvents(events);
                if (events.isEmpty()) {
                    Toast.makeText(requireContext(), "No events yet.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                loadingBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int countThisMonth(List<Event> events) {
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        int n = 0;
        for (Event e : events) {
            Timestamp ts = e.getDate();
            if (ts == null) {
                continue;
            }
            cal.setTime(ts.toDate());
            if (cal.get(Calendar.MONTH) == month && cal.get(Calendar.YEAR) == year) {
                n++;
            }
        }
        return n;
    }
}
