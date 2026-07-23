package com.priyanshu.spend_buddy;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    ImageView logo;
    TextView appName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        logo = findViewById(R.id.logo);
        appName = findViewById(R.id.appName);

        // Animations
        Animation topAnim = AnimationUtils.loadAnimation(this, R.anim.top_anim);
        Animation bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_anim);

        logo.startAnimation(topAnim);
        appName.startAnimation(bottomAnim);

        //animation
        ImageView logo = findViewById(R.id.logo);

        Animation fade=AnimationUtils.loadAnimation(this, R.anim.fade_in);
        logo.startAnimation(fade);

// Fade + Scale animation
        logo.setScaleX(0.8f);
        logo.setScaleY(0.8f);
        logo.setAlpha(0f);

        logo.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(1200)
                .start();

        // Delay then move to LoginActivity
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
            finish();
        }, 2500); // 2.5 sec
    }
}