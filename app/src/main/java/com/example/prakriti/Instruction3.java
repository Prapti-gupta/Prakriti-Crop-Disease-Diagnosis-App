package com.example.prakriti;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class Instruction3 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_instruction3);

        Button b1 = findViewById(R.id.btn_next);
        Button b2 = findViewById(R.id.btn_back);

        b2.setOnClickListener(v -> {
            Intent intent = new Intent(Instruction3.this, Instruction2.class);
            startActivity(intent);
            finish();
            overridePendingTransition(0, 0);
        });

        b1.setOnClickListener(v -> {
            Intent intent = new Intent(Instruction3.this, Instruction4.class);
            startActivity(intent);
            finish();
            overridePendingTransition(0, 0);
        });
    }

    public void onBackPressed() {
        Intent intent = new Intent(Instruction3.this, Instruction2.class);
        // Clear all activities above TargetActivity from the back stack
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish(); // optional, closes CurrentActivity
    }
}