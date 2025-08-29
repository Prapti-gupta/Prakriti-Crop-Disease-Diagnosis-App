package com.example.prakriti;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        initViews();
        populateFeatures();
    }

    private void initViews() {
        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
    }

    private void populateFeatures() {
        LinearLayout featuresContainer = findViewById(R.id.features_container);

        String[][] features = {
                {"Crop Disease Diagnosis", "Capture or upload a leaf photo and get instant offline disease identification", "ic_leaf"},
                {"Offline Support", "Works fully without internet, anytime and anywhere", "ic_offline"},
                {"Result Insights", "Get detailed analysis and useful tips for healthier crops", "ic_insights"},
                {"History Tracking", "Save and review past diagnoses for better crop management", "ic_history"},
                {"Farmer Friendly", "Simple, easy-to-use interface designed for farmers", "ic_farmer"}
        };

        // Add features with numbering
        for (int i = 0; i < features.length; i++) {
            String number = (i + 1) + "."; // numbering like 1. 2. 3.
            addFeatureItem(featuresContainer, number, features[i][0], features[i][1], features[i][2]);
        }
    }

    private void addFeatureItem(LinearLayout container, String number, String title, String description, String iconName) {
        LinearLayout featureItem = new LinearLayout(this);
        featureItem.setOrientation(LinearLayout.HORIZONTAL);
        featureItem.setPadding(0, dpToPx(8), 0, dpToPx(8));
        featureItem.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // ✅ Number (plain text only, no circle background)
        TextView numberText = new TextView(this);
        LinearLayout.LayoutParams numberParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        numberText.setLayoutParams(numberParams);
        numberText.setText(number);
        numberText.setTextSize(16);
        numberText.setTextColor(getColor(R.color.primary_green_dark));
        numberText.setTypeface(numberText.getTypeface(), android.graphics.Typeface.BOLD);
        numberText.setBackground(null);

        // ✅ Icon (reduced margin so it’s close to the number)
        ImageView icon = new ImageView(this);
        LinearLayout.LayoutParams iconSizeParams = new LinearLayout.LayoutParams(dpToPx(24), dpToPx(24));
        iconSizeParams.leftMargin = dpToPx(1);
        icon.setLayoutParams(iconSizeParams);
        int iconResId = getResources().getIdentifier(iconName, "drawable", getPackageName());
        if (iconResId != 0) {
            icon.setImageResource(iconResId);
        }

        // ✅ Text container (reduced left margin so text is closer to icon)
        LinearLayout textContainer = new LinearLayout(this);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
        );
        textParams.leftMargin = dpToPx(5);
        textContainer.setLayoutParams(textParams);
        textContainer.setOrientation(LinearLayout.VERTICAL);

        TextView titleText = new TextView(this);
        titleText.setText(title);
        titleText.setTextSize(16);
        titleText.setTextColor(getColor(R.color.primary_green_dark));
        titleText.setTypeface(titleText.getTypeface(), android.graphics.Typeface.BOLD);

        TextView descText = new TextView(this);
        descText.setText(description);
        descText.setTextSize(14);
        descText.setTextColor(getColor(R.color.darkgrey));
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        descParams.topMargin = dpToPx(4);
        descText.setLayoutParams(descParams);
        descText.setLineSpacing(0, 1.4f);

        textContainer.addView(titleText);
        textContainer.addView(descText);

        // Add all views together
        featureItem.addView(numberText);
        featureItem.addView(icon);
        featureItem.addView(textContainer);

        container.addView(featureItem);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
