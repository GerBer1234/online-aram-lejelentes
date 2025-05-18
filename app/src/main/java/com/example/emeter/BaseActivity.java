package com.example.emeter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Egy fejlécet tartalmazó alap activity, amelyet a többi képernyő örökölhet.
 */
public class BaseActivity extends AppCompatActivity {
    protected FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
    }

    @Override
    public void setContentView(int layoutResID) {
        // Betölti az alapot tartalmazó layoutot
        super.setContentView(R.layout.activity_base);

        // A további tartalom betölétse a fejléc alá
        getLayoutInflater().inflate(layoutResID, findViewById(R.id.activityContent), true);

        // Firebase hitelesítés inicializálása
        mAuth = FirebaseAuth.getInstance();

        // Kijelentkezés gomb beállítása
        ImageButton logoutButton = findViewById(R.id.logoutButton);
        if (logoutButton != null) {
            if (mAuth.getCurrentUser() != null) {
                logoutButton.setVisibility(View.VISIBLE);
                logoutButton.setOnClickListener(v -> logout());
            } else {
                logoutButton.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Kijelentkezteti a felhasználót, és visszairányítja a főképernyőre.
     */
    protected void logout() {
        mAuth.signOut();
        startActivity(new Intent(this, MainActivity.class));
        finishAffinity();
    }
}
