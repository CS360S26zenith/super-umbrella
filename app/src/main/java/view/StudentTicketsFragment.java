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
import com.example.campuseventstest.service.FirestoreService;
import com.example.campuseventstest.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Lists events the student has RSVP'd to with quick access to QR tickets.
 */
public class StudentTicketsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar loadingBar;
    private TextView emptyView;
    private TicketEventsAdapter adapter;
    private FirestoreService firestoreService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_student_tickets, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestoreService = new FirestoreService();
        recyclerView = view.findViewById(R.id.tickets_recycler);
        loadingBar = view.findViewById(R.id.tickets_loading);
        emptyView = view.findViewById(R.id.tickets_empty);

        adapter = new TicketEventsAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        loadTickets();
    }

    private void loadTickets() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return;
        }
        loadingBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        firestoreService.getRsvpedEventsByStudent(
                FirebaseAuth.getInstance().getCurrentUser().getUid(),
                new FirestoreService.EventListCallback() {
                    @Override
                    public void onSuccess(List<Event> events) {
                        loadingBar.setVisibility(View.GONE);
                        List<Event> list = events != null ? events : new ArrayList<>();
                        adapter.setEvents(list);
                        boolean empty = list.isEmpty();
                        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
                        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
                    }

                    @Override
                    public void onFailure(String error) {
                        loadingBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTickets();
    }

    private class TicketEventsAdapter extends RecyclerView.Adapter<TicketEventsAdapter.VH> {

        private final List<Event> items = new ArrayList<>();
        private final SimpleDateFormat df =
                new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());

        void setEvents(List<Event> events) {
            items.clear();
            items.addAll(events);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View row = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_student_ticket_row, parent, false);
            return new VH(row);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            Event e = items.get(position);
            holder.title.setText(e.getTitle());
            String meta = "";
            if (e.getDate() != null) {
                meta = df.format(e.getDate().toDate());
            }
            if (e.getVenue() != null && !e.getVenue().trim().isEmpty()) {
                meta = meta.isEmpty() ? e.getVenue() : meta + " · " + e.getVenue();
            }
            holder.meta.setText(meta);

            holder.button.setOnClickListener(v -> {
                Intent i = new Intent(requireContext(), TicketActivity.class);
                i.putExtra(Constants.EXTRA_EVENT_ID, e.getEventId());
                i.putExtra(Constants.EXTRA_EVENT_TITLE, e.getTitle());
                startActivity(i);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class VH extends RecyclerView.ViewHolder {
            final TextView title;
            final TextView meta;
            final View button;

            VH(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.ticket_row_title);
                meta = itemView.findViewById(R.id.ticket_row_meta);
                button = itemView.findViewById(R.id.ticket_row_button);
            }
        }
    }
}
