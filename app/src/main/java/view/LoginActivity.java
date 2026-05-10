package com.example.campuseventstest.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campuseventstest.R;
import com.example.campuseventstest.model.User;
import com.example.campuseventstest.service.AuthService;
import com.example.campuseventstest.utils.Constants;

/**
 * Login screen — handles user authentication via email/password.
 * Implements US-24 (Firebase Auth) and US-25 (Role-based routing).
 * Routes user to EventListActivity (student) or OrganizerMainActivity (organizer).
 */
public class LoginActivity extends AppCompatActivity {

    private EditText emailInput;
    private EditText passwordInput;
    private Button loginButton;
    private TextView registerLink;
    private ProgressBar progressBar;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authService = new AuthService();

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        registerLink = findViewById(R.id.registerLink);
        progressBar = findViewById(R.id.progressBar);

        loginButton.setOnClickListener(v -> attemptLogin());
        registerLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    /**
     * Attempts login with the entered email and password.
     */
    private void attemptLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);

        authService.login(email, password, new AuthService.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                progressBar.setVisibility(View.GONE);
                loginButton.setEnabled(true);
                authService.syncPushToken();
                routeUser(user);
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                loginButton.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Login failed: " + error,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Routes the user to the correct activity based on their role.
     *
     * @param user the authenticated user
     */
    private void routeUser(User user) {
        Intent intent;
        if (user.isOrganizer()) {
            intent = new Intent(this, OrganizerMainActivity.class);
        } else if (Constants.ROLE_STAFF.equalsIgnoreCase(user.getRole())) {
            intent = new Intent(this, StaffApprovalActivity.class);
        } else {
            intent = new Intent(this, EventListActivity.class);
        }
        intent.putExtra(Constants.EXTRA_USER_ROLE, user.getRole());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}