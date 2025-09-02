package com.example.prakriti;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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

    // NPK Planning Calculator UI elements
    private EditText etTargetNitrogen;
    private EditText etTargetPhosphorus;
    private EditText etTargetPotassium;
    private Button btnCalculate;
    private Button btnClear;
    private CardView cardResults;
    private TextView tvUreaResult;
    private TextView tvDapResult;
    private TextView tvMopResult;
    private TextView tvTotalFertilizerResult;

    // Decimal formatter for results
    private DecimalFormat decimalFormat = new DecimalFormat("0.00");

    // Fertilizer nutrient percentages (standard compositions)
    private static final double UREA_N_PERCENT = 46.0;          // Urea: 46% N
    private static final double DAP_N_PERCENT = 18.0;           // DAP: 18% N, 46% P2O5
    private static final double DAP_P_PERCENT = 46.0;
    private static final double MOP_K_PERCENT = 60.0;           // MOP: 60% K2O

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_npkcalculator);

        // Initialize NPK Planning Calculator views
        initializeViews();

        // Set up NPK Planning Calculator listeners
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
     * Initialize all NPK Planning Calculator UI elements
     */
    private void initializeViews() {
        // Target input fields
        etTargetNitrogen = findViewById(R.id.etFertilizerAmount);  // Reusing as target N
        etTargetPhosphorus = findViewById(R.id.etNitrogenPercent);  // Reusing as target P
        etTargetPotassium = findViewById(R.id.etPhosphorusPercent);  // Reusing as target K

        // Note: We're not using etPotassiumPercent in this version, but keeping it for layout consistency

        // Buttons
        btnCalculate = findViewById(R.id.btnCalculate);
        btnClear = findViewById(R.id.btnClear);

        // Results section
        cardResults = findViewById(R.id.cardResults);
        tvUreaResult = findViewById(R.id.tvNitrogenResult);     // Reusing for Urea amount
        tvDapResult = findViewById(R.id.tvPhosphorusResult);    // Reusing for DAP amount
        tvMopResult = findViewById(R.id.tvPotassiumResult);     // Reusing for MOP amount
        tvTotalFertilizerResult = findViewById(R.id.tvTotalResult); // Total fertilizer weight
    }

    /**
     * Set up click listeners for calculator buttons
     */
    private void setupCalculatorListeners() {
        btnCalculate.setOnClickListener(v -> calculateFertilizerNeeds());
        btnClear.setOnClickListener(v -> clearAllFields());
    }

    /**
     * Main calculation method for fertilizer planning
     * Uses the formula: Fertilizer needed = (Nutrient required / Nutrient %) × 100
     */
    private void calculateFertilizerNeeds() {
        try {
            // Get target nutrient values
            String targetNStr = etTargetNitrogen.getText().toString().trim();
            String targetPStr = etTargetPhosphorus.getText().toString().trim();
            String targetKStr = etTargetPotassium.getText().toString().trim();

            // Validate inputs
            if (!validateInputs(targetNStr, targetPStr, targetKStr)) {
                return;
            }

            // Parse values
            double targetNitrogen = Double.parseDouble(targetNStr);
            double targetPhosphorus = Double.parseDouble(targetPStr);
            double targetPotassium = Double.parseDouble(targetKStr);

            // Additional validation
            if (!validateTargetValues(targetNitrogen, targetPhosphorus, targetPotassium)) {
                return;
            }

            // Calculate fertilizer requirements using the planning formula
            double ureaNeeded = calculateFertilizerAmount(targetNitrogen, UREA_N_PERCENT);
            double dapNeeded = calculateFertilizerAmount(targetPhosphorus, DAP_P_PERCENT);
            double mopNeeded = calculateFertilizerAmount(targetPotassium, MOP_K_PERCENT);

            // Calculate total fertilizer weight
            double totalFertilizer = ureaNeeded + dapNeeded + mopNeeded;

            // Check for nutrient overlaps (DAP also provides nitrogen)
            double dapNitrogenContribution = (dapNeeded * DAP_N_PERCENT) / 100;
            double adjustedUreaNeeded = Math.max(0, ureaNeeded - (dapNitrogenContribution * (UREA_N_PERCENT / 100)));

            // Recalculate total with adjusted urea
            double adjustedTotal = adjustedUreaNeeded + dapNeeded + mopNeeded;

            // Display results
            displayResults(adjustedUreaNeeded, dapNeeded, mopNeeded, adjustedTotal, dapNitrogenContribution);

        } catch (NumberFormatException e) {
            showToast("Please enter valid numeric values");
        } catch (Exception e) {
            showToast("An error occurred during calculation");
        }
    }

    /**
     * Calculate fertilizer amount using the planning formula
     * Formula: Fertilizer needed = (Nutrient required / Nutrient %) × 100
     */
    private double calculateFertilizerAmount(double nutrientRequired, double nutrientPercent) {
        return (nutrientRequired / nutrientPercent) * 100;
    }

    /**
     * Validate input fields
     */
    private boolean validateInputs(String targetN, String targetP, String targetK) {
        if (targetN.isEmpty()) {
            showToast("Please enter target nitrogen amount (kg)");
            etTargetNitrogen.requestFocus();
            return false;
        }

        if (targetP.isEmpty()) {
            showToast("Please enter target phosphorus amount (kg)");
            etTargetPhosphorus.requestFocus();
            return false;
        }

        if (targetK.isEmpty()) {
            showToast("Please enter target potassium amount (kg)");
            etTargetPotassium.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Validate target nutrient values
     */
    private boolean validateTargetValues(double nitrogen, double phosphorus, double potassium) {
        if (nitrogen < 0) {
            showToast("Target nitrogen amount cannot be negative");
            etTargetNitrogen.requestFocus();
            return false;
        }

        if (phosphorus < 0) {
            showToast("Target phosphorus amount cannot be negative");
            etTargetPhosphorus.requestFocus();
            return false;
        }

        if (potassium < 0) {
            showToast("Target potassium amount cannot be negative");
            etTargetPotassium.requestFocus();
            return false;
        }

        if (nitrogen == 0 && phosphorus == 0 && potassium == 0) {
            showToast("Please enter at least one target nutrient amount");
            return false;
        }

        // Reasonable upper limits (adjust as needed)
        if (nitrogen > 1000 || phosphorus > 1000 || potassium > 1000) {
            showToast("Target values seem unusually high. Please verify your inputs.");
            return false;
        }

        return true;
    }

    /**
     * Display calculation results
     */
    private void displayResults(double urea, double dap, double mop, double total, double dapNContribution) {
        tvUreaResult.setText(decimalFormat.format(urea) + " kg");
        tvDapResult.setText(decimalFormat.format(dap) + " kg");
        tvMopResult.setText(decimalFormat.format(mop) + " kg");
        tvTotalFertilizerResult.setText(decimalFormat.format(total) + " kg");

        // Show results card with animation
        cardResults.setVisibility(View.VISIBLE);
        cardResults.setAlpha(0f);
        cardResults.animate()
                .alpha(1f)
                .setDuration(300)
                .start();

        // Show additional info about DAP nitrogen contribution if significant
        if (dapNContribution > 1.0) {
            showToast("Note: DAP provides " + decimalFormat.format(dapNContribution) + " kg extra nitrogen");
        } else {
            showToast("Fertilizer planning calculation completed!");
        }
    }

    /**
     * Clear all input fields and hide results
     */
    private void clearAllFields() {
        // Clear input fields
        etTargetNitrogen.setText("");
        etTargetPhosphorus.setText("");
        etTargetPotassium.setText("");

        // Reset results
        tvUreaResult.setText("0.0 kg");
        tvDapResult.setText("0.0 kg");
        tvMopResult.setText("0.0 kg");
        tvTotalFertilizerResult.setText("0.0 kg");

        // Hide results card
        cardResults.setVisibility(View.GONE);

        // Clear focus and move to first field
        etTargetNitrogen.requestFocus();

        showToast("All fields cleared");
    }

    /**
     * Show toast message
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        // Finish all activities and exit the app
        finishAffinity();
        System.exit(0);  // Optional, ensures process is killed
    }
}