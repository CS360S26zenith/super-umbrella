package com.example.campuseventstest.view;

import android.content.Intent;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.campuseventstest.R;
import com.example.campuseventstest.utils.Constants;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Student shell: Home, Explore (browse/search), Tickets (RSVP tickets), Profile.
 */
public class StudentMainActivity extends AppCompatActivity {

    public static final String EXTRA_INITIAL_TAB = "extra_student_initial_tab";
    public static final String TAB_HOME = "home";
    public static final String TAB_EXPLORE = "explore";
    public static final String TAB_TICKETS = "tickets";
    public static final String TAB_PROFILE = "profile";

    private static final int REQ_POST_NOTIFICATIONS = 9201;

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_main);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Please login first.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bottomNav = findViewById(R.id.student_bottom_nav);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance()
                .collection(Constants.COLLECTION_USERS)
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String role = documentSnapshot.getString("role");
                    if (role != null && Constants.ROLE_ORGANIZER.equalsIgnoreCase(role)) {
                        startActivity(new Intent(this, OrganizerMainActivity.class));
                        finish();
                        return;
                    }
                    if (role != null && Constants.ROLE_STAFF.equalsIgnoreCase(role)) {
                        startActivity(new Intent(this, StaffApprovalActivity.class));
                        finish();
                        return;
                    }

                    requestNotificationPermissionIfNeeded();

                    bottomNav.setOnItemSelectedListener(this::onNavItemSelected);
                    if (savedInstanceState == null) {
                        String tab = getIntent() != null
                                ? getIntent().getStringExtra(EXTRA_INITIAL_TAB) : null;
                        if (TAB_EXPLORE.equals(tab)) {
                            selectNav(R.id.nav_student_explore);
                        } else if (TAB_TICKETS.equals(tab)) {
                            selectNav(R.id.nav_student_tickets);
                        } else if (TAB_PROFILE.equals(tab)) {
                            selectNav(R.id.nav_student_profile);
                        } else {
                            selectNav(R.id.nav_student_home);
                        }
                    } else if (getSupportFragmentManager()
                            .findFragmentById(R.id.student_fragment_container) == null) {
                        selectNav(R.id.nav_student_home);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Unable to verify profile: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{ Manifest.permission.POST_NOTIFICATIONS },
                        REQ_POST_NOTIFICATIONS);
            }
        }
    }

    private boolean onNavItemSelected(@NonNull android.view.MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_student_home) {
            replaceFragment(new StudentHomeFragment());
            return true;
        }
        if (id == R.id.nav_student_explore) {
            replaceFragment(new StudentExploreFragment());
            return true;
        }
        if (id == R.id.nav_student_tickets) {
            replaceFragment(new StudentTicketsFragment());
            return true;
        }
        if (id == R.id.nav_student_profile) {
            replaceFragment(new StudentProfileFragment());
            return true;
        }
        return false;
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.student_fragment_container, fragment)
                .commit();
    }

    /** Lets child fragments switch tabs (e.g. Home → Explore). */
    public void selectNav(int menuItemId) {
        bottomNav.setOnItemSelectedListener(null);
        bottomNav.setSelectedItemId(menuItemId);
        bottomNav.setOnItemSelectedListener(this::onNavItemSelected);
        if (menuItemId == R.id.nav_student_home) {
            replaceFragment(new StudentHomeFragment());
        } else if (menuItemId == R.id.nav_student_explore) {
            replaceFragment(new StudentExploreFragment());
        } else if (menuItemId == R.id.nav_student_tickets) {
            replaceFragment(new StudentTicketsFragment());
        } else if (menuItemId == R.id.nav_student_profile) {
            replaceFragment(new StudentProfileFragment());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        String tab = intent.getStringExtra(EXTRA_INITIAL_TAB);
        if (TAB_EXPLORE.equals(tab)) {
            selectNav(R.id.nav_student_explore);
        } else if (TAB_TICKETS.equals(tab)) {
            selectNav(R.id.nav_student_tickets);
        } else if (TAB_PROFILE.equals(tab)) {
            selectNav(R.id.nav_student_profile);
        } else if (TAB_HOME.equals(tab)) {
            selectNav(R.id.nav_student_home);
        }
    }
}
