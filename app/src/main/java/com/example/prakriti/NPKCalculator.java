package com.example.prakriti;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.text.DecimalFormat;

public class NPKCalculator extends AppCompatActivity {

    // NPK Calculator UI elements
    private EditText etFertilizerAmount;
    private EditText etNitrogenPercent;
    private EditText etPhosphorusPercent;
    private EditText etPotassiumPercent;
    private Button btnCalculate;
    private Button btnClear;
    private CardView cardResults;
    private TextView tvNitrogenResult;
    private TextView tvPhosphorusResult;
    private TextView tvPotassiumResult;
    private TextView tvTotalResult;

    // Decimal formatter for results
    private DecimalFormat decimalFormat = new DecimalFormat("0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_npkcalculator);

        // Initialize NPK Calculator views
        initializeViews();

        // Set up NPK Calculator listeners
        setupCalculatorListeners();

        // Navigation setup (your existing code)
        LinearLayout navHome = findViewById(R.id.nav_home);
        LinearLayout navCrops = findViewById(R.id.nav_crops);
        LinearLayout navNPK = findViewById(R.id.nav_npk);
        LinearLayout navProfile = findViewById(R.id.nav_profile);
        ImageButton btnCamera = findViewById(R.id.btn_camera);

        // Set click listeners (your existing code)
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(NPKCalculator.this, HomeScreen.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        navCrops.setOnClickListener(v -> {
            Intent intent = new Intent(NPKCalculator.this, Crops.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        navNPK.setOnClickListener(v -> {
            Intent intent = new Intent(NPKCalculator.this, NPKCalculator.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(NPKCalculator.this, ProfileScreen.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        btnCamera.setOnClickListener(v -> {
            Intent intent = new Intent(NPKCalculator.this, CameraActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
    }

    /**
     * Initialize all NPK Calculator UI elements
     */
    private void initializeViews() {
        // Input fields
        etFertilizerAmount = findViewById(R.id.etFertilizerAmount);
        etNitrogenPercent = findViewById(R.id.etNitrogenPercent);
        etPhosphorusPercent = findViewById(R.id.etPhosphorusPercent);
        etPotassiumPercent = findViewById(R.id.etPotassiumPercent);

        // Buttons
        btnCalculate = findViewById(R.id.btnCalculate);
        btnClear = findViewById(R.id.btnClear);

        // Results section
        cardResults = findViewById(R.id.cardResults);
        tvNitrogenResult = findViewById(R.id.tvNitrogenResult);
        tvPhosphorusResult = findViewById(R.id.tvPhosphorusResult);
        tvPotassiumResult = findViewById(R.id.tvPotassiumResult);
        tvTotalResult = findViewById(R.id.tvTotalResult);
    }

    /**
     * Set up click listeners for calculator buttons
     */
    private void setupCalculatorListeners() {
        btnCalculate.setOnClickListener(v -> calculateNPK());
        btnClear.setOnClickListener(v -> clearAllFields());
    }

    /**
     * Main calculation method for NPK values
     */
    private void calculateNPK() {
        try {
            // Get input values
            String amountStr = etFertilizerAmount.getText().toString().trim();
            String nitrogenStr = etNitrogenPercent.getText().toString().trim();
            String phosphorusStr = etPhosphorusPercent.getText().toString().trim();
            String potassiumStr = etPotassiumPercent.getText().toString().trim();

            // Validate inputs
            if (!validateInputs(amountStr, nitrogenStr, phosphorusStr, potassiumStr)) {
                return;
            }

            // Parse values
            double fertilizerAmount = Double.parseDouble(amountStr);
            double nitrogenPercent = Double.parseDouble(nitrogenStr);
            double phosphorusPercent = Double.parseDouble(phosphorusStr);
            double potassiumPercent = Double.parseDouble(potassiumStr);

            // Additional validation
            if (!validatePercentages(nitrogenPercent, phosphorusPercent, potassiumPercent)) {
                return;
            }

            if (fertilizerAmount <= 0) {
                showToast("Please enter a valid fertilizer amount greater than 0");
                return;
            }

            // Calculate NPK amounts
            double nitrogenAmount = (fertilizerAmount * nitrogenPercent) / 100;
            double phosphorusAmount = (fertilizerAmount * phosphorusPercent) / 100;
            double potassiumAmount = (fertilizerAmount * potassiumPercent) / 100;
            double totalNPK = nitrogenAmount + phosphorusAmount + potassiumAmount;

            // Display results
            displayResults(nitrogenAmount, phosphorusAmount, potassiumAmount, totalNPK);

        } catch (NumberFormatException e) {
            showToast("Please enter valid numeric values");
        } catch (Exception e) {
            showToast("An error occurred during calculation");
        }
    }

    /**
     * Validate input fields
     */
    private boolean validateInputs(String amount, String nitrogen, String phosphorus, String potassium) {
        if (amount.isEmpty()) {
            showToast("Please enter fertilizer amount");
            etFertilizerAmount.requestFocus();
            return false;
        }

        if (nitrogen.isEmpty()) {
            showToast("Please enter nitrogen percentage");
            etNitrogenPercent.requestFocus();
            return false;
        }

        if (phosphorus.isEmpty()) {
            showToast("Please enter phosphorus percentage");
            etPhosphorusPercent.requestFocus();
            return false;
        }

        if (potassium.isEmpty()) {
            showToast("Please enter potassium percentage");
            etPotassiumPercent.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Validate percentage values
     */
    private boolean validatePercentages(double nitrogen, double phosphorus, double potassium) {
        if (nitrogen < 0 || nitrogen > 100) {
            showToast("Nitrogen percentage must be between 0 and 100");
            etNitrogenPercent.requestFocus();
            return false;
        }

        if (phosphorus < 0 || phosphorus > 100) {
            showToast("Phosphorus percentage must be between 0 and 100");
            etPhosphorusPercent.requestFocus();
            return false;
        }

        if (potassium < 0 || potassium > 100) {
            showToast("Potassium percentage must be between 0 and 100");
            etPotassiumPercent.requestFocus();
            return false;
        }

        double totalPercentage = nitrogen + phosphorus + potassium;
        if (totalPercentage > 100) {
            showToast("Total NPK percentage cannot exceed 100%");
            return false;
        }

        return true;
    }

    /**
     * Display calculation results
     */
    private void displayResults(double nitrogen, double phosphorus, double potassium, double total) {
        tvNitrogenResult.setText(decimalFormat.format(nitrogen) + " kg");
        tvPhosphorusResult.setText(decimalFormat.format(phosphorus) + " kg");
        tvPotassiumResult.setText(decimalFormat.format(potassium) + " kg");
        tvTotalResult.setText(decimalFormat.format(total) + " kg");

        // Show results card with animation
        cardResults.setVisibility(View.VISIBLE);
        cardResults.setAlpha(0f);
        cardResults.animate()
                .alpha(1f)
                .setDuration(300)
                .start();

        showToast("NPK calculation completed successfully!");
    }

    /**
     * Clear all input fields and hide results
     */
    private void clearAllFields() {
        // Clear input fields
        etFertilizerAmount.setText("");
        etNitrogenPercent.setText("");
        etPhosphorusPercent.setText("");
        etPotassiumPercent.setText("");

        // Reset results
        tvNitrogenResult.setText("0.0 kg");
        tvPhosphorusResult.setText("0.0 kg");
        tvPotassiumResult.setText("0.0 kg");
        tvTotalResult.setText("0.0 kg");

        // Hide results card
        cardResults.setVisibility(View.GONE);

        // Clear focus and move to first field
        etFertilizerAmount.requestFocus();

        showToast("All fields cleared");
    }

    /**
     * Show toast message
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}