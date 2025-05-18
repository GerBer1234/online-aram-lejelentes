package com.example.emeter;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * A bejelentkezés utáni képernyő funkcionalitásáért felelős osztály.
 * Kezeli az éves fogyasztási összegzést, az utolsó bejelentést, a három legutóbbi bejegyzés
 * kilistázását, az új bejelentés gombot, valamint az értesítéseket és az animációkat.
 */
public class HomeActivity extends BaseActivity {
    private TextView lastReadingDate;
    private TextView lastReadingValue;
    private TextView lastReadingCost;
    private TextView yearlySummaryTextView;
    private RecyclerView recentReadingsRecyclerView;
    private ReadingAdapter readingAdapter;
    private final List<Reading> readingList = new ArrayList<>();
    private Button viewAllReadingsButton;

    /**
     * Az Activity inicializálása. Ellenőrzi az értesítési jogosultságokat, hitelesítést, majd
     * beállítja a nézetet + a felhasználói felületet, kezeli az értesítésekből érkező intenteket,
     * és havi emlékeztetőt ütemez.
     *
     * @param savedInstanceState Az előzőleg elmentett állapot, vagy null, ha nincs.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkExactAlarmPermission();
        redirectIfNotAuthenticated();
        setContentView(R.layout.activity_home);
        initUI();
        handleNotificationIntent(getIntent());
        scheduleMonthlyReminder();
    }

    /**
     * Lekérdezi a legutóbbi bejelentést és megjeleníti azt.
     */
    private void fetchLastReading() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) return;

        String uid = user.getUid();

        db.collection("readings")
                .document(uid)
                .collection("user_readings")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        Reading reading = doc.toObject(Reading.class);
                        if (reading != null) {
                            reading.setId(doc.getId());
                            updateLastReadingUI(reading);
                        }
                    } else {
                        clearLastReadingUI();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                getString(R.string.reading_fetch_error, e.getMessage()),
                                Toast.LENGTH_LONG).show());
    }

    /**
     * Az Activity újrahívásakor frissíti a megjelenített adatokat.
     */
    @Override
    protected void onResume() {
        super.onResume();
        fetchLastReading();
        calculateYearlyConsumption();
        fetchRecentReadings();
    }

    /**
     * Összesíti az aktuális év bejelentései és kiszámolja a várható költséget.
     * Az eredményeket kiírja.
     */
    private void calculateYearlyConsumption() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) return;

        String uid = user.getUid();
        Date startOfYear = getStartOfCurrentYear();

        db.collection("readings")
                .document(uid)
                .collection("user_readings")
                .whereGreaterThanOrEqualTo("createdAt", startOfYear)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalConsumption = sumConsumption(queryDocumentSnapshots.getDocuments());
                    int totalCost = (int) (totalConsumption * 45);
                    updateYearlySummaryUI(totalConsumption, totalCost);
                })
                .addOnFailureListener(e ->
                        yearlySummaryTextView.setText(R.string.no_annual_data));
    }

    /**
     * Lekérdezi az utolsó három bejelentést, és animációval megjeleníti azokat.
     * Amennyiben van adat, az "Összes megtekintése" gombot is láthatóvá teszi.
     */
    private void fetchRecentReadings() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        FirebaseFirestore.getInstance()
                .collection("readings")
                .document(uid)
                .collection("user_readings")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(3)
                .get()
                .addOnSuccessListener(snapshots -> {
                    updateRecentReadingsList(snapshots.getDocuments());
                    animateRecentReadings();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(
                            this,
                            getString(R.string.no_readings_data),
                            Toast.LENGTH_SHORT).show();
                    viewAllReadingsButton.setVisibility(View.GONE);
                });
    }

    /**
     * Beállít egy havi emlékeztetőt minden hónap 1-jére 8:00 órára.
     */
    private void scheduleMonthlyReminder() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                123,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            }
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        }
    }

    /**
     * Ellenőrzi, hogy az Activity egy értesítésből lett-e megnyitva, és ha igen,
     * automatikusan átlépteti a felhasználót az új bejelentés képernyőre.
     */
    private void handleNotificationIntent(Intent intent) {
        if (intent.getBooleanExtra("from_notification", false)) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                Intent newIntent = new Intent(this, NewReadingActivity.class);
                startActivity(newIntent);
            }
        }
    }

    /**
     * Új intent fogadásakor meghívódik. Beállítja az új intentet, majd ellenőrzi, hogy értesítésből
     * érkezett-e, és ha igen, továbbnavigál a megfelelő képernyőre.
     *
     * @param intent az újonnan érkezett intent
     */
    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleNotificationIntent(intent);
    }

    /**
     * Ha Android 12+ esetén nincs engedélyezve az ébresztések és emlékeztetők, akkor ezt kérjük.
     */
    private void checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }
    }

    /**
     * Ha nincs bejelentkezve, akkor visszairányítjuk a főképernyőre
     */
    private void redirectIfNotAuthenticated() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    /**
     * Inicializálja az összes UI komponenst, a gombok eseménykezelőivel együtt.
     */
    private void initUI() {
        lastReadingDate = findViewById(R.id.lastReadingDate);
        lastReadingValue = findViewById(R.id.lastReadingValue);
        lastReadingCost = findViewById(R.id.lastReadingCost);
        yearlySummaryTextView = findViewById(R.id.yearlySummaryTextView);
        recentReadingsRecyclerView = findViewById(R.id.recentReadingsRecyclerView);
        recentReadingsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        String latestReadingId = readingList.isEmpty() ? null : readingList.get(0).getId();
        readingAdapter = new ReadingAdapter(this, readingList, latestReadingId);
        viewAllReadingsButton = findViewById(R.id.viewAllReadingsButton);
        viewAllReadingsButton.setVisibility(View.GONE);
        recentReadingsRecyclerView.setAdapter(readingAdapter);
        FloatingActionButton addReadingFab = findViewById(R.id.addReadingFab);
        addReadingFab.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, NewReadingActivity.class);
            startActivity(intent);
        });
        viewAllReadingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AllReadingsActivity.class);
            startActivity(intent);
        });
        readingAdapter.setOnDataChangedListener(this::refreshHomeData);
    }

    /**
     * Frissíti az utolsó bejelentéshez tartozó UI elemeket.
     */
    private void updateLastReadingUI(Reading reading) {
        Date createdDate = reading.getCreatedAt();

        if (createdDate != null) {
            String formattedCreatedDate =
                    new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            .format(createdDate);

            long daysBetween = calculateDaysBetweenDates(reading.getStartDate(), createdDate);

            lastReadingDate.setText(getString(R.string.reading_reported_on, formattedCreatedDate));
            lastReadingValue.setText(getString(R.string.reading_period_days, daysBetween));
        } else {
            lastReadingDate.setText(getString(R.string.reading_old_entry));
            lastReadingValue.setText(getString(R.string.reading_start_date, reading.getStartDate()));
        }

        int costEstimate = (int) (reading.getConsumption() * 45);
        lastReadingCost.setText(getString(R.string.reading_consumption_cost, reading.getConsumption(), costEstimate));
    }

    /**
     * Két dátum közötti napok számát számolja ki.
     *
     * @param startDateStr a kezdő dátum
     * @param endDate      a befejező dátum
     * @return a két dátum közti napok száma, vagy 0, ha a kezdő dátum nem értelmezhető
     */
    private long calculateDaysBetweenDates(String startDateStr, Date endDate) {
        try {
            Date startDate = new SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()).parse(startDateStr);
            if (startDate != null) {
                long diffMillis = endDate.getTime() - startDate.getTime();
                return TimeUnit.MILLISECONDS.toDays(diffMillis);
            }
        } catch (ParseException e) {
            Log.e("HomeActivity", "Nem sikerült a kezdődátumot értelmezni", e);
        }
        return 0;
    }

    /**
     * Alapállapotba állítja a legutóbbi bejelentés megjelenítését, ha nincs adat.
     */
    private void clearLastReadingUI() {
        lastReadingDate.setText(getString(R.string.reading_no_data));
        lastReadingValue.setText("");
        lastReadingCost.setText("");
    }

    /**
     * Visszaadja az aktuális év első napjának dátumát.
     *
     * @return Az aktuális év első napjának dátuma.
     */
    private Date getStartOfCurrentYear() {
        Calendar startOfYear = Calendar.getInstance();
        startOfYear.set(Calendar.MONTH, Calendar.JANUARY);
        startOfYear.set(Calendar.DAY_OF_MONTH, 1);
        startOfYear.set(Calendar.HOUR_OF_DAY, 0);
        startOfYear.set(Calendar.MINUTE, 0);
        startOfYear.set(Calendar.SECOND, 0);
        startOfYear.set(Calendar.MILLISECOND, 0);
        return startOfYear.getTime();
    }

    /**
     * Összeadja a bejelentésekben található fogyasztási értékeket.
     *
     * @param docs A Firestore adatok listája.
     * @return A fogyasztások összege kWh-ban.
     */
    private double sumConsumption(List<DocumentSnapshot> docs) {
        double total = 0;
        for (DocumentSnapshot doc : docs) {
            Reading r = doc.toObject(Reading.class);
            if (r != null) {
                total += r.getConsumption();
            }
        }
        return total;
    }

    /**
     * Frissíti a képernyőn az éves fogyasztás és költség értékét.
     *
     * @param kWh  A teljes fogyasztás kilowattórában.
     * @param cost A fogyasztás alapján becsült költség forintban.
     */
    private void updateYearlySummaryUI(double kWh, int cost) {
        yearlySummaryTextView.setText(getString(R.string.yearly_summary, kWh, cost));
    }

    /**
     * Frissíti a legutóbbi bejelentések listáját.
     * A meglévő lista tartalma törlődik és az adatok újra betöltésre kerülnek.
     *
     * @param docs A Firestore adatok listája.
     */
    @SuppressLint("NotifyDataSetChanged")
    private void updateRecentReadingsList(List<DocumentSnapshot> docs) {
        readingList.clear();
        for (DocumentSnapshot doc : docs) {
            Reading reading = doc.toObject(Reading.class);
            if (reading != null) {
                reading.setId(doc.getId());
                readingList.add(reading);
            }
        }
        readingAdapter.notifyDataSetChanged();
    }

    /**
     * Animációval jeleníti meg a legutóbbi bejelentések listáját.
     * Az "Összes megtekintése" gomb is animációval jelenik meg.
     */
    private void animateRecentReadings() {
        if (readingList.isEmpty()) {
            viewAllReadingsButton.setVisibility(View.GONE);
            return;
        }

        viewAllReadingsButton.setVisibility(View.INVISIBLE);

        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        LayoutAnimationController controller = new LayoutAnimationController(fadeIn);
        recentReadingsRecyclerView.setLayoutAnimation(controller);
        recentReadingsRecyclerView.scheduleLayoutAnimation();

        recentReadingsRecyclerView.postDelayed(() -> {
            viewAllReadingsButton.setVisibility(View.VISIBLE);
            Animation buttonAnim = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            viewAllReadingsButton.startAnimation(buttonAnim);
        }, 2500);
    }

    /**
     * Frissíti a főképernyőn megjelenített adatokat.
     */
    private void refreshHomeData() {
        fetchLastReading();
        calculateYearlyConsumption();
        fetchRecentReadings();
    }
}
