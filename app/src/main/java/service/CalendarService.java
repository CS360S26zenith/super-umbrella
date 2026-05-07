package com.example.campuseventstest.service;

import android.content.Intent;
import android.provider.CalendarContract;

import com.example.campuseventstest.model.Event;

/**
 * Builds intents for exporting events into external calendar apps.
 */
public class CalendarService {

    public Intent buildCalendarInsertIntent(Event event) {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setData(CalendarContract.Events.CONTENT_URI);
        intent.putExtra(CalendarContract.Events.TITLE, event.getTitle());
        intent.putExtra(CalendarContract.Events.DESCRIPTION, event.getDescription());
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, event.getVenue());
        if (event.getDate() != null) {
            long start = event.getDate().toDate().getTime();
            intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start);
            intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, start + (60L * 60L * 1000L));
        }
        return intent;
    }
}
