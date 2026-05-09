package com.example.campuseventstest.view;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campuseventstest.R;
import com.example.campuseventstest.service.FirestoreService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Staff analytics dashboard with core platform metrics.
 */
public class AnalyticsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);
        TextView summaryText = findViewById(R.id.analytics_summary_text);
        LinearLayout chartContainer = findViewById(R.id.category_chart_container);

        FirestoreService firestoreService = new FirestoreService();
        firestoreService.getPlatformAnalyticsDetailed(new FirestoreService.AnalyticsCallback() {
            @Override
            public void onSuccess(FirestoreService.PlatformAnalytics analytics) {
                summaryText.setText(
                        "Total Events: " + analytics.getTotalEvents() + "\n"
                                + "Total RSVPs: " + analytics.getTotalRsvps() + "\n"
                                + "Live: " + analytics.getLiveEvents() + " • Draft: "
                                + analytics.getDraftEvents() + " • Cancelled: "
                                + analytics.getCancelledEvents() + "\n"
                                + "Avg RSVPs/Event: " + analytics.getAverageRsvpsPerEvent()
                );
                renderCategoryChart(chartContainer, analytics.getCategoryCounts());
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(AnalyticsActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderCategoryChart(LinearLayout container, Map<String, Integer> categoryCounts) {
        container.removeAllViews();
        if (categoryCounts == null || categoryCounts.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No category data available yet.");
            container.addView(empty);
            return;
        }

        List<Integer> values = new ArrayList<>(categoryCounts.values());
        int max = 1;
        for (Integer v : values) {
            if (v != null && v > max) {
                max = v;
            }
        }

        for (Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
            int value = entry.getValue() == null ? 0 : entry.getValue();
            TextView label = new TextView(this);
            label.setText(entry.getKey() + ": " + value + " event(s)");
            container.addView(label);

            ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            bar.setMax(max);
            bar.setProgress(value);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            lp.bottomMargin = 16;
            bar.setLayoutParams(lp);
            container.addView(bar);
        }
    }
}
