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
import androidx.appcompat.app.AppCompatActivity;
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
 * Detailed list of events the student has RSVP'd to (attendance / history).
 */
public class StudentEventHistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_event_history);

        ProgressBar loading = findViewById(R.id.history_loading);
        TextView empty = findViewById(R.id.history_empty);
        RecyclerView recyclerView = findViewById(R.id.history_recycler);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            finish();
            return;
        }

        Adapter adapter = new Adapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loading.setVisibility(View.VISIBLE);
        new FirestoreService().getRsvpedEventsByStudent(
                FirebaseAuth.getInstance().getCurrentUser().getUid(),
                new FirestoreService.EventListCallback() {
                    @Override
                    public void onSuccess(List<Event> events) {
                        loading.setVisibility(View.GONE);
                        List<Event> list = events != null ? events : new ArrayList<>();
                        adapter.setData(list);
                        empty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onFailure(String error) {
                        loading.setVisibility(View.GONE);
                        Toast.makeText(StudentEventHistoryActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private class Adapter extends RecyclerView.Adapter<Adapter.VH> {

        private final List<Event> items = new ArrayList<>();
        private final SimpleDateFormat df =
                new SimpleDateFormat("EEE, MMM d yyyy · HH:mm", Locale.getDefault());

        void setData(List<Event> e) {
            items.clear();
            items.addAll(e);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_event_history_row, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            Event e = items.get(position);
            holder.title.setText(e.getTitle());
            String line = "";
            if (e.getDate() != null) {
                line = df.format(e.getDate().toDate());
            }
            if (e.getVenue() != null && !e.getVenue().isEmpty()) {
                line = line.isEmpty() ? e.getVenue() : line + " · " + e.getVenue();
            }
            holder.whenWhere.setText(line);
            holder.category.setText(e.getCategory() != null ? e.getCategory() : "");
            holder.capacity.setText(getString(R.string.history_capacity_fmt,
                    e.getRsvpCount(), e.getCapacity(), e.getFillPercentage()));

            holder.itemView.setOnClickListener(v -> {
                Intent i = new Intent(StudentEventHistoryActivity.this, EventDetailActivity.class);
                i.putExtra(Constants.EXTRA_EVENT_ID, e.getEventId());
                startActivity(i);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class VH extends RecyclerView.ViewHolder {
            final TextView title;
            final TextView whenWhere;
            final TextView category;
            final TextView capacity;

            VH(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.history_title);
                whenWhere = itemView.findViewById(R.id.history_when_where);
                category = itemView.findViewById(R.id.history_category);
                capacity = itemView.findViewById(R.id.history_capacity);
            }
        }
    }
}
