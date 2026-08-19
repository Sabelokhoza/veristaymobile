package com.veristay.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

import com.veristay.app.R;
import com.veristay.app.SessionManager;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SessionManager session = new SessionManager(this);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent;
            if (session.isLoggedIn()) {
                intent = new Intent(this, com.veristay.app.MainActivity.class);
            } else {
                intent = new Intent(this, com.veristay.app.LoginActivity.class);
            }
            startActivity(intent);
            finish();
        }, 1800);
    }
}