package com.example.campuseventstest.view;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campuseventstest.R;
import com.example.campuseventstest.service.FirestoreService;

import java.util.Map;

/**
 * Staff analytics dashboard with core platform metrics.
 */
public class AnalyticsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);
        TextView analyticsText = findViewById(R.id.analytics_text);

        FirestoreService firestoreService = new FirestoreService();
        firestoreService.getPlatformAnalytics(new FirestoreService.MapCallback() {
            @Override
            public void onSuccess(Map<String, Integer> metrics) {
                analyticsText.setText(
                        "Total Events: " + metrics.getOrDefault("totalEvents", 0) + "\n" +
                        "Total RSVPs: " + metrics.getOrDefault("totalRsvps", 0) + "\n" +
                        "Top Category Event Count: " + metrics.getOrDefault("topCategoryCount", 0)
                );
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(AnalyticsActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
