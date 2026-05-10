package com.example.campuseventstest.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuseventstest.R;
import com.example.campuseventstest.model.Society;
import com.example.campuseventstest.model.User;
import com.example.campuseventstest.service.FirestoreService;
import com.example.campuseventstest.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Student-facing directory of campus societies with search, follow, and drill-down to events.
 */
public class SocietiesListActivity extends AppCompatActivity implements CampusSocietyAdapter.Listener {

    private RecyclerView recyclerView;
    private EditText searchInput;
    private ProgressBar loadingBar;
    private CampusSocietyAdapter adapter;
    private FirestoreService firestoreService;
    private String userId;
    private Set<String> followedIds = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_societies_list);

        firestoreService = new FirestoreService();

        Toolbar toolbar = findViewById(R.id.societies_toolbar);
        toolbar.setTitle(R.string.campus_societies_title);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.societies_recycler);
        searchInput = findViewById(R.id.societies_search);
        loadingBar = findViewById(R.id.societies_loading);

        adapter = new CampusSocietyAdapter(this, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s != null ? s.toString() : "");
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Please sign in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        loadFollowedThenSocieties();
    }

    private void loadFollowedThenSocieties() {
        loadingBar.setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance()
                .collection(Constants.COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    User u = doc.toObject(User.class);
                    followedIds.clear();
                    if (u != null && u.getFollowedSocietyIds() != null) {
                        followedIds.addAll(u.getFollowedSocietyIds());
                    }
                    adapter.setFollowedIds(followedIds);
                    loadSocieties();
                })
                .addOnFailureListener(e -> {
                    loadingBar.setVisibility(View.GONE);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadSocieties() {
        firestoreService.getSocieties(new FirestoreService.SocietyListCallback() {
            @Override
            public void onSuccess(List<Society> societies) {
                loadingBar.setVisibility(View.GONE);
                adapter.setSocieties(societies);
                adapter.filter(searchInput.getText().toString());
            }

            @Override
            public void onFailure(String error) {
                loadingBar.setVisibility(View.GONE);
                Toast.makeText(SocietiesListActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onOpenSociety(Society society) {
        Intent i = new Intent(this, SocietyEventsActivity.class);
        i.putExtra(Constants.EXTRA_SOCIETY_ID, society.getId());
        i.putExtra(Constants.EXTRA_SOCIETY_NAME, society.getDisplayLabel());
        startActivity(i);
    }

    @Override
    public void onToggleFollow(Society society, boolean currentlyFollowing) {
        if (society.getId() == null) {
            return;
        }
        firestoreService.setFollowingSociety(userId, society.getId(), !currentlyFollowing,
                new FirestoreService.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        if (currentlyFollowing) {
                            followedIds.remove(society.getId());
                        } else {
                            followedIds.add(society.getId());
                        }
                        adapter.setFollowedIds(followedIds);
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(SocietiesListActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
