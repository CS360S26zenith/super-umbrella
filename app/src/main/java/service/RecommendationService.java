package com.example.campuseventstest.service;

import com.example.campuseventstest.model.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Provides lightweight recommendation ranking for student event discovery.
 */
public class RecommendationService {

    /**
     * Ranks events by relevance: {@code preferredCategories} adds a strong boost when an event's
     * category matches (e.g. chip selection or categories inferred from past RSVPs).
     * An empty {@code preferredCategories} list ranks by {@link Event#getRsvpCount()} only (popularity).
     */
    public List<Event> rankEvents(List<Event> allEvents, List<String> preferredCategories) {
        List<Event> ranked = new ArrayList<>(allEvents);
        ranked.sort((left, right) -> Integer.compare(score(right, preferredCategories),
                score(left, preferredCategories)));
        return ranked;
    }

    private int score(Event event, List<String> preferredCategories) {
        int score = event.getRsvpCount();
        if (event.getCategory() != null) {
            String category = event.getCategory().toLowerCase(Locale.getDefault());
            for (String preferred : preferredCategories) {
                if (category.equals(preferred.toLowerCase(Locale.getDefault()))) {
                    score += 100;
                    break;
                }
            }
        }
        return score;
    }
}
