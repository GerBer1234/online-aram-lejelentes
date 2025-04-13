package com.example.emeter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailEditText;
    private Button resetButton, cancelButton;

    private FirebaseAuth mAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.forgotPasswordLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        emailEditText = findViewById(R.id.forgotEmailEditText);
        resetButton = findViewById(R.id.resetPasswordButton);
        cancelButton = findViewById(R.id.cancelForgotButton);
        mAuth = FirebaseAuth.getInstance();

        resetButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Add meg az e-mail címedet!", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "E-mail elküldve a jelszó visszaállításához.", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            String error = task.getException() != null ? task.getException().getMessage() : "Ismeretlen hiba.";
                            if (error.contains("badly formatted")) {
                                Toast.makeText(this, "Hibás e-mail formátum.", Toast.LENGTH_SHORT).show();
                            } else if (error.contains("no user record")) {
                                Toast.makeText(this, "Nem található felhasználó ezzel az e-mail címmel.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Hiba: " + error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });

        cancelButton.setOnClickListener(v -> {
            finish();
        });
    }
}
