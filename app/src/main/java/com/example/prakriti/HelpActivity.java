package com.example.prakriti;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class HelpActivity extends AppCompatActivity {

    private LinearLayout faqContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        initViews();
        setupClickListeners();
        populateFAQs();
        populateSteps();
    }

    private void initViews() {
        faqContainer = findViewById(R.id.faq_container);
        ImageView backButton = findViewById(R.id.back_button);
        CardView contactSupportBtn = findViewById(R.id.contact_support_btn);

        backButton.setOnClickListener(v -> finish());
        contactSupportBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileScreen.class);
            startActivity(intent);
        });
    }

    private void setupClickListeners() {
        // Any additional setup can go here
    }

    private void setStepData(int stepNumber, String number, String title, String description, boolean isLast) {
        int stepId = getResources().getIdentifier("step" + stepNumber, "id", getPackageName());
        LinearLayout stepLayout = findViewById(stepId);

        if (stepLayout != null) {
            TextView stepNumberView = stepLayout.findViewById(R.id.step_number);
            TextView stepTitleView = stepLayout.findViewById(R.id.step_title);
            TextView stepDescriptionView = stepLayout.findViewById(R.id.step_description);
            android.view.View stepLine = stepLayout.findViewById(R.id.step_line);

            stepNumberView.setText(number);
            stepTitleView.setText(title);
            stepDescriptionView.setText(description);

            if (isLast) {
                stepLine.setVisibility(android.view.View.GONE);
                stepLayout.setPadding(stepLayout.getPaddingLeft(), stepLayout.getPaddingTop(),
                        stepLayout.getPaddingRight(), 0);
            }
        }
    }
    private void populateSteps() {
        LinearLayout stepsContainer = findViewById(R.id.steps_container);

        // Steps data
        String[][] steps = {
                {"1", "Set up your profile", "Begin your Prakriti journey by creating your profile in this offline crop diagnosis app."},
                {"2", "Capture or upload leaf photo", "Take a picture of the crop leaf using your camera or upload one from your gallery."},
                {"3", "Get instant analysis", "The inbuilt model processes the photo offline and detects possible diseases."},
                {"4", "View results & insights", "Receive accurate diagnosis along with useful tips for managing crop health."},
                {"5", "Check history", "Review past diagnoses to track and manage crop health over time."}
        };

        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < steps.length; i++) {
            View stepView = inflater.inflate(R.layout.step_item, stepsContainer, false);

            TextView stepNumberView = stepView.findViewById(R.id.step_number);
            TextView stepTitleView = stepView.findViewById(R.id.step_title);
            TextView stepDescriptionView = stepView.findViewById(R.id.step_description);
            View stepLine = stepView.findViewById(R.id.step_line);

            stepNumberView.setText(steps[i][0]);
            stepTitleView.setText(steps[i][1]);
            stepDescriptionView.setText(steps[i][2]);

            // Hide connector line for the last step
            if (i == steps.length - 1) {
                stepLine.setVisibility(View.GONE);
            }

            stepsContainer.addView(stepView);
        }
    }

    private void populateFAQs() {
        String[][] faqs = {
                {"How do I diagnose a crop disease?", "Simply capture or upload a photo of the crop leaf, and the app will analyze it instantly offline."},
                {"Do I need internet to use Prakriti?", "No! The app works fully offline, so you can use it anytime and anywhere without connectivity."},
                {"How accurate are the results?", "Prakriti uses an inbuilt trained model to provide highly accurate disease detection and insights."},
                {"Can I check previous diagnoses?", "Yes! Your past results are saved in history, so you can review them anytime for better crop management."},
                {"What should I do after diagnosis?", "The app provides useful tips and insights to help you take informed decisions for healthier crops."},
                {"Who can use Prakriti?", "It’s designed especially for farmers and anyone who wants a quick, simple, and reliable crop disease diagnosis tool."}
        };


        for (String[] faq : faqs) {
            View faqItem = getLayoutInflater().inflate(R.layout.faq_item, null);

            androidx.cardview.widget.CardView faqCard = faqItem.findViewById(R.id.faq_card);
            android.widget.TextView questionText = faqItem.findViewById(R.id.question_text);
            android.widget.TextView answerText = faqItem.findViewById(R.id.answer_text);
            android.widget.ImageView expandIcon = faqItem.findViewById(R.id.expand_icon);

            questionText.setText(faq[0]);
            answerText.setText(faq[1]);

            // Initially hide the answer
            answerText.setVisibility(View.GONE);

            faqCard.setOnClickListener(v -> {
                if (answerText.getVisibility() == View.GONE) {
                    answerText.setVisibility(View.VISIBLE);
                    expandIcon.setRotation(180);
                } else {
                    answerText.setVisibility(View.GONE);
                    expandIcon.setRotation(0);
                }
            });

            faqContainer.addView(faqItem);
        }
    }
}