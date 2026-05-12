package com.example.campuseventstest.view;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuseventstest.R;
import com.example.campuseventstest.model.Event;
import com.example.campuseventstest.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Explore list rows with availability badge.
 */
public class ExploreEventAdapter extends RecyclerView.Adapter<ExploreEventAdapter.Holder> {

    private final Context context;
    private List<Event> events;

    public ExploreEventAdapter(Context context, List<Event> events) {
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
        View v = LayoutInflater.from(context).inflate(R.layout.item_explore_event, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Event e = events.get(position);
        holder.title.setText(e.getTitle());
        String venueLine = e.getVenue() != null ? "📍 " + e.getVenue() : "";
        holder.venue.setText(venueLine);

        if (e.getDate() != null) {
            SimpleDateFormat day = new SimpleDateFormat("d", Locale.getDefault());
            SimpleDateFormat mon = new SimpleDateFormat("MMM", Locale.getDefault());
            holder.day.setText(day.format(e.getDate().toDate()));
            holder.month.setText(mon.format(e.getDate().toDate()));
        } else {
            holder.day.setText("—");
            holder.month.setText("");
        }

        int fill = e.getFillPercentage();
        if (e.isFull()) {
            holder.badge.setText(R.string.badge_full);
            holder.badge.setTextColor(ContextCompat.getColor(context, R.color.campus_badge_error_text));
            holder.badge.setBackgroundColor(ContextCompat.getColor(context, R.color.campus_badge_error_bg));
        } else if (fill >= 85) {
            holder.badge.setText(context.getString(R.string.badge_almost_full_fmt, fill));
            holder.badge.setTextColor(ContextCompat.getColor(context, R.color.campus_badge_warning_text));
            holder.badge.setBackgroundColor(ContextCompat.getColor(context, R.color.campus_badge_warning_bg));
        } else if (fill >= 60) {
            holder.badge.setText(context.getString(R.string.badge_percent_fmt, fill));
            holder.badge.setTextColor(ContextCompat.getColor(context, R.color.campus_badge_caution_text));
            holder.badge.setBackgroundColor(ContextCompat.getColor(context, R.color.campus_badge_caution_bg));
        } else {
            holder.badge.setText(R.string.badge_available);
            holder.badge.setTextColor(ContextCompat.getColor(context, R.color.campus_badge_success_text));
            holder.badge.setBackgroundColor(ContextCompat.getColor(context, R.color.campus_badge_success_bg));
        }

        String society = e.getSocietyName();
        if (society != null && !society.trim().isEmpty()) {
            holder.society.setVisibility(View.VISIBLE);
            holder.society.setText(context.getString(R.string.explore_event_host_by, society.trim()));
        } else {
            holder.society.setVisibility(View.GONE);
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
        final TextView badge;
        final TextView society;

        Holder(@NonNull View itemView) {
            super(itemView);
            day = itemView.findViewById(R.id.explore_event_day);
            month = itemView.findViewById(R.id.explore_event_month);
            title = itemView.findViewById(R.id.explore_event_title);
            venue = itemView.findViewById(R.id.explore_event_venue);
            badge = itemView.findViewById(R.id.explore_event_badge);
            society = itemView.findViewById(R.id.explore_event_society);
        }
    }
}
