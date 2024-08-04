package com.example.waterintaketracker.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;

import com.example.shared.receivers.ReminderReceiver;
import com.example.waterintaketracker.utils.Constants;

public class ReminderService extends Service {

    private static final String ACTION_START_REMINDERS = "com.example.waterintaketracker.START_REMINDERS";
    private static final String ACTION_STOP_REMINDERS = "com.example.waterintaketracker.STOP_REMINDERS";

    public static void startReminders(Context context) {
        Intent intent = new Intent(context, ReminderService.class);
        intent.setAction(ACTION_START_REMINDERS);
        context.startService(intent);
    }

    public static void stopReminders(Context context) {
        Intent intent = new Intent(context, ReminderService.class);
        intent.setAction(ACTION_STOP_REMINDERS);
        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_START_REMINDERS.equals(action)) {
                handleStartReminders();
            } else if (ACTION_STOP_REMINDERS.equals(action)) {
                handleStopReminders();
            }
        }
        return START_NOT_STICKY;
    }

    private void handleStartReminders() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), Constants.REMINDER_INTERVAL, pendingIntent);
    }

    private void handleStopReminders() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        alarmManager.cancel(pendingIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}