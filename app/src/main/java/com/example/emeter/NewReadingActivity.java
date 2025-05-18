package com.example.emeter;

import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NewReadingActivity extends BaseActivity {
    private EditText startDateEditText, endDateEditText, consumptionEditText, commentEditText;
    private final Calendar startCalendar = Calendar.getInstance();
    private final Calendar endCalendar = Calendar.getInstance();
    private static final int REQUEST_NOTIFICATION_PERMISSION = 102;

    /**
     * Inicializálás.
     *
     * @param savedInstanceState korábbi mentett állapot, ha volt.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_reading);

        startDateEditText = findViewById(R.id.dateEditText);
        endDateEditText = findViewById(R.id.endDateEditText);
        consumptionEditText = findViewById(R.id.consumptionEditText);
        commentEditText = findViewById(R.id.commentEditText);
        Button saveButton = findViewById(R.id.saveButton);
        Button cancelButton = findViewById(R.id.cancelButton);

        startDateEditText.setOnClickListener(v -> {
            new DatePickerDialog(NewReadingActivity.this, startDateSetListener,
                    startCalendar.get(Calendar.YEAR),
                    startCalendar.get(Calendar.MONTH),
                    startCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        endDateEditText.setOnClickListener(v -> {
            new DatePickerDialog(NewReadingActivity.this, endDateSetListener,
                    endCalendar.get(Calendar.YEAR),
                    endCalendar.get(Calendar.MONTH),
                    endCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        saveButton.setOnClickListener(v -> validateAndSaveReading());
        cancelButton.setOnClickListener(v -> finish());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "reading_channel",
                    "Bejelentés értesítések",
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Validáció. Ha minden rendben van, mentést kezdeményez.
     */
    private void validateAndSaveReading() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Nincs bejelentkezve felhasználó.", Toast.LENGTH_SHORT).show();
            return;
        }
        String uid = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String startDate = startDateEditText.getText().toString().trim();
        String endDate = endDateEditText.getText().toString().trim();
        db.collection("readings")
                .document(uid)
                .collection("user_readings")
                .whereEqualTo("startDate", startDate)
                .whereEqualTo("endDate", endDate)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Toast.makeText(this, "Már létezik bejelentés erre az időszakra!", Toast.LENGTH_LONG).show();
                    } else {
                        String consumptionStr = consumptionEditText.getText().toString().trim();
                        String comment = commentEditText.getText().toString().trim();

                        if (startDate.isEmpty() || endDate.isEmpty() || consumptionStr.isEmpty()) {
                            Toast.makeText(this, "Kérlek, a megjegyzésen kívül tölts ki minden mezőt!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            sdf.setLenient(false);

                            Date start = sdf.parse(startDate);
                            Date end = sdf.parse(endDate);

                            if (start == null || end == null) {
                                Toast.makeText(this, "Nem sikerült értelmezni a dátumokat!", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            if (end.before(start)) {
                                Toast.makeText(this, "A záró dátum nem lehet korábbi, mint a kezdő dátum.", Toast.LENGTH_LONG).show();
                                return;
                            }
                        } catch (ParseException e) {
                            Toast.makeText(this, "Érvénytelen dátum formátum!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        double consumption;
                        try {
                            consumption = Double.parseDouble(consumptionStr);
                        } catch (NumberFormatException e) {
                            Toast.makeText(this, "A fogyasztás csak szám lehet!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        saveReadingToFirestore(startDate, endDate, consumption, comment);
                    }
                });
    }

    /**
     * A bejelentési adatokat menti a Firestore adatbázisba.
     *
     * @param startDate   kezdő dátum
     * @param endDate     záró dátum
     * @param consumption a fogyasztás
     * @param comment     megjegyzés, ha van
     */

    private void saveReadingToFirestore(String startDate, String endDate, double consumption, String comment) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        Reading reading = new Reading(startDate, endDate, consumption, comment);
        reading.setCreatedAt(new Date());

        FirebaseFirestore.getInstance()
                .collection("readings")
                .document(user.getUid())
                .collection("user_readings")
                .add(reading)
                .addOnSuccessListener(documentReference -> {
                    // Itt beállítjuk az ID-t és frissítjük a dokumentumot
                    String generatedId = documentReference.getId();
                    documentReference.update("id", generatedId);
                    sendNotification(consumption);
                    Toast.makeText(this, "Sikeres mentés!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Hiba történt: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    /**
     * Értesítést küld a felhasználónak sikeres bejelentés után.
     *
     * @param consumption a fogyasztás
     */
    private void sendNotification(double consumption) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                    REQUEST_NOTIFICATION_PERMISSION);
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "reading_channel")
                .setSmallIcon(R.drawable.logo_for_notification)
                .setContentTitle("Sikeres bejelentés!")
                .setContentText("Rögzítetted a fogyasztást: " + consumption + " kWh.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setTimeoutAfter(10000);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1001, builder.build());
    }

    /**
     * A kezdő dátum kiválasztását kezeli a felugró dátumválasztóból, és frissíti a megfelelő mezőt.
     */
    private final DatePickerDialog.OnDateSetListener startDateSetListener = (view, year, month, dayOfMonth) -> {
        startCalendar.set(year, month, dayOfMonth);
        updateEditText(startDateEditText, startCalendar);
    };

    /**
     * A záró dátum kiválasztását kezeli a felugró dátumválasztóból, és frissíti a megfelelő mezőt.
     */
    private final DatePickerDialog.OnDateSetListener endDateSetListener = (view, year, month, dayOfMonth) -> {
        endCalendar.set(year, month, dayOfMonth);
        updateEditText(endDateEditText, endCalendar);
    };

    /**
     * A kiválasztott dátumot megjeleníti az adott mezőben.
     *
     * @param editText A mező, amelyet frissítünk.
     * @param calendar A dátum, amit meg szeretnénk jeleníteni.
     */
    private void updateEditText(EditText editText, Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        editText.setText(String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, day));
    }
}
