package com.example.campuseventstest.view;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Legacy student browse entry — forwards into {@link StudentMainActivity} (Explore tab).
 */
public class EventListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, StudentMainActivity.class);
        intent.putExtra(StudentMainActivity.EXTRA_INITIAL_TAB, StudentMainActivity.TAB_EXPLORE);
        startActivity(intent);
        finish();
    }
}
