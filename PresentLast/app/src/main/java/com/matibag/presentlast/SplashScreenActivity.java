package com.matibag.presentlast;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.Nullable;

import com.matibag.presentlast.api.AuthManager;
import com.matibag.presentlast.ui.HomeActivity;
import com.matibag.presentlast.ui.LoginActivity;

public class SplashScreenActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Reference the logo container for animation
        View logoContainer = findViewById(R.id.logo_container);

        // 1. Simple Fade-In Animation (Modern touch)
        logoContainer.animate()
                .alpha(1f)
                .setDuration(1200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // 2. Navigation Logic after 2.5 seconds
        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            AuthManager auth = AuthManager.getInstance(this);
            Intent nextActivity;

            if (auth.isLoggedIn()) {
                // User is already logged in, go to Dashboard
                nextActivity = new Intent(SplashScreenActivity.this, HomeActivity.class);
            } else {
                // No session found, go to Login
                nextActivity = new Intent(SplashScreenActivity.this, LoginActivity.class);
            }

            startActivity(nextActivity);

            // Apply a smooth cross-fade transition between activities
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

            finish();

        }, 2500); // Reduced to 2.5s for a snappier feel
    }
}