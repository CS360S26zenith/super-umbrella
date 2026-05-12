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
 * Student home feed: optional section headers plus event cards.
 */
public class HomeUpcomingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_EVENT = 1;

    public static final class FeedRow {
        public final int type;
        public final String sectionTitle;
        public final Event event;
        public final boolean isPast;

        private FeedRow(int type, String sectionTitle, Event event, boolean isPast) {
            this.type = type;
            this.sectionTitle = sectionTitle;
            this.event = event;
            this.isPast = isPast;
        }

        public static FeedRow header(String title) {
            return new FeedRow(TYPE_HEADER, title, null, false);
        }

        public static FeedRow event(Event e, boolean past) {
            return new FeedRow(TYPE_EVENT, null, e, past);
        }
    }

    private final Context context;
    private final List<FeedRow> rows = new ArrayList<>();

    public HomeUpcomingAdapter(Context context, List<FeedRow> initialRows) {
        this.context = context;
        if (initialRows != null) {
            this.rows.addAll(initialRows);
        }
    }

    public void setFeedRows(List<FeedRow> newRows) {
        rows.clear();
        if (newRows != null) {
            rows.addAll(newRows);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(context).inflate(R.layout.item_home_section_header, parent, false);
            return new HeaderHolder(v);
        }
        View v = LayoutInflater.from(context).inflate(R.layout.item_home_upcoming_event, parent, false);
        return new EventHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        FeedRow row = rows.get(position);
        if (holder instanceof HeaderHolder) {
            ((HeaderHolder) holder).title.setText(row.sectionTitle);
        } else if (holder instanceof EventHolder) {
            ((EventHolder) holder).bind(row.event, row.isPast);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position).type;
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class HeaderHolder extends RecyclerView.ViewHolder {
        final TextView title;

        HeaderHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.home_section_title);
        }
    }

    class EventHolder extends RecyclerView.ViewHolder {
        final TextView pastLabel;
        final TextView day;
        final TextView month;
        final TextView title;
        final TextView venue;
        final TextView tag;

        EventHolder(@NonNull View itemView) {
            super(itemView);
            pastLabel = itemView.findViewById(R.id.home_event_past_label);
            day = itemView.findViewById(R.id.home_event_day);
            month = itemView.findViewById(R.id.home_event_month);
            title = itemView.findViewById(R.id.home_event_title);
            venue = itemView.findViewById(R.id.home_event_venue);
            tag = itemView.findViewById(R.id.home_event_tag);
        }

        void bind(Event e, boolean isPast) {
            pastLabel.setVisibility(isPast ? View.VISIBLE : View.GONE);
            itemView.setAlpha(isPast ? 0.88f : 1f);

            title.setText(e.getTitle());
            venue.setText(e.getVenue() != null ? e.getVenue() : "");
            tag.setText(e.getCategory() != null ? e.getCategory() : "");

            if (e.getDate() != null) {
                SimpleDateFormat dayFmt = new SimpleDateFormat("d", Locale.getDefault());
                SimpleDateFormat monFmt = new SimpleDateFormat("MMM", Locale.getDefault());
                day.setText(dayFmt.format(e.getDate().toDate()));
                month.setText(monFmt.format(e.getDate().toDate()));
            } else {
                day.setText("—");
                month.setText("");
            }

            itemView.setOnClickListener(v -> {
                Intent i = new Intent(context, EventDetailActivity.class);
                i.putExtra(Constants.EXTRA_EVENT_ID, e.getEventId());
                context.startActivity(i);
            });
        }
    }
}
