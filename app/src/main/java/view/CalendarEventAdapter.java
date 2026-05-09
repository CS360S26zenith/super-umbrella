package com.example.campuseventstest.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuseventstest.R;
import com.example.campuseventstest.model.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CalendarEventAdapter extends RecyclerView.Adapter<CalendarEventAdapter.CalendarEventViewHolder> {
    private final List<Event> items = new ArrayList<>();
    private final SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE, MMM dd", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

    public void submit(List<Event> events) {
        items.clear();
        if (events != null) {
            items.addAll(events);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CalendarEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_event, parent, false);
        return new CalendarEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarEventViewHolder holder, int position) {
        Event event = items.get(position);
        if (event.getDate() != null) {
            holder.date.setText(dayFormat.format(event.getDate().toDate()));
            holder.details.setText(timeFormat.format(event.getDate().toDate()) + " • " + event.getVenue());
        } else {
            holder.date.setText("Date TBD");
            holder.details.setText(event.getVenue());
        }
        holder.title.setText(event.getTitle());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CalendarEventViewHolder extends RecyclerView.ViewHolder {
        private final TextView date;
        private final TextView title;
        private final TextView details;

        CalendarEventViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.calendar_item_date);
            title = itemView.findViewById(R.id.calendar_item_title);
            details = itemView.findViewById(R.id.calendar_item_details);
        }
    }
}
