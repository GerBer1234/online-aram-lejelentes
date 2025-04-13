package com.example.emeter;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton, registerButton;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        mAuth = FirebaseAuth.getInstance();

        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(MainActivity.this, "Töltsd ki az összes mezőt!", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Sikeres bejelentkezés!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            String errorMessage = task.getException().getMessage();
                            if (errorMessage != null) {
                                if (errorMessage.contains("badly formatted")) {
                                    errorMessage = "Az e-mail cím formátuma helytelen.";
                                } else if (errorMessage.contains("no user record")) {
                                    errorMessage = "Nem található felhasználó a megadott e-mail címmel.";
                                } else if (errorMessage.contains("password is invalid")) {
                                    errorMessage = "Hibás jelszó. Kérlek, próbáld újra.";
                                } else if (errorMessage.contains("interrupted connection")) {
                                    errorMessage = "Nincs internetkapcsolat.";
                                } else if (errorMessage.contains("blocked all requests")) {
                                    errorMessage = "Túl sok próbálkozás. Próbáld meg később.";
                                } else if (errorMessage.contains("auth credential is incorrect")) {
                                    errorMessage = "A hitelesítési adatok érvénytelenek vagy lejártak.";
                                }

                            }
                            Toast.makeText(MainActivity.this, "Hiba: " + errorMessage, Toast.LENGTH_LONG).show();

                        }
                    });
        });

        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        Button forgotPasswordButton = findViewById(R.id.forgotPasswordButton);
        forgotPasswordButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

    }
}
