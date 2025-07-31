package com.example.prakriti;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button b1 = findViewById(R.id.btn_submit);
        EditText etName = findViewById(R.id.et_name);
        EditText etRegion = findViewById(R.id.et_region);
        AutoCompleteTextView actGender = findViewById(R.id.act_gender);

        // Set up gender dropdown
        String[] genderOptions = {"Male", "Female"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, genderOptions);
        actGender.setAdapter(adapter);

        // Handle submit button
        b1.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String gender = actGender.getText().toString().trim();
            String region = etRegion.getText().toString().trim();

            // Process form data
            Intent intent = new Intent(MainActivity.this, OfflineInstruction.class);
            startActivity(intent);
            finish();
        });
    }
}