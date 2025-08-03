package com.example.prakriti;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_screen);
        LinearLayout navHome = findViewById(R.id.nav_home);
        LinearLayout navCrops = findViewById(R.id.nav_crops);
        LinearLayout navNPK = findViewById(R.id.nav_npk);
        LinearLayout navProfile = findViewById(R.id.nav_profile);
        ImageButton btnCamera = findViewById(R.id.btn_camera);

        // Set click listeners
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileScreen.this, HomeScreen.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        navCrops.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileScreen.this, Crops.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        navNPK.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileScreen.this, NPKCalculator.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileScreen.this, ProfileScreen.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        btnCamera.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileScreen.this, CameraActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
    }

    }
