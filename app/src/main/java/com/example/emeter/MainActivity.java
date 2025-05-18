package com.example.emeter;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

/**
 * A bejelentkezési képernyő inicializálása. Beállítja a mezőket, eseménykezelőket,
 * valamint kezeli a bejelentkezési, regisztrációs és jelszóemlékeztető gombokat.
 */
public class MainActivity extends BaseActivity {
    private EditText emailEditText, passwordEditText;
    private FirebaseAuth mAuth;

    /**
     * Inicializálás.
     *
     * @param savedInstanceState Előzőleg elmentett állapot, ha van.
     */
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
        Button loginButton = findViewById(R.id.loginButton);
        Button registerButton = findViewById(R.id.registerButton);
        mAuth = FirebaseAuth.getInstance();

        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, R.string.error_email_required, Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, R.string.error_password_required, Toast.LENGTH_SHORT).show();
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
                            String errorMessage = getFriendlyErrorMessage(Objects.requireNonNull(task.getException()).getMessage());
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

    /**
     * A Firebase által visszaadott hibaüzenetet alakítja felhasználóbarát szöveggé.
     *
     * @param technicalMessage A Firebase által visszaadott hibaüzenet.
     * @return A felhasználóbarát hibaüzenet.
     */
    private String getFriendlyErrorMessage(String technicalMessage) {
        if (technicalMessage == null) return getString(R.string.error_generic, "Ismeretlen hiba.");

        if (technicalMessage.contains("badly formatted")) {
            return getString(R.string.error_bad_email_format);
        } else if (technicalMessage.contains("no user record")) {
            return getString(R.string.error_no_user);
        } else if (technicalMessage.contains("password is invalid")) {
            return getString(R.string.error_wrong_password);
        } else if (technicalMessage.contains("interrupted connection")) {
            return getString(R.string.error_no_internet);
        } else if (technicalMessage.contains("blocked all requests")) {
            return getString(R.string.error_too_many_attempts);
        } else if (technicalMessage.contains("auth credential is incorrect")) {
            return getString(R.string.error_invalid_credentials);
        }

        return getString(R.string.error_generic, technicalMessage);
    }
}
