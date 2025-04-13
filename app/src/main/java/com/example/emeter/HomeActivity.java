package com.example.emeter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ImageButton logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        logoutButton = findViewById(R.id.logoutButton);

        if (mAuth.getCurrentUser() != null) {
            logoutButton.setVisibility(View.VISIBLE); // mutatjuk az ikont, ha be van jelentkezve
            logoutButton.setOnClickListener(v -> logout());
        } else {
            logoutButton.setVisibility(View.GONE); // ha nem, akkor elrejtjük
        }
    }

    public void logout() {
        mAuth.signOut();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
