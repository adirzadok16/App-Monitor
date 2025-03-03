package com.example.appmonitor;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.example.appmonitor.Utilities.SharedPreferencesManager;


public class SplashActivity extends AppCompatActivity {
    private LottieAnimationView lottie;
    private boolean isLoggedIn = false;
    private String username = "", password = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        lottie = findViewById(R.id.splash_LOTTIE_lottie);

        // Initialize SharedPreferences
        SharedPreferencesManager.init(this);
        isLoggedIn = SharedPreferencesManager.getInstance().getBooleanOrLog("isLoggedIn", false);

        if (isLoggedIn) {
            username = SharedPreferencesManager.getInstance().getString("username", "");
            password = SharedPreferencesManager.getInstance().getString("password", "");
        }

        startAnimation(lottie);

    }

    private void startAnimation(LottieAnimationView lottieAnimationView) {
        lottieAnimationView.playAnimation();
        lottieAnimationView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                Log.d("SplashActivity", "Animation started");
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Log.d("SplashActivity", "Animation ended");
                if (isLoggedIn) {
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    intent.putExtra("Username", username);
                    startActivity(intent);
                    finish();
                } else {
                    transactToLoginActivity();
                }

            }

            @Override
            public void onAnimationCancel(Animator animation) {
                Log.d("SplashActivity", "Animation canceled");
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                Log.d("SplashActivity", "Animation repeated");
            }
        });
    }

    private void transactToLoginActivity() {
        startActivity(new Intent(this, LoginScreen.class));
        finish();
    }



}