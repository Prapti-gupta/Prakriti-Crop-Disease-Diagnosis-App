package com.example.prakriti;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditProfile extends AppCompatActivity {

    private EditText editName, editAge, editRegion, phoneNumber, cropsGrown;
    private AutoCompleteTextView editGender;
    private TextView namee, editusername;
    private Button saveButton;
    private ImageButton back;
    private ImageView profile_picture;
    private UserDatabaseHelper dbHelper;
    private long userId;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Use the correct SharedPreferences name - same as MainActivity and HomeScreen
        sharedPreferences = getSharedPreferences("PrakritiLogin", MODE_PRIVATE);
        userId = sharedPreferences.getLong("userId", -1);

        // Initialize database helper
        dbHelper = new UserDatabaseHelper(this);

        // Initialize views - CORRECTED FIELD MAPPINGS
        editName = findViewById(R.id.edit_name);           // Full Name field
        editGender = findViewById(R.id.edit_gender);       // Gender field
        editAge = findViewById(R.id.edit_region);          // Age field (this was the confusion!)
        editRegion = findViewById(R.id.farm_location);     // Region field
        phoneNumber = findViewById(R.id.phone_number);     // Phone Number field
        cropsGrown = findViewById(R.id.crops_grown);       // Crops field
        saveButton = findViewById(R.id.save_button);
        back = findViewById(R.id.back_button);
        namee = findViewById(R.id.name);
        editusername = findViewById(R.id.username);
        profile_picture = findViewById(R.id.profile_picture);

        // Set profile picture based on gender
        setProfilePicture(profile_picture);

        // Set up gender dropdown - CORRECTED
        String[] genderOptions = {"Male", "Female"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, genderOptions) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setTextColor(getResources().getColor(android.R.color.black));
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setTextColor(getResources().getColor(android.R.color.black));
                textView.setBackgroundColor(getResources().getColor(android.R.color.white));
                textView.setPadding(20, 20, 20, 20);
                return view;
            }
        };
        editGender.setAdapter(adapter);

        // Configure dropdown to prevent typing and show proper behavior
        editGender.setThreshold(1000); // High threshold to prevent auto-show
        editGender.setInputType(0); // No input type
        editGender.setKeyListener(null); // Prevent typing
        editGender.setFocusable(false); // Prevent focus
        editGender.setCursorVisible(false); // Hide cursor
        editGender.setDropDownBackgroundResource(android.R.color.white);

        // Show dropdown when clicked and hide keyboard
        editGender.setOnClickListener(v -> {
            editGender.showDropDown();
            // Hide keyboard
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editGender.getWindowToken(), 0);
        });

        // Handle item selection
        editGender.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editGender.dismissDropDown();
                // Update profile picture immediately when gender changes
                setProfilePicture(profile_picture);
                // Hide keyboard
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editGender.getWindowToken(), 0);
            }
        });

        // Back button
        back.setOnClickListener(view -> {
            Intent i = new Intent(EditProfile.this, ProfileScreen.class);
            startActivity(i);
            finish();
        });

        // Save button with validation
        saveButton.setOnClickListener(view -> {
            String userName = editName.getText().toString().trim();
            String userGender = editGender.getText().toString().trim();
            String userAge = editAge.getText().toString().trim();
            String userRegion = editRegion.getText().toString().trim();
            String userPhoneNumber = phoneNumber.getText().toString().trim();
            String userCropsGrown = cropsGrown.getText().toString().trim();

            if (userId != -1) {
                // Validate required fields
                if (userName.isEmpty()) {
                    Toast.makeText(EditProfile.this, "Name is required", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate gender - now accepts case-insensitive variations
                if (!userGender.isEmpty() &&
                        !userGender.equalsIgnoreCase("Male") &&
                        !userGender.equalsIgnoreCase("Female")) {
                    Toast.makeText(EditProfile.this, "Gender must be Male or Female", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Normalize gender case for database storage
                String normalizedGender = "";
                if (!userGender.isEmpty()) {
                    if (userGender.equalsIgnoreCase("Male")) {
                        normalizedGender = "Male";
                    } else if (userGender.equalsIgnoreCase("Female")) {
                        normalizedGender = "Female";
                    }
                }

                // Validate age if provided
                if (!userAge.isEmpty()) {
                    try {
                        int age = Integer.parseInt(userAge);
                        if (age < 1 || age > 120) {
                            Toast.makeText(EditProfile.this, "Please enter a valid age", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(EditProfile.this, "Please enter a valid age", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                // Update user profile with normalized gender
                boolean updated = updateUserProfile(userId, userName, normalizedGender, userAge, userRegion, userPhoneNumber, userCropsGrown);

                if (updated) {
                    Toast.makeText(EditProfile.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();

                    // Update SharedPreferences with ALL the new data
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("userName", userName);
                    if (!normalizedGender.isEmpty()) {
                        editor.putString("gender", normalizedGender);
                    }
                    if (!userRegion.isEmpty()) {
                        editor.putString("region", userRegion);
                    }
                    if (!userAge.isEmpty()) {
                        editor.putString("age", userAge);
                    }
                    if (!userPhoneNumber.isEmpty()) {
                        editor.putString("phoneNumber", userPhoneNumber);
                    }
                    if (!userCropsGrown.isEmpty()) {
                        editor.putString("cropsGrown", userCropsGrown);
                    }
                    editor.apply();

                    // Update profile picture if gender changed
                    setProfilePicture(profile_picture);

                    // Reload the profile data
                    loadUserProfile();
                } else {
                    Toast.makeText(EditProfile.this, "Error Updating Profile", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(EditProfile.this, "User not found", Toast.LENGTH_SHORT).show();
            }
        });

        // Load user profile
        loadUserProfile();
    }

    private void setProfilePicture(ImageView profilePicture) {
        // Get gender from current selection or SharedPreferences
        String currentGender = editGender.getText().toString().trim();
        if (currentGender.isEmpty()) {
            currentGender = sharedPreferences.getString("gender", "Male");
        }

        // Debug logging
        android.util.Log.d("EditProfile", "Setting profile picture for gender: " + currentGender);

        if ("Female".equalsIgnoreCase(currentGender)) {
            profilePicture.setImageResource(R.drawable.female_farmer_green);
        } else {
            profilePicture.setImageResource(R.drawable.male_farmer_green);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update profile picture when returning to this activity
        setProfilePicture(profile_picture);
        loadUserProfile();
    }

    private void loadUserProfile() {
        // First, try to load from SharedPreferences (most up-to-date data)
        String spName = sharedPreferences.getString("userName", "");
        String spGender = sharedPreferences.getString("gender", "");
        String spRegion = sharedPreferences.getString("region", "");
        String spAge = sharedPreferences.getString("age", "");
        String spPhone = sharedPreferences.getString("phoneNumber", "");
        String spCrops = sharedPreferences.getString("cropsGrown", "");

        // Then load from database to fill in any missing data
        Cursor cursor = dbHelper.getUserInfo(userId);
        if (cursor != null && cursor.moveToFirst()) {
            String dbName = cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_USER_NAME));
            String dbGender = cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_USER_GENDER));
            String dbAge = cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_USER_AGE));
            String dbRegion = cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_USER_REGION));
            String dbPhone = cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_PHONE_NUMBER));
            String dbCrops = cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_CROP_TYPE));

            // Use SharedPreferences data if available, otherwise use database data
            String finalName = !spName.isEmpty() ? spName : (dbName != null ? dbName : "");
            String finalGender = !spGender.isEmpty() ? spGender : (dbGender != null ? dbGender : "");
            String finalAge = !spAge.isEmpty() ? spAge : (dbAge != null ? dbAge : "");
            String finalRegion = !spRegion.isEmpty() ? spRegion : (dbRegion != null ? dbRegion : "");
            String finalPhone = !spPhone.isEmpty() ? spPhone : (dbPhone != null ? dbPhone : "");
            String finalCrops = !spCrops.isEmpty() ? spCrops : (dbCrops != null ? dbCrops : "");

            // Display profile information in header
            namee.setText(!finalName.isEmpty() ? finalName : "User");
            editusername.setText("@" + (!finalName.isEmpty() ? finalName.toLowerCase().replaceAll("\\s+", "") : "user"));

            // Set values in edit fields
            editName.setText(finalName);
            editGender.setText(finalGender);
            editAge.setText(finalAge);
            editRegion.setText(finalRegion);
            phoneNumber.setText(finalPhone);
            cropsGrown.setText(finalCrops);

            // Update SharedPreferences with database data if SharedPreferences is missing some data
            if (spName.isEmpty() || spGender.isEmpty() || spRegion.isEmpty() || spAge.isEmpty() || spPhone.isEmpty() || spCrops.isEmpty()) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (spName.isEmpty() && !finalName.isEmpty()) {
                    editor.putString("userName", finalName);
                }
                if (spGender.isEmpty() && !finalGender.isEmpty()) {
                    editor.putString("gender", finalGender);
                }
                if (spRegion.isEmpty() && !finalRegion.isEmpty()) {
                    editor.putString("region", finalRegion);
                }
                if (spAge.isEmpty() && !finalAge.isEmpty()) {
                    editor.putString("age", finalAge);
                }
                if (spPhone.isEmpty() && !finalPhone.isEmpty()) {
                    editor.putString("phoneNumber", finalPhone);
                }
                if (spCrops.isEmpty() && !finalCrops.isEmpty()) {
                    editor.putString("cropsGrown", finalCrops);
                }
                editor.apply();
            }
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    private boolean updateUserProfile(long userId, String name, String gender, String age, String region, String phoneNumber, String cropsGrown) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Update users table
        ContentValues userValues = new ContentValues();
        userValues.put(UserDatabaseHelper.COLUMN_USER_NAME, name);
        userValues.put(UserDatabaseHelper.COLUMN_USER_GENDER, gender);
        userValues.put(UserDatabaseHelper.COLUMN_USER_AGE, age);
        userValues.put(UserDatabaseHelper.COLUMN_USER_REGION, region);

        int userRowsAffected = db.update(
                UserDatabaseHelper.TABLE_USERS,
                userValues,
                UserDatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)}
        );

        // Update user_info table
        ContentValues profileValues = new ContentValues();
        profileValues.put(UserDatabaseHelper.COLUMN_PHONE_NUMBER, phoneNumber);
        profileValues.put(UserDatabaseHelper.COLUMN_CROP_TYPE, cropsGrown);
        profileValues.put(UserDatabaseHelper.COLUMN_FARM_SIZE, ""); // Not using
        profileValues.put(UserDatabaseHelper.COLUMN_FARM_LOCATION, ""); // Not using

        int profileRowsAffected = db.update(
                UserDatabaseHelper.TABLE_USER_INFO,
                profileValues,
                UserDatabaseHelper.COLUMN_FOREIGN_KEY_USER_ID + " = ?",
                new String[]{String.valueOf(userId)}
        );

        return userRowsAffected > 0 || profileRowsAffected > 0;
    }
}