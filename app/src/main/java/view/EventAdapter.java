package com.example.campuseventstest.view;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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
 * RecyclerView adapter for displaying event items in the student event list.
 * Each item shows event title, date, venue, category, and capacity fill percentage.
 * Clicking an item navigates to EventDetailActivity.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final Context context;
    private List<Event> events;
    private List<Event> eventsFiltered;
    private final SimpleDateFormat dateFormat;

    /**
     * Constructs an EventAdapter with the given context and event list.
     *
     * @param context the context from the calling activity
     * @param events  the list of events to display
     */
    public EventAdapter(Context context, List<Event> events) {
        this.context = context;
        this.events = events;
        this.eventsFiltered = new ArrayList<>(events);
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventsFiltered.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return eventsFiltered.size();
    }

    /**
     * Updates the adapter with a new list of events and refreshes the view.
     *
     * @param newEvents the updated event list
     */
    public void updateEvents(List<Event> newEvents) {
        this.events = newEvents;
        this.eventsFiltered = new ArrayList<>(newEvents);
        notifyDataSetChanged();
    }

    /**
     * Filters events by search query (matches title, description, or venue).
     *
     * @param query the search query string
     */
    public void filter(String query) {
        eventsFiltered.clear();
        if (query.isEmpty()) {
            eventsFiltered.addAll(events);
        } else {
            String lowerQuery = query.toLowerCase(Locale.getDefault());
            for (Event event : events) {
                boolean matchesTitle = event.getTitle() != null &&
                        event.getTitle().toLowerCase(Locale.getDefault()).contains(lowerQuery);
                boolean matchesDesc = event.getDescription() != null &&
                        event.getDescription().toLowerCase(Locale.getDefault()).contains(lowerQuery);
                boolean matchesVenue = event.getVenue() != null &&
                        event.getVenue().toLowerCase(Locale.getDefault()).contains(lowerQuery);

                if (matchesTitle || matchesDesc || matchesVenue) {
                    eventsFiltered.add(event);
                }
            }
        }
        notifyDataSetChanged();
    }

    /**
     * Filters events by category.
     *
     * @param category the category to filter by (null or empty shows all)
     */
    public void filterByCategory(String category) {
        eventsFiltered.clear();
        if (category == null || category.isEmpty()) {
            eventsFiltered.addAll(events);
        } else {
            for (Event event : events) {
                if (event.getCategory() != null &&
                        event.getCategory().equalsIgnoreCase(category)) {
                    eventsFiltered.add(event);
                }
            }
        }
        notifyDataSetChanged();
    }

    /**
     * ViewHolder for individual event items.
     */
    class EventViewHolder extends RecyclerView.ViewHolder {

        private final TextView titleText;
        private final TextView dateText;
        private final TextView venueText;
        private final TextView categoryText;
        private final TextView capacityText;
        private final ProgressBar capacityBar;

        /**
         * Constructs an EventViewHolder and binds sub-views.
         *
         * @param itemView the view for this item
         */
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.event_title);
            dateText = itemView.findViewById(R.id.event_date);
            venueText = itemView.findViewById(R.id.event_venue);
            categoryText = itemView.findViewById(R.id.event_category);
            capacityText = itemView.findViewById(R.id.event_capacity);
            capacityBar = itemView.findViewById(R.id.event_capacity_bar);
        }

        /**
         * Binds event data to the view elements.
         *
         * @param event the event to display
         */
        public void bind(Event event) {
            titleText.setText(event.getTitle());

            if (event.getDate() != null) {
                dateText.setText(dateFormat.format(event.getDate().toDate()));
            } else {
                dateText.setText("Date TBD");
            }

            venueText.setText(event.getVenue());
            categoryText.setText(event.getCategory());

            int fillPercentage = event.getFillPercentage();
            capacityText.setText(event.getRsvpCount() + "/" + event.getCapacity() +
                    " (" + fillPercentage + "% full)");
            capacityBar.setProgress(fillPercentage);

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, EventDetailActivity.class);
                intent.putExtra(Constants.EXTRA_EVENT_ID, event.getEventId());
                context.startActivity(intent);
            });
        }
    }
}