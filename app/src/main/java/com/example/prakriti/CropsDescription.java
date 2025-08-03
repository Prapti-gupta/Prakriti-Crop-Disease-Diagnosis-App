package com.example.prakriti;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CropsDescription extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crops_description);

        LinearLayout Home = findViewById(R.id.nav_home);
        LinearLayout Crops = findViewById(R.id.nav_crops);
        ImageButton Camera = findViewById(R.id.btn_camera);
        LinearLayout NPK = findViewById(R.id.nav_npk);
        LinearLayout Profile = findViewById(R.id.nav_profile);

        Home.setOnClickListener(v -> {
            startActivity(new Intent(CropsDescription.this, HomeScreen.class));
            overridePendingTransition(0, 0);
        });

        Crops.setOnClickListener(v -> {
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

        Profile.setOnClickListener(v -> {
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
