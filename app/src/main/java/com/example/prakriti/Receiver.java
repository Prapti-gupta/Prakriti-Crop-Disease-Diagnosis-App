package com.example.prakriti;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Calendar;
import java.util.Random;

public class Receiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int reminderId = intent.getIntExtra("reminderId", 0);

        String title = "Reminder";
        String message = "It's time for your reminder!";

        Random random = new Random();

        // 🌞 Morning notifications
        if (reminderId == 1) {
            if (random.nextBoolean()) {
                title = "🌱 Rise & Scan!";
                message = "Start your day with a crop check — healthy fields, happy yields 🚜✨";
            } else {
                title = "💧 Time to Water!";
                message = "Keep your crops fresh and strong — don’t skip today’s watering 🌾✨";
            }
        }
        // 🌆 Evening notifications
        else if (reminderId == 2) {
            if (random.nextBoolean()) {
                title = "🌾 Evening Care";
                message = "Wrap up the day by checking your crops — healthy fields, peaceful nights ✨";
            } else {
                title = "🌙 Good Night, Green Fields";
                message = "End your day with care, tomorrow’s growth starts today 🌱💤";
            }
        }

        // ✅ Intent to open MainActivity when notification is tapped
        Intent activityIntent = new Intent(context, HomeScreen.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingActivityIntent = PendingIntent.getActivity(
                context,
                reminderId,
                activityIntent,
                PendingIntent.FLAG_IMMUTABLE
        );



        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "notifyMe")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(bitmap)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingActivityIntent) // make it clickable
                .setAutoCancel(true); // dismiss after click

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED || android.os.Build.VERSION.SDK_INT < 33) {
            notificationManager.notify(reminderId, builder.build());
        }
        rescheduleNextDay(context, reminderId);
    }

    @SuppressLint("ScheduleExactAlarm")
    private void rescheduleNextDay(Context context, int reminderId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        if (reminderId == 1) {
            calendar.set(Calendar.HOUR_OF_DAY, 8);
            calendar.set(Calendar.MINUTE, 40);
            calendar.set(Calendar.SECOND, 0);
        } else if (reminderId == 2) {
            calendar.set(Calendar.HOUR_OF_DAY, 18);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
        }
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        Intent newIntent = new Intent(context, Receiver.class);
        newIntent.putExtra("reminderId", reminderId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId,
                newIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent
        );
    }
}
