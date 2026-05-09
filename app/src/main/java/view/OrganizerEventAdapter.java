package com.example.campuseventstest.view;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuseventstest.R;
import com.example.campuseventstest.model.Event;
import com.example.campuseventstest.service.FirestoreService;
import com.example.campuseventstest.service.NotificationService;
import com.example.campuseventstest.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for displaying organizer's events in the dashboard.
 * Shows event details with RSVP counts, status, and an edit button.
 */
public class OrganizerEventAdapter extends RecyclerView.Adapter<OrganizerEventAdapter.OrganizerEventViewHolder> {

    private final Context context;
    private List<Event> events;
    private final SimpleDateFormat dateFormat;
    private final FirestoreService firestoreService;
    private final NotificationService notificationService;

    /**
     * Constructs an OrganizerEventAdapter.
     *
     * @param context the context from the calling activity
     * @param events  the list of organizer's events to display
     */
    public OrganizerEventAdapter(Context context, List<Event> events) {
        this.context = context;
        this.events = events;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
        this.firestoreService = new FirestoreService();
        this.notificationService = new NotificationService();
    }

    @NonNull
    @Override
    public OrganizerEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_organizer_event, parent, false);
        return new OrganizerEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrganizerEventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    /**
     * Updates the adapter with a new list of events and refreshes the view.
     *
     * @param newEvents the updated event list
     */
    public void updateEvents(List<Event> newEvents) {
        this.events = newEvents;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder for organizer event items.
     */
    class OrganizerEventViewHolder extends RecyclerView.ViewHolder {

        private final TextView titleText;
        private final TextView dateText;
        private final TextView statusText;
        private final TextView rsvpCountText;
        private final ProgressBar capacityBar;
        private final Button editButton;
        private final Button cancelButton;
        private final Button broadcastButton;

        /**
         * Constructs an OrganizerEventViewHolder and binds sub-views.
         *
         * @param itemView the view for this item
         */
        public OrganizerEventViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.organizer_event_title);
            dateText = itemView.findViewById(R.id.organizer_event_date);
            statusText = itemView.findViewById(R.id.organizer_event_status);
            rsvpCountText = itemView.findViewById(R.id.organizer_rsvp_count);
            capacityBar = itemView.findViewById(R.id.organizer_capacity_bar);
            editButton = itemView.findViewById(R.id.edit_event_button);
            cancelButton = itemView.findViewById(R.id.cancel_event_button);
            broadcastButton = itemView.findViewById(R.id.broadcast_event_button);
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

            String status = event.getStatus() != null ? event.getStatus().toUpperCase() : "LIVE";
            statusText.setText("Status: " + status);

            int fillPercentage = event.getFillPercentage();
            rsvpCountText.setText("RSVPs: " + event.getRsvpCount() + "/" + event.getCapacity() +
                    " (" + fillPercentage + "% full)");
            capacityBar.setProgress(fillPercentage);

            editButton.setOnClickListener(v -> {
                Intent intent = new Intent(context, EditEventActivity.class);
                intent.putExtra(Constants.EXTRA_EVENT_ID, event.getEventId());
                context.startActivity(intent);
            });

            broadcastButton.setOnClickListener(v -> {
                Intent intent = new Intent(context, BroadcastMessageActivity.class);
                intent.putExtra(Constants.EXTRA_EVENT_ID, event.getEventId());
                intent.putExtra(Constants.EXTRA_EVENT_TITLE, event.getTitle());
                context.startActivity(intent);
            });

            cancelButton.setOnClickListener(v -> firestoreService.updateEventStatus(
                    event.getEventId(),
                    Constants.STATUS_CANCELLED,
                    new FirestoreService.SimpleCallback() {
                        @Override
                        public void onSuccess() {
                            firestoreService.getAttendeeIdsForEvent(event.getEventId(),
                                    new FirestoreService.UserIdListCallback() {
                                        @Override
                                        public void onSuccess(List<String> userIds) {
                                            notificationService.notifyUsers(
                                                    userIds,
                                                    event.getEventId(),
                                                    "Event Cancelled",
                                                    event.getTitle() + " has been cancelled.",
                                                    "event_cancelled",
                                                    new NotificationService.NotificationDispatchCallback() {
                                                        @Override
                                                        public void onSuccess(int deliveredCount) {
                                                            Toast.makeText(context,
                                                                    "Event cancelled. Notifications sent: "
                                                                            + deliveredCount,
                                                                    Toast.LENGTH_SHORT).show();
                                                        }

                                                        @Override
                                                        public void onFailure(String error) {
                                                            Toast.makeText(context,
                                                                    "Event cancelled. Notification error: "
                                                                            + error,
                                                                    Toast.LENGTH_LONG).show();
                                                        }
                                                    });
                                        }

                                        @Override
                                        public void onFailure(String error) {
                                            Toast.makeText(context,
                                                    "Event cancelled. Could not load attendees: " + error,
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });
                            event.setStatus(Constants.STATUS_CANCELLED);
                            notifyItemChanged(getAdapterPosition());
                        }

                        @Override
                        public void onFailure(String error) {
                            Toast.makeText(context,
                                    "Failed to cancel event: " + error,
                                    Toast.LENGTH_LONG).show();
                        }
                    }));

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, EventDetailActivity.class);
                intent.putExtra(Constants.EXTRA_EVENT_ID, event.getEventId());
                context.startActivity(intent);
            });
        }
    }
}