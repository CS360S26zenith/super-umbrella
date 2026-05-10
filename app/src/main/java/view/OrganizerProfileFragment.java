package com.example.campuseventstest.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.campuseventstest.R;
import com.example.campuseventstest.model.User;
import com.example.campuseventstest.service.AuthService;
import com.example.campuseventstest.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Organizer profile tab — quick links and sign-out.
 */
public class OrganizerProfileFragment extends Fragment {

    private AuthService authService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_organizer_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);

        authService = new AuthService();

        TextView nameView = root.findViewById(R.id.profile_name);
        TextView emailView = root.findViewById(R.id.profile_email);
        TextView orgView = root.findViewById(R.id.profile_organization);
        Button createBtn = root.findViewById(R.id.profile_create_event);
        Button notifBtn = root.findViewById(R.id.profile_notifications);
        Button historyBtn = root.findViewById(R.id.profile_events_history);
        Button signOutBtn = root.findViewById(R.id.profile_sign_out);

        FirebaseUser fb = FirebaseAuth.getInstance().getCurrentUser();
        if (fb == null) {
            Toast.makeText(requireContext(), "Not signed in.", Toast.LENGTH_SHORT).show();
            return;
        }
        emailView.setText(fb.getEmail());
        nameView.setText(fb.getDisplayName() != null ? fb.getDisplayName() : fb.getEmail());

        FirebaseFirestore.getInstance()
                .collection(Constants.COLLECTION_USERS)
                .document(fb.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    User u = doc.toObject(User.class);
                    if (u != null) {
                        if (!TextUtils.isEmpty(u.getName())) {
                            nameView.setText(u.getName());
                        }
                        if (!TextUtils.isEmpty(u.getOrganizationName())) {
                            orgView.setVisibility(View.VISIBLE);
                            orgView.setText(u.getOrganizationName());
                        }
                    }
                });

        createBtn.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CreateEventActivity.class)));

        notifBtn.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), NotificationsActivity.class)));

        historyBtn.setOnClickListener(v -> {
            if (getActivity() instanceof OrganizerMainActivity) {
                ((OrganizerMainActivity) requireActivity())
                        .selectNav(R.id.nav_organizer_my_events);
            }
        });

        signOutBtn.setOnClickListener(v -> {
            authService.logout();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });
    }
}
