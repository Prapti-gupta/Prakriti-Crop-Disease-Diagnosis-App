package com.example.prakriti;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Calendar;

public class Instruction5 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_instruction5);

        // Ask for notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        // Opens Setting to enable exact alarms
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }

        createNotificationChannel();

        Button b1 = findViewById(R.id.btn_skip);
        Button b2 = findViewById(R.id.btn_allow);

        b2.setOnClickListener(v -> {

            Toast.makeText(Instruction5.this, "Daily Reminders Set", Toast.LENGTH_SHORT).show();
            scheduleDailyReminders();

            Intent intent = new Intent(Instruction5.this, HomeScreen.class);
            startActivity(intent);
            finish();
            overridePendingTransition(0, 0);

        });

        b1.setOnClickListener(v -> {

            Intent intent = new Intent(Instruction5.this, HomeScreen.class);
            startActivity(intent);
            finish();
            overridePendingTransition(0, 0);

        });
    }

    // 🔔 Schedule both reminders (8 AM & 6 PM)
    public void scheduleDailyReminders() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        // 8 AM reminder
        Calendar calendarMorning = Calendar.getInstance();
        calendarMorning.set(Calendar.HOUR_OF_DAY, 8);
        calendarMorning.set(Calendar.MINUTE, 40);
        calendarMorning.set(Calendar.SECOND, 0);

        if (calendarMorning.getTimeInMillis() < System.currentTimeMillis()) {
            calendarMorning.add(Calendar.DAY_OF_MONTH, 1);
        }

        Intent intentMorning = new Intent(Instruction5.this, Receiver.class);
        intentMorning.putExtra("reminderId", 1);

        PendingIntent pendingIntentMorning = PendingIntent.getBroadcast(
                Instruction5.this,
                1,
                intentMorning,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        setExactAlarm(alarmManager, calendarMorning.getTimeInMillis(), pendingIntentMorning);

        // 6 PM reminder
        Calendar calendarEvening = Calendar.getInstance();
        calendarEvening.set(Calendar.HOUR_OF_DAY, 18);
        calendarEvening.set(Calendar.MINUTE, 0);
        calendarEvening.set(Calendar.SECOND, 0);

        if (calendarEvening.getTimeInMillis() < System.currentTimeMillis()) {
            calendarEvening.add(Calendar.DAY_OF_MONTH, 1);
        }

        Intent intentEvening = new Intent(Instruction5.this, Receiver.class);
        intentEvening.putExtra("reminderId", 2);

        PendingIntent pendingIntentEvening = PendingIntent.getBroadcast(
                Instruction5.this,
                2,
                intentEvening,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        setExactAlarm(alarmManager, calendarEvening.getTimeInMillis(), pendingIntentEvening);
    }


    @SuppressLint("ScheduleExactAlarm")
    private void setExactAlarm(AlarmManager alarmManager, long triggerAtMillis, PendingIntent pi) {
        if (alarmManager == null || pi == null) return;

        try {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pi
            );
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "ReminderChannel";
            String description = "Channel for Reminder";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("notifyMe", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
