package com.example.prakriti;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Crops extends AppCompatActivity {

    private CropDataManager cropDataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crops);

        // Initialize crop data manager
        cropDataManager = new CropDataManager(this);

        // Initialize navigation views
        LinearLayout navHome = findViewById(R.id.nav_home);
        LinearLayout navCrops = findViewById(R.id.nav_crops);
        LinearLayout navNPK = findViewById(R.id.nav_npk);
        LinearLayout navProfile = findViewById(R.id.nav_profile);
        ImageButton btnCamera = findViewById(R.id.btn_camera);

        // Initialize crop buttons
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

        // Set navigation click listeners
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(Crops.this, HomeScreen.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        navCrops.setOnClickListener(v -> {
            // Already on crops screen, maybe refresh or do nothing
            Toast.makeText(this, "Already on Crops screen", Toast.LENGTH_SHORT).show();
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

        // Enhanced crop click listener with database validation
        View.OnClickListener cropClickListener = v -> {
            String cropName = getCropNameFromButton(v.getId());

            if (cropName != null) {
                // Check if crop exists in database before navigating
                if (cropDataManager.cropExists(cropName)) {
                    Intent intent = new Intent(Crops.this, CropsDescription.class);
                    intent.putExtra("cropName", cropName);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                } else {
                    Toast.makeText(this, "Information for " + cropName + " is not available",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Unknown crop selected", Toast.LENGTH_SHORT).show();
            }
        };

        // Attach listeners to all crop buttons
        Apple.setOnClickListener(cropClickListener);
        Cotton.setOnClickListener(cropClickListener);
        Cherry.setOnClickListener(cropClickListener);
        Jute.setOnClickListener(cropClickListener);
        Coffee.setOnClickListener(cropClickListener);
        Corn.setOnClickListener(cropClickListener);
        Grapes.setOnClickListener(cropClickListener);
        Potato.setOnClickListener(cropClickListener);
        Rice.setOnClickListener(cropClickListener);
        Sugarcane.setOnClickListener(cropClickListener);
        Strawberry.setOnClickListener(cropClickListener);
        Wheat.setOnClickListener(cropClickListener);
    }

    /**
     * Helper method to get crop name from button ID
     */
    private String getCropNameFromButton(int buttonId) {
        if (buttonId == R.id.crop_desc) {
            return "Apple";
        } else if (buttonId == R.id.crop_desc2) {
            return "Cotton";
        } else if (buttonId == R.id.crop_desc3) {
            return "Cherry";
        } else if (buttonId == R.id.crop_desc4) {
            return "Jute";
        } else if (buttonId == R.id.crop_desc5) {
            return "Coffee";
        } else if (buttonId == R.id.crop_desc6) {
            return "Corn";
        } else if (buttonId == R.id.crop_desc7) {
            return "Grape"; // Note: Database uses "Grape" not "Grapes"
        } else if (buttonId == R.id.crop_desc8) {
            return "Potato";
        } else if (buttonId == R.id.crop_desc9) {
            return "Rice";
        } else if (buttonId == R.id.crop_desc10) {
            return "Sugarcane";
        } else if (buttonId == R.id.crop_desc11) {
            return "Strawberry";
        } else if (buttonId == R.id.crop_desc12) {
            return "Wheat";
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close the crop data manager
        if (cropDataManager != null) {
            cropDataManager.close();
        }
    }

    public void onBackPressed() {
        // Finish all activities and exit the app
        finishAffinity();
        System.exit(0);  // Optional, ensures process is killed
    }
}

