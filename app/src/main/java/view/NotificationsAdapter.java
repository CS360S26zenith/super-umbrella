package com.example.campuseventstest.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuseventstest.R;
import com.example.campuseventstest.model.AppNotification;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for {@link NotificationsActivity}.
 */
public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.Holder> {

    private final Context context;
    private List<AppNotification> items = new ArrayList<>();
    private final SimpleDateFormat dateFormat;

    public NotificationsAdapter(Context context) {
        this.context = context;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    }

    public void setItems(List<AppNotification> notifications) {
        this.items = notifications != null ? notifications : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        AppNotification n = items.get(position);
        holder.title.setText(n.getTitle() != null ? n.getTitle() : "");
        holder.message.setText(n.getMessage() != null ? n.getMessage() : "");
        if (n.getTimestamp() != null) {
            holder.time.setText(dateFormat.format(n.getTimestamp().toDate()));
        } else {
            holder.time.setText("");
        }
        holder.type.setText(humanizeNotificationType(n.getType()));
    }

    /** Turns stored keys like {@code event_reminder} into readable labels for the inbox. */
    private static String humanizeNotificationType(String type) {
        if (type == null || type.isEmpty()) {
            return "";
        }
        String[] parts = type.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                sb.append(part.substring(1).toLowerCase(Locale.ROOT));
            }
        }
        return sb.toString();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView message;
        final TextView time;
        final TextView type;

        Holder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.notification_title);
            message = itemView.findViewById(R.id.notification_message);
            time = itemView.findViewById(R.id.notification_time);
            type = itemView.findViewById(R.id.notification_type);
        }
    }
}
