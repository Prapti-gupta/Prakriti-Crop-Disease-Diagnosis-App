package com.example.prakriti;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
        AutoCompleteTextView actGender = findViewById(R.id.act_gender);

        // Set up gender dropdown
        String[] genderOptions = {"Male", "Female"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, genderOptions);
        actGender.setAdapter(adapter);

        // White background for dropdown
        actGender.setDropDownBackgroundResource(android.R.color.white);

        // Prevent typing + hide keyboard
        actGender.setInputType(InputType.TYPE_NULL);
        actGender.setFocusable(false);

        // Show dropdown when clicked
        actGender.setOnClickListener(v -> actGender.showDropDown());

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
}
