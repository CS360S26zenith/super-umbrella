package com.example.campuseventstest.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.campuseventstest.R;
import com.example.campuseventstest.model.Event;
import com.example.campuseventstest.model.User;
import com.example.campuseventstest.service.AuthService;
import com.example.campuseventstest.service.FirestoreService;
import com.example.campuseventstest.utils.Constants;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.List;

/**
 * Student profile — stats, quick links, sign-out.
 */
public class StudentProfileFragment extends Fragment {

    private FirestoreService firestoreService;
    private AuthService authService;

    private TextView nameView;
    private TextView emailView;
    private TextView statAttendedView;
    private TextView statMonthView;
    private TextView statFollowingView;

    private boolean loadedUser;
    private boolean loadedRsvp;
    private List<Event> rsvpCache = new java.util.ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_student_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestoreService = new FirestoreService();
        authService = new AuthService();

        nameView = view.findViewById(R.id.student_profile_name);
        emailView = view.findViewById(R.id.student_profile_email);
        statAttendedView = view.findViewById(R.id.profile_stat_attended);
        statMonthView = view.findViewById(R.id.profile_stat_month);
        statFollowingView = view.findViewById(R.id.profile_stat_following);

        ImageButton notif = view.findViewById(R.id.profile_notif_button);
        notif.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), NotificationsActivity.class)));

        view.findViewById(R.id.row_attendance_history).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), MyCalendarActivity.class)));

        view.findViewById(R.id.row_my_societies).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), SocietiesListActivity.class)));

        view.findViewById(R.id.row_qr_checkin).setOnClickListener(v -> {
            if (getActivity() instanceof StudentMainActivity) {
                ((StudentMainActivity) requireActivity()).selectNav(R.id.nav_student_tickets);
            }
        });

        view.findViewById(R.id.row_my_payments).setOnClickListener(v ->
                Toast.makeText(requireContext(), R.string.payments_placeholder, Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.student_privacy_button).setOnClickListener(v ->
                Toast.makeText(requireContext(), R.string.privacy_placeholder, Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.student_sign_out_button).setOnClickListener(v -> {
            authService.logout();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        loadProfile();
    }

    private void loadProfile() {
        FirebaseUser fb = FirebaseAuth.getInstance().getCurrentUser();
        if (fb == null) {
            return;
        }
        loadedUser = false;
        loadedRsvp = false;

        emailView.setText(fb.getEmail());
        nameView.setText(fb.getDisplayName() != null && !fb.getDisplayName().trim().isEmpty()
                ? fb.getDisplayName()
                : fb.getEmail());

        FirebaseFirestore.getInstance()
                .collection(Constants.COLLECTION_USERS)
                .document(fb.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    User u = doc.toObject(User.class);
                    if (u != null && u.getName() != null && !u.getName().trim().isEmpty()) {
                        nameView.setText(u.getName());
                    }
                    int following = u != null ? u.getFollowedSocietyIds().size() : 0;
                    statFollowingView.setText(String.valueOf(following));
                    loadedUser = true;
                    mergeProfileStats();
                })
                .addOnFailureListener(e -> {
                    statFollowingView.setText("0");
                    loadedUser = true;
                    mergeProfileStats();
                });

        firestoreService.getRsvpedEventsByStudent(fb.getUid(), new FirestoreService.EventListCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                rsvpCache = events != null ? events : new java.util.ArrayList<>();
                loadedRsvp = true;
                mergeProfileStats();
            }

            @Override
            public void onFailure(String error) {
                rsvpCache = new java.util.ArrayList<>();
                loadedRsvp = true;
                mergeProfileStats();
            }
        });
    }

    private void mergeProfileStats() {
        if (!loadedUser || !loadedRsvp) {
            return;
        }
        statAttendedView.setText(String.valueOf(rsvpCache.size()));
        statMonthView.setText(String.valueOf(countThisCalendarMonth(rsvpCache)));
    }

    private static int countThisCalendarMonth(List<Event> events) {
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        int n = 0;
        for (Event e : events) {
            Timestamp ts = e.getDate();
            if (ts == null) {
                continue;
            }
            cal.setTime(ts.toDate());
            if (cal.get(Calendar.MONTH) == month && cal.get(Calendar.YEAR) == year) {
                n++;
            }
        }
        return n;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfile();
    }
}
