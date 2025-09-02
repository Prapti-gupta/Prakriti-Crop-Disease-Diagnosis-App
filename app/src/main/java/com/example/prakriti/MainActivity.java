package com.example.prakriti;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity {

    private UserDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if user has already entered their details
        SharedPreferences sharedPreferences = getSharedPreferences("PrakritiLogin", MODE_PRIVATE);
        boolean isUserRegistered = sharedPreferences.getBoolean("isUserRegistered", false);

        if (isUserRegistered) {
            Intent intent = new Intent(MainActivity.this, HomeScreen.class);
            startActivity(intent);
            finish();
            return;
        }

        dbHelper = new UserDatabaseHelper(this);

        Button b1 = findViewById(R.id.btn_submit);
        EditText etName = findViewById(R.id.et_name);
        EditText etRegion = findViewById(R.id.et_region);
        MaterialAutoCompleteTextView actGender = findViewById(R.id.act_gender);
        TextInputLayout genderLayout = findViewById(R.id.til_gender);

        // Set up gender dropdown with white background
        String[] genderOptions = {"Male", "Female"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, genderOptions) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setTextColor(Color.BLACK);
                textView.setBackgroundColor(Color.WHITE);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setTextColor(Color.BLACK);
                textView.setBackgroundColor(Color.WHITE);
                textView.setPadding(20, 20, 20, 20);
                return view;
            }
        };
        actGender.setAdapter(adapter);

        // Configure MaterialAutoCompleteTextView properly
        actGender.setInputType(0); // Prevent typing
        actGender.setKeyListener(null);
        actGender.setCursorVisible(false);
        actGender.setShowSoftInputOnFocus(false);

        // Force white background for dropdown regardless of theme
        actGender.setDropDownBackgroundDrawable(new ColorDrawable(Color.WHITE));

        // Remove anchor to prevent positioning issues - let it position naturally
        actGender.setDropDownVerticalOffset(0);
        actGender.setDropDownHeight(android.widget.ListPopupWindow.WRAP_CONTENT);

        // Prevent keyboard from appearing on focus
        actGender.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(actGender.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        });

        // Single OnClickListener
        actGender.setOnClickListener(v -> {
            // Always hide keyboard first
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(actGender.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }

            // Clear focus from other fields to prevent keyboard
            etName.clearFocus();
            etRegion.clearFocus();


            // Show dropdown after a short delay
            actGender.postDelayed(() -> {
                actGender.showDropDown();
            }, 50);
        });

        // Handle item selection
        actGender.setOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(actGender.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
            etName.clearFocus();
            etRegion.clearFocus();

            actGender.postDelayed(actGender::showDropDown, 50);
        });


        b1.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String gender = actGender.getText().toString().trim();
            String region = etRegion.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter your name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (gender.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please select your gender", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate gender - accepts case-insensitive variations
            String normalizedGender = "";
            if (gender.equalsIgnoreCase("Male")) {
                normalizedGender = "Male";
            } else if (gender.equalsIgnoreCase("Female")) {
                normalizedGender = "Female";
            } else {
                Toast.makeText(MainActivity.this, "Please select Male or Female", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save user data
            String dummyEmail = "user_" + System.currentTimeMillis() + "@offline.local";
            String dummyPassword = "offline_user";

            long userId = dbHelper.addUser(name, dummyEmail, dummyPassword, normalizedGender, region);

            if (userId != -1) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong("userId", userId);
                editor.putBoolean("isUserRegistered", true);
                editor.putString("userName", name);
                editor.putString("gender", normalizedGender);
                editor.putString("region", region);
                editor.apply();

                Toast.makeText(MainActivity.this, "Welcome " + name + "!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(MainActivity.this, OfflineInstruction.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(MainActivity.this, "Something went wrong. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Finish all activities and exit the app
        finishAffinity();
        System.exit(0);  // Optional, ensures process is killed
    }

}