package com.example.prakriti;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CropsDescription extends AppCompatActivity {

    TextView cropTitle, descriptionText, conditionText, treatmentsText;
    ImageView diseaseImage;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crops_description);

        // Initialize Views
        cropTitle = findViewById(R.id.cropTitleText);
        descriptionText = findViewById(R.id.descriptionText);
        conditionText = findViewById(R.id.commonDiseaseText);
        treatmentsText = findViewById(R.id.treatmentsText);
        diseaseImage = findViewById(R.id.diseaseImageView);

        dbHelper = new DatabaseHelper(this);

        // Get crop name from intent
        String cropName = getIntent().getStringExtra("cropName");

        if (cropName != null) {
            loadCropData(cropName);
        } else {
            // Fallback if no crop name provided
            cropTitle.setText("No Crop Selected");
            descriptionText.setText("Please select a crop from the previous screen.");
            conditionText.setText("Healthy");
            treatmentsText.setText("No treatment information available.");
        }

        setupBottomNavigation();
    }

    private void loadCropData(String cropName) {
        // Get healthy crop information from database using correct method
        DatabaseHelper.CropInfo cropInfo = dbHelper.getHealthyCropInfo(cropName);

        if (cropInfo != null) {
            cropTitle.setText(cropInfo.cropName);
            descriptionText.setText(cropInfo.description);
            conditionText.setText(cropInfo.condition);
            treatmentsText.setText(cropInfo.treatment);

            // Set crop image based on crop name
            setCropImage(cropName.toLowerCase());
        } else {
            // Fallback if crop not found in database
            cropTitle.setText(cropName);
            descriptionText.setText("Information for " + cropName + " is not available in the database.");
            conditionText.setText("Unknown");
            treatmentsText.setText("Please consult agricultural experts for proper care instructions.");

            // Try to set image even if data not found
            setCropImage(cropName.toLowerCase());
        }
    }

    private void setCropImage(String cropName) {
        // Set appropriate image based on crop name
        int imageResourceId;

        switch (cropName.toLowerCase()) {
            case "apple":
                imageResourceId = R.drawable.apple_leaf;
                break;
            case "cherry":
                imageResourceId = R.drawable.cherry_leaf;
                break;
            case "coffee":
                imageResourceId = R.drawable.coffee_leaf;
                break;
            case "corn":
                imageResourceId = R.drawable.corn_leaf;
                break;
            case "cotton":
                imageResourceId = R.drawable.cotton_leaf;
                break;
            case "grape":
            case "grapes":
                imageResourceId = R.drawable.grapes_leaf;
                break;
            case "jute":
                imageResourceId = R.drawable.jute_leaf;
                break;
            case "potato":
                imageResourceId = R.drawable.potato_leaf;
                break;
            case "rice":
                imageResourceId = R.drawable.rice_leaf;
                break;
            case "strawberry":
                imageResourceId = R.drawable.strawberry_leaf;
                break;
            case "sugarcane":
                imageResourceId = R.drawable.sugarcane_leaf;
                break;
            case "wheat":
                imageResourceId = R.drawable.wheat_leaf;
                break;
            default:
                imageResourceId = R.drawable.potato_leaf; // Default fallback
                break;
        }

        try {
            diseaseImage.setImageResource(imageResourceId);
        } catch (Exception e) {
            // If image resource not found, use fallback
            diseaseImage.setImageResource(R.drawable.potato_leaf);
        }
    }

    private void setupBottomNavigation() {
        // Bottom navigation setup
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
            startActivity(new Intent(CropsDescription.this, CameraActivity.class));
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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(CropsDescription.this, Crops.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close database helper if needed
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}