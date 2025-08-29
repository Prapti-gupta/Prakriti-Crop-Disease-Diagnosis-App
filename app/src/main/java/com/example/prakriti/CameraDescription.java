package com.example.prakriti;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CameraDescription extends AppCompatActivity {

    private static final String TAG = "CameraDescription";

    private ImageView headerImage;
    private TextView titleText;
    private TextView tabDescription, tabTreatment;
    private ScrollView scrollViewDescription, scrollViewTreatment;
    private View underlineIndicator;

    private TextView descriptionTextView, treatmentTextView;
    private DatabaseHelper dbHelper;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_description);

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Initialize views
        tabDescription = findViewById(R.id.tabDescription);
        tabTreatment = findViewById(R.id.tabTreatment);
        scrollViewDescription = findViewById(R.id.scrollViewDescription);
        scrollViewTreatment = findViewById(R.id.scrollViewTreatment);
        underlineIndicator = findViewById(R.id.underlineIndicator);
        headerImage = findViewById(R.id.headerImage);
        titleText = findViewById(R.id.titleText);
        descriptionTextView = findViewById(R.id.descriptionText);
        treatmentTextView = findViewById(R.id.treatmentText);

        // Handle incoming intent
        Intent intent = getIntent();
        if (intent != null) {
            String prediction = intent.getStringExtra("prediction");
            String photoPath = intent.getStringExtra("photo_path");
            float confidence = intent.getFloatExtra("confidence", 0.0f);

            Log.d(TAG, "Received prediction: " + prediction + " with confidence: " + confidence);

            if (prediction != null) {
                loadDiseaseData(prediction, confidence);
            } else {
                // Fallback
                titleText.setText("No Prediction");
                descriptionTextView.setText("No disease prediction available.");
                treatmentTextView.setText("Please consult agricultural experts.");
            }

            // Load the captured image
            if (photoPath != null) {
                loadImageFromPath(photoPath);
            } else {
                // Use a default placeholder image
                headerImage.setImageResource(R.drawable.apple_leaf);
            }
        }

        // Set underline indicator width after layout
        tabDescription.post(() -> {
            int tabWidth = tabDescription.getWidth();
            underlineIndicator.getLayoutParams().width = tabWidth;
            underlineIndicator.requestLayout();
        });

        // Tab switching
        tabDescription.setOnClickListener(v -> switchTab(true));
        tabTreatment.setOnClickListener(v -> switchTab(false));

        switchTab(true); // Show description tab by default
    }

    private void loadDiseaseData(String prediction, float confidence) {
        // Convert CropClassifier prediction format to database format
        String normalizedPrediction = normalizePredictionFormat(prediction);

        Log.d(TAG, "Normalized prediction: " + normalizedPrediction);

        // Get disease information from database
        DatabaseHelper.CropInfo diseaseInfo = dbHelper.getDiseaseInfo(normalizedPrediction);

        if (diseaseInfo != null) {
            // Format the title to show crop and condition with confidence
            String title = diseaseInfo.cropName + " - " + formatConditionName(diseaseInfo.condition);
            if (confidence > 0) {
                title += String.format(" (%.1f%% confidence)", confidence);
            }
            titleText.setText(title);

            // Set description and treatment
            String description = diseaseInfo.description;
            if (confidence > 0) {
                description += "\n\nModel Confidence: " + String.format("%.1f%%", confidence);
                if (confidence < 70) {
                    description += "\n\nNote: Low confidence prediction. Please verify with an expert.";
                }
            }
            descriptionTextView.setText(description);
            treatmentTextView.setText(diseaseInfo.treatment);
        } else {
            // Try alternative matching strategies
            diseaseInfo = tryAlternativeMatching(prediction);

            if (diseaseInfo != null) {
                String title = diseaseInfo.cropName + " - " + formatConditionName(diseaseInfo.condition);
                if (confidence > 0) {
                    title += String.format(" (%.1f%% confidence)", confidence);
                }
                titleText.setText(title);
                descriptionTextView.setText(diseaseInfo.description + "\n\nNote: Matched using alternative search.");
                treatmentTextView.setText(diseaseInfo.treatment);
            } else {
                // Fallback if disease not found in database
                handleUnknownPrediction(prediction, confidence);
            }
        }
    }

    private String normalizePredictionFormat(String prediction) {
        if (prediction == null) return "";

        // Handle CropClassifier prediction format: "Tomato__Early_blight" -> "Early_blight"
        // Extract condition part after the crop name
        String normalized = prediction;

        // Split by double underscore first (CropClassifier format)
        if (prediction.contains("__")) {
            String[] parts = prediction.split("__");
            if (parts.length > 1) {
                normalized = parts[1]; // Take the condition part
            }
        }
        // Handle single underscore format: "Apple_Black_rot" -> "Black_rot"
        else if (prediction.contains("_")) {
            // Common crop prefixes to remove
            String[] cropPrefixes = {"Tomato_", "Apple_", "Grape_", "Cherry_", "Coffee_",
                    "Corn_", "Cotton_", "Jute_", "Potato_", "Rice_",
                    "Strawberry_", "Sugarcane_", "Wheat_"};

            for (String prefix : cropPrefixes) {
                if (prediction.startsWith(prefix)) {
                    normalized = prediction.substring(prefix.length());
                    break;
                }
            }
        }

        Log.d(TAG, "Prediction format conversion: " + prediction + " -> " + normalized);
        return normalized;
    }

    private DatabaseHelper.CropInfo tryAlternativeMatching(String prediction) {
        // Try searching with original prediction
        DatabaseHelper.CropInfo info = searchInDatabase(prediction);
        if (info != null) return info;

        // Try with spaces instead of underscores
        info = searchInDatabase(prediction.replace("_", " "));
        if (info != null) return info;

        // Try extracting keywords and searching
        String[] words = prediction.split("[_\\s]+");
        for (String word : words) {
            if (word.length() > 3) { // Skip short words
                info = searchInDatabase(word);
                if (info != null) return info;
            }
        }

        return null;
    }

    private DatabaseHelper.CropInfo searchInDatabase(String searchTerm) {
        // Use a simple cursor query to search
        return dbHelper.getDiseaseInfo(searchTerm);
    }

    private void handleUnknownPrediction(String prediction, float confidence) {
        titleText.setText("Unrecognized Condition");

        String description = "The detected condition '" + prediction + "' is not recognized in our database.\n\n";

        if (confidence > 0) {
            description += "Model Confidence: " + String.format("%.1f%%\n\n", confidence);

            if (confidence < 50) {
                description += "The model has low confidence in this prediction. ";
            }
        }

        description += "This might be:\n" +
                "• A new or rare condition not in our database\n" +
                "• A healthy plant with unusual lighting/angle\n" +
                "• An environmental factor affecting the image\n\n" +
                "Please consider taking another photo with better lighting and clarity.";

        descriptionTextView.setText(description);

        String treatment = "Recommendations:\n\n" +
                "1. Consult with local agricultural experts or extension services\n" +
                "2. Take additional photos from different angles\n" +
                "3. Check for common symptoms: discoloration, spots, wilting\n" +
                "4. Monitor the plant over several days for changes\n" +
                "5. Consider soil and environmental conditions";

        treatmentTextView.setText(treatment);
    }

    private String formatConditionName(String condition) {
        // Convert database condition names to readable format
        return condition.replace("_", " ")
                .replace("(", " (")
                .trim();
    }

    private void loadImageFromPath(String photoPath) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
            if (bitmap != null) {
                headerImage.setImageBitmap(bitmap);
            } else {
                // If bitmap is null, use default image
                headerImage.setImageResource(R.drawable.apple_leaf);
            }
        } catch (Exception e) {
            // Handle any exceptions during image loading
            Log.e(TAG, "Error loading image: " + e.getMessage());
            headerImage.setImageResource(R.drawable.apple_leaf);
        }
    }

    private void switchTab(boolean showDescription) {
        if (showDescription) {
            scrollViewDescription.setVisibility(View.VISIBLE);
            scrollViewTreatment.setVisibility(View.GONE);
            tabDescription.setTextColor(getResources().getColor(android.R.color.black));
            tabTreatment.setTextColor(getResources().getColor(android.R.color.darker_gray));
            underlineIndicator.animate().translationX(0).setDuration(200).start();
        } else {
            scrollViewDescription.setVisibility(View.GONE);
            scrollViewTreatment.setVisibility(View.VISIBLE);
            tabDescription.setTextColor(getResources().getColor(android.R.color.darker_gray));
            tabTreatment.setTextColor(getResources().getColor(android.R.color.black));
            underlineIndicator.animate().translationX(tabDescription.getWidth()).setDuration(200).start();
        }
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