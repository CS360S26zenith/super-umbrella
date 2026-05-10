package com.example.campuseventstest.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuseventstest.R;
import com.example.campuseventstest.model.Society;
import com.example.campuseventstest.model.User;
import com.example.campuseventstest.service.FirestoreService;
import com.example.campuseventstest.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Societies the student follows — tap through to past/upcoming events for each.
 */
public class StudentFollowedSocietiesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_followed_societies);

        ProgressBar loading = findViewById(R.id.followed_loading);
        TextView empty = findViewById(R.id.followed_empty);
        RecyclerView recyclerView = findViewById(R.id.followed_recycler);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            finish();
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Adapter adapter = new Adapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loading.setVisibility(View.VISIBLE);

        FirebaseFirestore.getInstance()
                .collection(Constants.COLLECTION_USERS)
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    User u = doc.toObject(User.class);
                    Set<String> followed = new HashSet<>();
                    if (u != null && u.getFollowedSocietyIds() != null) {
                        followed.addAll(u.getFollowedSocietyIds());
                    }
                    if (followed.isEmpty()) {
                        loading.setVisibility(View.GONE);
                        empty.setVisibility(View.VISIBLE);
                        return;
                    }
                    new FirestoreService().getSocieties(new FirestoreService.SocietyListCallback() {
                        @Override
                        public void onSuccess(List<Society> all) {
                            loading.setVisibility(View.GONE);
                            List<Society> mine = new ArrayList<>();
                            if (all != null) {
                                for (Society s : all) {
                                    if (s.getId() != null && followed.contains(s.getId())) {
                                        mine.add(s);
                                    }
                                }
                            }
                            adapter.setData(mine);
                            empty.setVisibility(mine.isEmpty() ? View.VISIBLE : View.GONE);
                        }

                        @Override
                        public void onFailure(String error) {
                            loading.setVisibility(View.GONE);
                            Toast.makeText(StudentFollowedSocietiesActivity.this, error,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    loading.setVisibility(View.GONE);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private class Adapter extends RecyclerView.Adapter<Adapter.VH> {

        private final List<Society> items = new ArrayList<>();

        void setData(List<Society> societies) {
            items.clear();
            items.addAll(societies);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_followed_society_row, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            Society s = items.get(position);
            holder.name.setText(s.getDisplayLabel());
            holder.itemView.setOnClickListener(v -> {
                Intent i = new Intent(StudentFollowedSocietiesActivity.this, SocietyEventsActivity.class);
                i.putExtra(Constants.EXTRA_SOCIETY_ID, s.getId());
                i.putExtra(Constants.EXTRA_SOCIETY_NAME, s.getDisplayLabel());
                startActivity(i);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class VH extends RecyclerView.ViewHolder {
            final TextView name;

            VH(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.followed_society_name);
            }
        }
    }
}
