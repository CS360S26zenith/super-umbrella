package com.example.campuseventstest;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.campuseventstest.utils.Constants;
import com.example.campuseventstest.view.EventDetailActivity;
import com.example.campuseventstest.view.TicketActivity;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class AuthenticatedRsvpFlowTest {

    @Before
    public void signInFixtureStudent() throws Exception {
        Assume.assumeTrue("Set TEST_STUDENT_EMAIL in gradle.properties",
                !BuildConfig.TEST_STUDENT_EMAIL.isEmpty());
        Assume.assumeTrue("Set TEST_STUDENT_PASSWORD in gradle.properties",
                !BuildConfig.TEST_STUDENT_PASSWORD.isEmpty());
        Assume.assumeTrue("Set TEST_EVENT_ID in gradle.properties",
                !BuildConfig.TEST_EVENT_ID.isEmpty());

        Tasks.await(FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(BuildConfig.TEST_STUDENT_EMAIL, BuildConfig.TEST_STUDENT_PASSWORD),
                20, TimeUnit.SECONDS);
    }

    @After
    public void signOut() {
        FirebaseAuth.getInstance().signOut();
    }

    @Test
    public void authenticatedStudentCanOpenTicketScreen() {
        Intent ticketIntent = new Intent(
                androidx.test.core.app.ApplicationProvider.getApplicationContext(),
                TicketActivity.class
        );
        ticketIntent.putExtra(Constants.EXTRA_EVENT_ID, BuildConfig.TEST_EVENT_ID);
        ticketIntent.putExtra(Constants.EXTRA_EVENT_TITLE, "Fixture Event");
        try (ActivityScenario<TicketActivity> ignored = ActivityScenario.launch(ticketIntent)) {
            onView(withId(R.id.ticket_text)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void authenticatedStudentCanUseRsvpScreenControls() throws InterruptedException {
        Intent detailIntent = new Intent(
                androidx.test.core.app.ApplicationProvider.getApplicationContext(),
                EventDetailActivity.class
        );
        detailIntent.putExtra(Constants.EXTRA_EVENT_ID, BuildConfig.TEST_EVENT_ID);

        try (ActivityScenario<EventDetailActivity> ignored = ActivityScenario.launch(detailIntent)) {
            Thread.sleep(3000L);
            // If already RSVPed, cancel first to normalize test path.
            try {
                onView(withId(R.id.cancel_rsvp_button)).perform(click());
                Thread.sleep(2000L);
            } catch (Exception ignoredException) {
                // Button may be hidden if user is not RSVPed yet.
            }

            onView(withId(R.id.rsvp_button)).perform(click());
            Thread.sleep(2500L);
            onView(withId(R.id.cancel_rsvp_button)).check(matches(isDisplayed()));
            onView(withId(R.id.cancel_rsvp_button)).perform(click());
        }
    }
}
