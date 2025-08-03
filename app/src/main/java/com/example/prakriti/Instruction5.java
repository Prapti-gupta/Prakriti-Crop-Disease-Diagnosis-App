package com.example.prakriti;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class Instruction5 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_instruction5);

        Button b1 = findViewById(R.id.btn_skip);
        Button b2 = findViewById(R.id.btn_allow);

        b2.setOnClickListener(v -> {

        });

        b1.setOnClickListener(v -> {
            Intent intent = new Intent(Instruction5.this, HomeScreen.class);
            startActivity(intent);
            finish();
            overridePendingTransition(0, 0);

        });
    }
}