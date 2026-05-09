package com.example.campuseventstest.view;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campuseventstest.R;

public class HelpAssistantActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_assistant);

        EditText questionInput = findViewById(R.id.help_question_input);
        TextView answerText = findViewById(R.id.help_answer_text);
        Button askButton = findViewById(R.id.help_ask_button);

        askButton.setOnClickListener(v -> {
            String q = questionInput.getText().toString().toLowerCase().trim();
            if (q.contains("rsvp")) {
                answerText.setText("Open an event and tap RSVP. If full, use Join Waitlist.");
            } else if (q.contains("ticket") || q.contains("qr")) {
                answerText.setText("After RSVP, open event details and tap View Ticket to show your QR.");
            } else if (q.contains("create") || q.contains("organizer")) {
                answerText.setText("Organizers can create events from Organizer Dashboard with the + button.");
            } else {
                answerText.setText("Try asking about RSVP, tickets, organizer actions, or notifications.");
            }
        });
    }
}
