package com.example.campuseventstest.service;

import static org.junit.Assert.assertEquals;

import com.example.campuseventstest.model.Event;
import com.google.firebase.Timestamp;

import org.junit.Test;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class RecommendationServiceTest {
    @Test
    public void preferredCategoryShouldRankHigher() {
        RecommendationService service = new RecommendationService();
        Calendar soon = Calendar.getInstance();
        soon.add(Calendar.DATE, 2);

        Event preferred = new Event("Preferred", "d", new Timestamp(soon.getTime()), "Hall",
                "Talks", 100, "org");
        preferred.setRsvpCount(2);

        Event popularNonPreferred = new Event("Popular", "d", new Timestamp(soon.getTime()), "Hall",
                "Sports", 100, "org");
        popularNonPreferred.setRsvpCount(20);

        List<Event> ranked = service.rankEvents(
                Arrays.asList(popularNonPreferred, preferred),
                Arrays.asList("Talks"));

        assertEquals("Preferred", ranked.get(0).getTitle());
    }
}
