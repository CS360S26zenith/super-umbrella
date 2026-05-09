package com.example.campuseventstest.view;

import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campuseventstest.R;
import com.example.campuseventstest.service.FirestoreService;
import com.example.campuseventstest.utils.Constants;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Displays a generated ticket payload as QR text representation.
 */
public class TicketActivity extends AppCompatActivity {
    private TextView ticketText;
    private ImageView qrImage;
    private FirestoreService firestoreService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket);

        ticketText = findViewById(R.id.ticket_text);
        qrImage = findViewById(R.id.ticket_qr_image);
        firestoreService = new FirestoreService();

        String rawEventId = getIntent().getStringExtra(Constants.EXTRA_EVENT_ID);
        if (rawEventId == null || rawEventId.isEmpty()) {
            rawEventId = getIntent().getStringExtra("eventId");
        }
        final String resolvedEventId = rawEventId;

        String rawTitle = getIntent().getStringExtra(Constants.EXTRA_EVENT_TITLE);
        if (rawTitle == null) {
            rawTitle = getIntent().getStringExtra("eventTitle");
        }
        final String resolvedEventTitle = rawTitle;

        if (FirebaseAuth.getInstance().getCurrentUser() == null
                || resolvedEventId == null || resolvedEventId.isEmpty()) {
            ticketText.setText("Unable to load ticket: missing sign-in or event.");
            return;
        }
        ticketText.setText("Loading ticket…");
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firestoreService.generateTicketForRsvp(userId, resolvedEventId, new FirestoreService.StringCallback() {
            @Override
            public void onSuccess(String value) {
                Bitmap bitmap = generateQrBitmap(value, 512);
                if (bitmap != null) {
                    qrImage.setImageBitmap(bitmap);
                }
                ticketText.setText("Event: "
                        + (resolvedEventTitle != null ? resolvedEventTitle : "(unknown)")
                        + "\n\nQR Payload:\n" + value);
            }

            @Override
            public void onFailure(String error) {
                ticketText.setText("Could not load ticket.\n\n" + error + "\n\n"
                        + "If you just RSVP'd, wait a moment and tap Back, then open View Ticket again. "
                        + "Also confirm Firestore rules allow read/write on the \"tickets\" collection.");
            }
        });
    }

    private Bitmap generateQrBitmap(String payload, int size) {
        try {
            BitMatrix matrix = new QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, size, size);
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bitmap.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bitmap;
        } catch (WriterException e) {
            return null;
        }
    }
}
