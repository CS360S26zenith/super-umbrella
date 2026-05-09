package com.example.campuseventstest.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campuseventstest.R;
import com.example.campuseventstest.model.User;
import com.example.campuseventstest.service.AuthService;
import com.example.campuseventstest.utils.Constants;

/**
 * Registration screen — creates a new user account with email, password,
 * display name, and role selection (student or organizer).
 * Implements US-24 (Firebase Auth registration).
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText nameInput;
    private EditText emailInput;
    private EditText passwordInput;
    private EditText organizationNameInput;
    private EditText organizationBioInput;
    private RadioGroup roleGroup;
    private Button registerButton;
    private ProgressBar progressBar;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authService = new AuthService();

        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        organizationNameInput = findViewById(R.id.organizationNameInput);
        organizationBioInput = findViewById(R.id.organizationBioInput);
        roleGroup = findViewById(R.id.roleGroup);
        registerButton = findViewById(R.id.registerButton);
        progressBar = findViewById(R.id.progressBar);

        registerButton.setOnClickListener(v -> attemptRegister());
        roleGroup.setOnCheckedChangeListener((group, checkedId) -> {
            boolean organizer = checkedId == R.id.roleOrganizer;
            organizationNameInput.setVisibility(organizer ? View.VISIBLE : View.GONE);
            organizationBioInput.setVisibility(organizer ? View.VISIBLE : View.GONE);
        });
    }

    /**
     * Attempts registration with the entered details.
     */
    private void attemptRegister() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String organizationName = organizationNameInput.getText().toString().trim();
        String organizationBio = organizationBioInput.getText().toString().trim();

        int selectedId = roleGroup.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
            return;
        }
        String role;
        if (selectedId == R.id.roleOrganizer) {
            role = Constants.ROLE_ORGANIZER;
        } else {
            role = Constants.ROLE_STUDENT;
        }

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (Constants.ROLE_ORGANIZER.equals(role)) {
            if (TextUtils.isEmpty(organizationName)) {
                Toast.makeText(this, "Organizer must provide organization name", Toast.LENGTH_SHORT).show();
                return;
            }
            if (organizationBio.length() < 10) {
                Toast.makeText(this, "Organization bio should be at least 10 characters",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            organizationName = null;
            organizationBio = null;
        }

        progressBar.setVisibility(View.VISIBLE);
        registerButton.setEnabled(false);

        authService.register(email, password, name, role, organizationName, organizationBio,
                new AuthService.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(RegisterActivity.this, "Account created! Please login.",
                        Toast.LENGTH_LONG).show();
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                registerButton.setEnabled(true);
                Toast.makeText(RegisterActivity.this, "Failed: " + error,
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}