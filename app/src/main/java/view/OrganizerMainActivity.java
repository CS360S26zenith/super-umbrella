package com.example.campuseventstest.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.campuseventstest.R;
import com.example.campuseventstest.utils.Constants;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Organizer shell with bottom navigation: Home (dashboard), My Events, Profile.
 */
public class OrganizerMainActivity extends AppCompatActivity {

    public static final String EXTRA_INITIAL_TAB = "extra_organizer_initial_tab";
    public static final String TAB_MY_EVENTS = "my_events";
    public static final String TAB_PROFILE = "profile";

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_main);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Please login first.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bottomNav = findViewById(R.id.organizer_bottom_nav);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance()
                .collection(Constants.COLLECTION_USERS)
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String role = documentSnapshot.getString("role");
                    if (!Constants.ROLE_ORGANIZER.equalsIgnoreCase(role)) {
                        Toast.makeText(this, "Organizer access required.", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                    bottomNav.setOnItemSelectedListener(this::onNavItemSelected);
                    if (savedInstanceState == null) {
                        String tab = getIntent() != null
                                ? getIntent().getStringExtra(EXTRA_INITIAL_TAB) : null;
                        if (TAB_MY_EVENTS.equals(tab)) {
                            selectNav(R.id.nav_organizer_my_events);
                        } else if (TAB_PROFILE.equals(tab)) {
                            selectNav(R.id.nav_organizer_profile);
                        } else {
                            selectNav(R.id.nav_organizer_home);
                        }
                    } else if (getSupportFragmentManager()
                            .findFragmentById(R.id.organizer_fragment_container) == null) {
                        selectNav(R.id.nav_organizer_home);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Unable to verify role: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private boolean onNavItemSelected(@NonNull android.view.MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_organizer_home) {
            replaceFragment(new OrganizerHomeFragment());
            return true;
        }
        if (id == R.id.nav_organizer_my_events) {
            replaceFragment(new OrganizerMyEventsFragment());
            return true;
        }
        if (id == R.id.nav_organizer_profile) {
            replaceFragment(new OrganizerProfileFragment());
            return true;
        }
        return false;
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.organizer_fragment_container, fragment)
                .commit();
    }

    /** Switches bottom navigation and fragment from child fragments (e.g. Profile → My Events). */
    public void selectNav(int menuItemId) {
        bottomNav.setOnItemSelectedListener(null);
        bottomNav.setSelectedItemId(menuItemId);
        bottomNav.setOnItemSelectedListener(this::onNavItemSelected);
        if (menuItemId == R.id.nav_organizer_home) {
            replaceFragment(new OrganizerHomeFragment());
        } else if (menuItemId == R.id.nav_organizer_my_events) {
            replaceFragment(new OrganizerMyEventsFragment());
        } else if (menuItemId == R.id.nav_organizer_profile) {
            replaceFragment(new OrganizerProfileFragment());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        String tab = intent.getStringExtra(EXTRA_INITIAL_TAB);
        if (TAB_MY_EVENTS.equals(tab)) {
            selectNav(R.id.nav_organizer_my_events);
        } else if (TAB_PROFILE.equals(tab)) {
            selectNav(R.id.nav_organizer_profile);
        }
    }
}
