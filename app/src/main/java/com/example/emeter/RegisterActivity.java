package com.example.emeter;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {
    private static final String LOG_TAG = RegisterActivity.class.getName();

    private EditText fullNameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText passwordAgainEditText;
    private EditText phoneEditText;
    private EditText addressEditText;

    private FirebaseAuth mAuth;

    private final List<String> validAreaCodes = Arrays.asList(
            "1", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29",
            "30", "31", "32", "33", "34", "35", "36", "37", "40", "42", "44",
            "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "56",
            "57", "59", "62", "63", "66", "68", "69", "70", "72", "73", "74",
            "75", "76", "77", "78", "79", "80", "82", "83", "84", "85", "87",
            "88", "89", "90", "91", "92", "93", "94", "95", "96", "99"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registerLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fullNameEditText = findViewById(R.id.fullNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        passwordAgainEditText = findViewById(R.id.passwordAgainEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        addressEditText = findViewById(R.id.addressEditText);

        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.registerConfirmButton).setOnClickListener(this::register);
        findViewById(R.id.registerCancelButton).setOnClickListener(this::cancel);

        phoneEditText.setInputType(InputType.TYPE_CLASS_PHONE);

        phoneEditText.addTextChangedListener(new TextWatcher() {
            boolean editing;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (editing) return;
                editing = true;

                // Csak akkor egészítjük ki, ha már elkezdett gépelni
                if (s.length() > 0 && !s.toString().startsWith("+36")) {
                    String digitsOnly = s.toString().replaceAll("[^\\d]", "");
                    phoneEditText.setText("+36" + digitsOnly);
                    phoneEditText.setSelection(phoneEditText.getText().length());
                }

                editing = false;
            }
        });

        phoneEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String input = phoneEditText.getText().toString().replaceAll("[^\\d]", "");
                if (input.startsWith("36")) input = input.substring(2);

                String areaCode = "";
                String number = "";

                if (input.length() >= 2 && validAreaCodes.contains(input.substring(0, 2))) {
                    areaCode = input.substring(0, 2);
                    number = input.substring(2);
                } else if (input.length() >= 1 && validAreaCodes.contains(input.substring(0, 1))) {
                    areaCode = input.substring(0, 1);
                    number = input.substring(1);
                } else {
                    phoneEditText.setError("Érvénytelen körzetszám.");
                    return;
                }

                if (number.length() < 6 || number.length() > 7) {
                    phoneEditText.setError("A szám hossza érvénytelen (6-7 számjegy szükséges).");
                    return;
                }

                String formatted = "+36-" + areaCode + "/" +
                        number.substring(0, 3) + "-" + number.substring(3);
                phoneEditText.setText(formatted);
            }
        });
    }

    public void register(View view) {
        Log.d(LOG_TAG, "register() meghívódott");

        String fullName = fullNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String passwordAgain = passwordAgainEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || passwordAgain.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Minden mezőt ki kell tölteni!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (fullName.length() < 3) {
            fullNameEditText.setError("A névnek legalább 3 karakter hosszúnak kell lennie.");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Érvénytelen email formátum.");
            return;
        }

        if (!password.equals(passwordAgain)) {
            Toast.makeText(this, "A jelszavak nem egyeznek!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$")) {
            passwordEditText.setError("A jelszónak legalább 8 karakterből kell állnia, tartalmaznia kell kis- és nagybetűt, számot és speciális karaktert.");
            return;
        }

        if (address.isEmpty()) {
            addressEditText.setError("A lakcím mező nem lehet üres.");
            return;
        }

        String rawDigits = phone.replaceAll("[^\\d]", "");
        if (rawDigits.startsWith("36")) rawDigits = rawDigits.substring(2);

        String areaCode = "";
        String number = "";

        if (rawDigits.length() >= 2 && validAreaCodes.contains(rawDigits.substring(0, 2))) {
            areaCode = rawDigits.substring(0, 2);
            number = rawDigits.substring(2);
        } else if (rawDigits.length() >= 1 && validAreaCodes.contains(rawDigits.substring(0, 1))) {
            areaCode = rawDigits.substring(0, 1);
            number = rawDigits.substring(1);
        } else {
            phoneEditText.setError("Érvénytelen körzetszám.");
            return;
        }

        boolean isMobile = areaCode.equals("20") || areaCode.equals("30") || areaCode.equals("70") || areaCode.equals("50");

        if ((isMobile && number.length() != 7) || (!isMobile && (number.length() < 6 || number.length() > 7))) {
            phoneEditText.setError("Hibás a hossza a számnak. Mobilnál 7 számjegy kell, vonalasnál 6 vagy 7.");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(LOG_TAG, "Sikeres regisztráció");
                        Toast.makeText(RegisterActivity.this, "Sikeres regisztráció!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        finish();
                    } else {
                        String errorMsg = task.getException().getMessage();
                        Log.d(LOG_TAG, "Regisztráció sikertelen: " + errorMsg);
                        if (errorMsg != null && errorMsg.contains("email address is already in use")) {
                            Toast.makeText(RegisterActivity.this, "Ez az e-mail cím már regisztrálva van.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Hiba: " + errorMsg, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void cancel(View view) {
        finish();
    }
}
