package com.example.prakriti;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CropsDescription extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crops_description);

        ImageButton Home = findViewById(R.id.btn_home);
        ImageButton Crops = findViewById(R.id.btn_crops);
        ImageButton Camera = findViewById(R.id.btn_camera);
        ImageButton NPK = findViewById(R.id.btn_npk);
        ImageButton Profile = findViewById(R.id.btn_profile);
        TextView Home_text = findViewById(R.id.btn_home_text);
        TextView Crops_text = findViewById(R.id.btn_crops_text);
        TextView NPK_text = findViewById(R.id.btn_npk_text);
        TextView Profile_text = findViewById(R.id.btn_profile_text);

        Home.setOnClickListener(v -> {
            startActivity(new Intent(CropsDescription.this, HomeScreen.class));
            overridePendingTransition(0, 0);
        });

        Home_text.setOnClickListener(v -> {
            startActivity(new Intent(CropsDescription.this, HomeScreen.class));
            overridePendingTransition(0, 0);
        });

        Crops.setOnClickListener(v -> {
            startActivity(new Intent(CropsDescription.this, Crops.class));
            overridePendingTransition(0, 0);
        });

        Crops_text.setOnClickListener(v -> {
            startActivity(new Intent(CropsDescription.this, Crops.class));
            overridePendingTransition(0, 0);
        });

        Camera.setOnClickListener(v -> {
            startActivity(new Intent(CropsDescription.this, CameraDescription.class));
            overridePendingTransition(0, 0);
        });

        NPK.setOnClickListener(v -> {
            startActivity(new Intent(CropsDescription.this, NPKCalculator.class));
            overridePendingTransition(0, 0);
        });

        NPK_text.setOnClickListener(v -> {
            startActivity(new Intent(CropsDescription.this, NPKCalculator.class));
            overridePendingTransition(0, 0);
        });

        Profile.setOnClickListener(v -> {
            startActivity(new Intent(CropsDescription.this, ProfileScreen.class));
            overridePendingTransition(0, 0);
        });

        Profile_text.setOnClickListener(v -> {
            startActivity(new Intent(CropsDescription.this, ProfileScreen.class));
            overridePendingTransition(0, 0);
        });
    }

    // 🔴 Moved outside onCreate
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(CropsDescription.this, Crops.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }
}
