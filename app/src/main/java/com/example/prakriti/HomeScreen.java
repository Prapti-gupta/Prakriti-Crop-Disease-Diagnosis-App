package com.example.prakriti;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HomeScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        LinearLayout navHome = findViewById(R.id.nav_home);
        LinearLayout navCrops = findViewById(R.id.nav_crops);
        LinearLayout navNPK = findViewById(R.id.nav_npk);
        LinearLayout navProfile = findViewById(R.id.nav_profile);
        ImageButton btnCamera = findViewById(R.id.btn_camera);
        Button app_instructions = findViewById(R.id.app_instructions);
        ImageView profilePicture = findViewById(R.id.profile_picture);


        // Set profile picture based on gender from SharedPreferences
        setProfilePicture(profilePicture);

        app_instructions.setOnClickListener(v -> {
            Intent intent = new Intent(HomeScreen.this, HowToUseTheApp.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        // Set click listeners
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(HomeScreen.this, HomeScreen.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        navCrops.setOnClickListener(v -> {
            Intent intent = new Intent(HomeScreen.this, Crops.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        navNPK.setOnClickListener(v -> {
            Intent intent = new Intent(HomeScreen.this, NPKCalculator.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(HomeScreen.this, ProfileScreen.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        btnCamera.setOnClickListener(v -> {
            Intent intent = new Intent(HomeScreen.this, CameraActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

       profilePicture.setOnClickListener(v -> {
            Intent intent = new Intent(HomeScreen.this, ProfileScreen.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
    }

    private void setProfilePicture(ImageView profilePicture) {
        // Always use the same SharedPreferences file
        SharedPreferences sharedPreferences = getSharedPreferences("PrakritiLogin", MODE_PRIVATE);
        String gender = sharedPreferences.getString("gender", "male"); // Default male

        if ("female".equalsIgnoreCase(gender)) {
            profilePicture.setImageResource(R.drawable.female_farmer_green);
        } else {
            profilePicture.setImageResource(R.drawable.male_farmer_green);
        }
    }



    @Override

    protected void onResume() {
        super.onResume();

        // Profile picture update
        ImageView profilePicture = findViewById(R.id.profile_picture);
        if (profilePicture != null) {
            setProfilePicture(profilePicture);
        }

        // Username greeting
        TextView tvGreeting = findViewById(R.id.username);
        SharedPreferences loginPrefs = getSharedPreferences("PrakritiLogin", MODE_PRIVATE);
        String userName = loginPrefs.getString("userName", "Kisan ji");
        tvGreeting.setText(userName);

        // Crop count and last diagnosis date
        SharedPreferences prefs = getSharedPreferences("PrakritiData", MODE_PRIVATE);
        int cropCount = prefs.getInt("cropCount", 0);
        long lastTime = prefs.getLong("lastDiagnosisTime", 0);

        TextView tvCropCount = findViewById(R.id.tv_crop_count);
        TextView tvLastDate = findViewById(R.id.tv_last_diagnosis);

        tvCropCount.setText(String.valueOf(cropCount));


        if (lastTime > 0) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM yyyy");
            String formatted = sdf.format(new java.util.Date(lastTime));
            tvLastDate.setText("" + formatted);
        } else {
            tvLastDate.setText("No diagnosis yet");
        }
    }

}