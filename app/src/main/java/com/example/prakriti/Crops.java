package com.example.prakriti;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Crops extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crops);
        LinearLayout navHome = findViewById(R.id.nav_home);
        LinearLayout navCrops = findViewById(R.id.nav_crops);
        LinearLayout navNPK = findViewById(R.id.nav_npk);
        LinearLayout navProfile = findViewById(R.id.nav_profile);
        ImageButton btnCamera = findViewById(R.id.btn_camera);

        ImageButton Apple = findViewById(R.id.crop_desc);
        ImageButton Cotton = findViewById(R.id.crop_desc2);
        ImageButton Cherry = findViewById(R.id.crop_desc3);
        ImageButton Jute = findViewById(R.id.crop_desc4);
        ImageButton Coffee = findViewById(R.id.crop_desc5);
        ImageButton Corn = findViewById(R.id.crop_desc6);
        ImageButton Grapes = findViewById(R.id.crop_desc7);
        ImageButton Potato = findViewById(R.id.crop_desc8);
        ImageButton Rice = findViewById(R.id.crop_desc9);
        ImageButton Sugarcane = findViewById(R.id.crop_desc10);
        ImageButton Strawberry = findViewById(R.id.crop_desc11);
        ImageButton Wheat = findViewById(R.id.crop_desc12);


        View.OnClickListener cropClickListener = v -> {
            Intent intent = new Intent(Crops.this, CropsDescription.class);
            startActivity(intent);
            finish();
            overridePendingTransition(0, 0);
        };

        Wheat.setOnClickListener(cropClickListener);
        Strawberry.setOnClickListener(cropClickListener);
        Sugarcane.setOnClickListener(cropClickListener);
        Rice.setOnClickListener(cropClickListener);
        Potato.setOnClickListener(cropClickListener);
        Grapes.setOnClickListener(cropClickListener);
        Corn.setOnClickListener(cropClickListener);
        Coffee.setOnClickListener(cropClickListener);
        Jute.setOnClickListener(cropClickListener);
        Cherry.setOnClickListener(cropClickListener);
        Cotton.setOnClickListener(cropClickListener);
        Apple.setOnClickListener(cropClickListener);

        // Set click listeners
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(Crops.this, HomeScreen.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        navCrops.setOnClickListener(v -> {
            Intent intent = new Intent(Crops.this, Crops.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        navNPK.setOnClickListener(v -> {
            Intent intent = new Intent(Crops.this, NPKCalculator.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(Crops.this, ProfileScreen.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        btnCamera.setOnClickListener(v -> {
            Intent intent = new Intent(Crops.this, CameraActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

    }
}