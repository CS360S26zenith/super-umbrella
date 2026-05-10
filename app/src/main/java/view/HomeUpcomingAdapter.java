package com.example.campuseventstest.view;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuseventstest.R;
import com.example.campuseventstest.model.Event;
import com.example.campuseventstest.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Home feed cards — date tile + title + venue + category pill.
 */
public class HomeUpcomingAdapter extends RecyclerView.Adapter<HomeUpcomingAdapter.Holder> {

    private final Context context;
    private List<Event> events;

    public HomeUpcomingAdapter(Context context, List<Event> events) {
        this.context = context;
        this.events = events != null ? events : new ArrayList<>();
    }

    public void setEvents(List<Event> events) {
        this.events = events != null ? events : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_home_upcoming_event, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Event e = events.get(position);
        holder.title.setText(e.getTitle());
        holder.venue.setText(e.getVenue() != null ? e.getVenue() : "");
        holder.tag.setText(e.getCategory() != null ? e.getCategory() : "");

        if (e.getDate() != null) {
            SimpleDateFormat day = new SimpleDateFormat("d", Locale.getDefault());
            SimpleDateFormat mon = new SimpleDateFormat("MMM", Locale.getDefault());
            holder.day.setText(day.format(e.getDate().toDate()));
            holder.month.setText(mon.format(e.getDate().toDate()));
        } else {
            holder.day.setText("—");
            holder.month.setText("");
        }

        holder.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, EventDetailActivity.class);
            i.putExtra(Constants.EXTRA_EVENT_ID, e.getEventId());
            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView day;
        final TextView month;
        final TextView title;
        final TextView venue;
        final TextView tag;

        Holder(@NonNull View itemView) {
            super(itemView);
            day = itemView.findViewById(R.id.home_event_day);
            month = itemView.findViewById(R.id.home_event_month);
            title = itemView.findViewById(R.id.home_event_title);
            venue = itemView.findViewById(R.id.home_event_venue);
            tag = itemView.findViewById(R.id.home_event_tag);
        }
    }
}
