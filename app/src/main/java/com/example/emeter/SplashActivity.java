package com.example.emeter;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.media.MediaPlayer;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Lefuttatja az intró animációkat és lejátszik egy rövid hangot. Ezután automatikusan
 * továbbnavigál a bejelentkezési képernyőre.
 */
public class SplashActivity extends AppCompatActivity {

    private static final int STEP_DELAY = 500;
    private static final int SPLASH_DELAY = 2500;

    /**
     * Elindítja az animációkat és a kezdőhangot.
     *
     * @param savedInstanceState Az activity korábbi állapotáta, ha van.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.intro_sound);
        mediaPlayer.start();

        ImageView circle = findViewById(R.id.backgroundCircle);
        ImageView person = findViewById(R.id.person);
        ImageView lightning = findViewById(R.id.lightning);
        TextView appName = findViewById(R.id.appName);

        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation rotate = AnimationUtils.loadAnimation(this, R.anim.rotate_logo);

        person.setVisibility(View.INVISIBLE);
        lightning.setVisibility(View.INVISIBLE);
        appName.setVisibility(View.INVISIBLE);

        Handler handler = new Handler(Looper.getMainLooper());

        handler.postDelayed(() -> {
            person.setVisibility(View.VISIBLE);
            person.startAnimation(rotate);
        }, STEP_DELAY);

        handler.postDelayed(() -> {
            lightning.setVisibility(View.VISIBLE);
            lightning.startAnimation(fadeIn);
        }, STEP_DELAY * 3);

        handler.postDelayed(() -> {
            appName.setVisibility(View.VISIBLE);
            appName.startAnimation(fadeIn);
        }, STEP_DELAY * 4);

        handler.postDelayed(() -> {
            mediaPlayer.release();
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }, SPLASH_DELAY * 2);
    }
}
