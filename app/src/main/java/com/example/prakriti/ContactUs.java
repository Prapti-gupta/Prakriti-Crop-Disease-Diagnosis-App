package com.example.prakriti;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ContactUs extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);
    }
    // Male trainer button actions
    public void call_p(View view) {
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:9702550580"));
        startActivity(callIntent);
    }

    public void message_p(View view) {
        Intent messageIntent = new Intent(Intent.ACTION_VIEW);
        messageIntent.setData(Uri.parse("sms:9702550580"));
        messageIntent.putExtra("sms_body", "Hello, Prapti!");
        startActivity(messageIntent);
    }

    public void email_p(View view) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");  // Ensures only email apps handle the intent
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"praptig2008@gmail.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Prakriti App Inquiry");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello, I would like to know more about the Prakriti App.");

        startActivity(Intent.createChooser(emailIntent, "Send email"));
    }

    // Female trainer button actions
    public void call_t(View view) {
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:7678002603"));
        startActivity(callIntent);
    }

    public void message_t(View view) {
        Intent messageIntent = new Intent(Intent.ACTION_VIEW);
        messageIntent.setData(Uri.parse("sms:7678002603"));
        messageIntent.putExtra("sms_body", "Hello, Tanisha!");
        startActivity(messageIntent);
    }

    public void email_t(View view) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");  // Ensures only email apps handle the intent
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"shaha.tanisha@gmail.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Prakriti App Inquiry");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello, I would like to know more about the Prakriti App.");

        startActivity(Intent.createChooser(emailIntent, "Send email"));
    }

}