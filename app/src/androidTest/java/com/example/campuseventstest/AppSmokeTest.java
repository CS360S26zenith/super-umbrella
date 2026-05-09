package com.example.campuseventstest;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.campuseventstest.view.EventDetailActivity;
import com.example.campuseventstest.view.LoginActivity;
import com.example.campuseventstest.view.TicketActivity;
import com.example.campuseventstest.utils.Constants;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AppSmokeTest {
    @Test
    public void loginScreenLaunches() {
        try (ActivityScenario<LoginActivity> ignored = ActivityScenario.launch(LoginActivity.class)) {
            // Smoke test: activity should launch without crashing.
        }
    }

    @Test
    public void ticketScreenShowsMissingSignInMessageWithoutAuth() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), TicketActivity.class);
        intent.putExtra(Constants.EXTRA_EVENT_ID, "event123");
        try (ActivityScenario<TicketActivity> ignored = ActivityScenario.launch(intent)) {
            onView(withId(R.id.ticket_text))
                    .check(matches(withText("Unable to load ticket: missing sign-in or event.")));
        }
    }

    @Test
    public void eventDetailHandlesMissingEventIdGracefully() {
        try (ActivityScenario<EventDetailActivity> ignored = ActivityScenario.launch(EventDetailActivity.class)) {
            // Activity should self-finish without crash when eventId is missing.
        }
    }
}
