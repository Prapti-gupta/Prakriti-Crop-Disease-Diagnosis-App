package com.example.prakriti;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CameraDescription extends AppCompatActivity {

    private ImageView headerImage;
    private TextView titleText;
    private TextView tabDescription, tabTreatment;
    private ScrollView scrollViewDescription, scrollViewTreatment;
    private View underlineIndicator;

    private TextView descriptionTextView, treatmentTextView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_description); // Use your merged layout here

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

        // Handle intent data
        Intent intent = getIntent();
        if (intent != null) {
            String prediction = intent.getStringExtra("prediction");
            String imageUriString = intent.getStringExtra("imageUri");
            String photoPath = intent.getStringExtra("photo_path");

            if (prediction != null) {
                titleText.setText(prediction);
                descriptionTextView.setText("Diagnosis: " + prediction);
                treatmentTextView.setText("Treatment instructions for: " + prediction);
            }

            if (imageUriString != null) {
                Uri imageUri = Uri.parse(imageUriString);
                headerImage.setImageURI(imageUri);
            } else if (photoPath != null) {
                Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
                if (bitmap != null) {
                    headerImage.setImageBitmap(bitmap);
                    descriptionTextView.setText("Photo loaded from: " + photoPath + "\n\nDiagnosis: " + prediction);
                } else {
                    descriptionTextView.setText("Failed to load photo from file path.");
                }
            } else {
                descriptionTextView.setText("No image provided.");
            }
        }

        // Back button functionality
        /*
          ImageButton backButton = findViewById(R.id.backButton);
          backButton.setOnClickListener(v -> finish());
        */
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
}
