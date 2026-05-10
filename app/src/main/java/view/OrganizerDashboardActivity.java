package com.example.campuseventstest.view;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Legacy entry point — forwards to {@link OrganizerMainActivity} (bottom navigation shell).
 */
public class OrganizerDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivity(new Intent(this, OrganizerMainActivity.class));
        finish();
    }
}
