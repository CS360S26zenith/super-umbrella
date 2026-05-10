package com.example.campuseventstest.view;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuseventstest.R;
import com.example.campuseventstest.model.Society;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Directory list for Campus Societies with follow toggle and navigation to a society's events.
 */
public class CampusSocietyAdapter extends RecyclerView.Adapter<CampusSocietyAdapter.Holder> {

    public interface Listener {
        void onOpenSociety(Society society);

        void onToggleFollow(Society society, boolean currentlyFollowing);
    }

    private static final int[] ACCENT_COLORS = {
            0xFFE91E63, 0xFF4CAF50, 0xFF2196F3, 0xFFF44336,
            0xFFCDDC39, 0xFFFF9800, 0xFF9C27B0, 0xFF009688
    };

    private final Context context;
    private final Listener listener;
    private List<Society> all;
    private List<Society> filtered;
    private final Set<String> followedIds;

    public CampusSocietyAdapter(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;
        this.all = new ArrayList<>();
        this.filtered = new ArrayList<>();
        this.followedIds = new HashSet<>();
    }

    public void setSocieties(List<Society> societies) {
        all = societies != null ? new ArrayList<>(societies) : new ArrayList<>();
        filtered = new ArrayList<>(all);
        notifyDataSetChanged();
    }

    public void setFollowedIds(Set<String> ids) {
        followedIds.clear();
        if (ids != null) {
            followedIds.addAll(ids);
        }
        notifyDataSetChanged();
    }

    public void filter(String query) {
        filtered.clear();
        String q = query == null ? "" : query.trim().toLowerCase(Locale.US);
        if (q.isEmpty()) {
            filtered.addAll(all);
        } else {
            for (Society s : all) {
                String name = s.getName() != null ? s.getName().toLowerCase(Locale.US) : "";
                String ac = s.getAcronym() != null ? s.getAcronym().toLowerCase(Locale.US) : "";
                if (name.contains(q) || ac.contains(q)) {
                    filtered.add(s);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_society_row, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Society s = filtered.get(position);
        holder.nameText.setText(s.getDisplayLabel());
        holder.acronymText.setVisibility(View.GONE);

        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        int color = ACCENT_COLORS[Math.abs(s.getAccentIndex()) % ACCENT_COLORS.length];
        circle.setColor(color);
        holder.iconFrame.setBackground(circle);

        boolean following = s.getId() != null && followedIds.contains(s.getId());
        holder.followButton.setText(following
                ? context.getString(R.string.following)
                : context.getString(R.string.follow));
        holder.followButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                following ? 0xFFFFF3E0 : 0xFF1976D2));
        holder.followButton.setTextColor(following ? 0xFF5D4037 : 0xFFFFFFFF);

        holder.itemView.setOnClickListener(v -> listener.onOpenSociety(s));
        holder.followButton.setOnClickListener(v ->
                listener.onToggleFollow(s, following));
    }

    @Override
    public int getItemCount() {
        return filtered.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final FrameLayout iconFrame;
        final TextView nameText;
        final TextView acronymText;
        final Button followButton;

        Holder(@NonNull View itemView) {
            super(itemView);
            iconFrame = itemView.findViewById(R.id.society_icon_frame);
            nameText = itemView.findViewById(R.id.society_name_text);
            acronymText = itemView.findViewById(R.id.society_acronym_text);
            followButton = itemView.findViewById(R.id.society_follow_button);
        }
    }
}
