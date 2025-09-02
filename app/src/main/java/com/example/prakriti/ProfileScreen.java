package com.example.prakriti;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileScreen extends AppCompatActivity {

    // Remove the TextView declarations that don't exist in your layout
    private UserDatabaseHelper dbHelper;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_screen);

        // Initialize database helper
        dbHelper = new UserDatabaseHelper(this);

        // Get user ID from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("ActiveLifeLogin", MODE_PRIVATE);
        userId = sharedPreferences.getLong("userId", -1);

        // Navigation elements
        LinearLayout navHome = findViewById(R.id.nav_home);
        LinearLayout navCrops = findViewById(R.id.nav_crops);
        LinearLayout navNPK = findViewById(R.id.nav_npk);
        LinearLayout navProfile = findViewById(R.id.nav_profile);
        ImageButton btnCamera = findViewById(R.id.btn_camera);
        LinearLayout contact_expert = findViewById(R.id.btnContactExpert);
        LinearLayout edit_profile = findViewById(R.id.btnEditProfile);
        LinearLayout help = findViewById(R.id.btnHelpFaq);
        LinearLayout notification = findViewById(R.id.btnNotifications);
        LinearLayout about = findViewById(R.id.btnAboutApp);
        LinearLayout logout = findViewById(R.id.btnLogout);

        // Load and display user profile (if you have TextViews in your layout)
        loadUserProfile();

        // Set click listeners

        notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileScreen.this, Instruction5.class);
                startActivity(intent);
            }
        });

        contact_expert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileScreen.this, ContactTrainer.class);
                startActivity(intent);
            }
        });

        edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileScreen.this, EditProfile.class);
                startActivity(intent);
            }
        });

        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileScreen.this, HelpActivity.class);
                startActivity(intent);
            }
        });

        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileScreen.this, AboutActivity.class);
                startActivity(intent);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences sharedPreferences = getSharedPreferences("PrakritiLogin", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isUserRegistered", false);
                editor.apply();

                Intent intent = new Intent(ProfileScreen.this, MainActivity.class);
                startActivity(intent);
            }
        });

        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileScreen.this, HomeScreen.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        navCrops.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileScreen.this, Crops.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        navNPK.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileScreen.this, NPKCalculator.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        navProfile.setOnClickListener(v -> {
            // Already on profile screen, no need to navigate
        });

        btnCamera.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileScreen.this, CameraActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
    }

    private void setProfilePicture(ImageView profilePicture) {
        // Always use the same SharedPreferences file
        SharedPreferences sharedPreferences = getSharedPreferences("PrakritiLogin", MODE_PRIVATE);
        String gender = sharedPreferences.getString("gender", "male"); // Default male

        if ("female".equalsIgnoreCase(gender)) {
            profilePicture.setImageResource(R.drawable.female_farmer_green);
        } else {
            profilePicture.setImageResource(R.drawable.male_farmer_green);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Update profile picture when returning to this activity
        ImageView profilePicture = findViewById(R.id.profileImage);
        if (profilePicture != null) {
            setProfilePicture(profilePicture);
        }


        TextView tvGreeting = findViewById(R.id.userName);
        SharedPreferences sharedPreferences = getSharedPreferences("PrakritiLogin", MODE_PRIVATE);
        String userName = sharedPreferences.getString("userName", "Kisan ji");
        tvGreeting.setText(userName);

        loadUserProfile();
    }

    private void loadUserProfile() {
        if (userId != -1) {
            Cursor cursor = dbHelper.getUserInfo(userId);
            if (cursor != null && cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_USER_NAME));
                String gender = cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_USER_GENDER));
                String region = cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_USER_REGION));
                String farmLocation = cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_FARM_LOCATION));
                String phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_PHONE_NUMBER));

                // Only set TextViews if they exist in your layout
                // You need to add findViewById() calls for any TextViews you want to display
                // For example:
                // TextView nameView = findViewById(R.id.profile_name_display);
                // if (nameView != null) {
                //     nameView.setText(name != null ? name : "Not provided");
                // }

                // If you have TextViews in your layout for displaying profile info,
                // initialize them with findViewById() and then set their text here
            }
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void onBackPressed() {
        // Finish all activities and exit the app
        finishAffinity();
        System.exit(0);  // Optional, ensures process is killed
    }
}