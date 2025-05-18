package com.example.emeter;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import android.widget.ImageButton;

/**
 * Megjeleníti az összes bejelentést.
 * A lista RecyclerView-ban jelenik meg, időrendi sorrendben (legújabb elől).
 */
public class AllReadingsActivity extends BaseActivity {
    private ReadingAdapter adapter;
    private final List<Reading> readingList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_readings);

        // A vissza gomb bezárja az activity-t
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // A RecyclerView konfigurálása
        RecyclerView allReadingsRecyclerView = findViewById(R.id.allReadingsRecyclerView);
        allReadingsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Az adapter inicializálása
        adapter = new ReadingAdapter(this, readingList, null);
        allReadingsRecyclerView.setAdapter(adapter);

        // A bejelentések betöltése
        fetchAllReadings();
    }

    /**
     * Lekéri az összes bejelentést a Firestore-ból, majd frissíti az adaptert.
     */
    @SuppressLint("NotifyDataSetChanged")
    private void fetchAllReadings() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
                .collection("readings")
                .document(user.getUid())
                .collection("user_readings")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    readingList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Reading reading = doc.toObject(Reading.class);
                        if (reading != null) {
                            reading.setId(doc.getId());
                            readingList.add(reading);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Hiba a bejelentések lekérdezésekor.",
                                Toast.LENGTH_SHORT).show()
                );
    }
}
