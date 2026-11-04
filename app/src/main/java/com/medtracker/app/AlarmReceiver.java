package com.medtracker.app;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import java.util.Calendar;
public class AlarmReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        String med = intent.getStringExtra("med");
        if (med == null) med = "Medication";
        int code = med.equals("Amitriptyline") ? 1001 : 1002;
        Intent open = new Intent(context, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification n = new Notification.Builder(context, "med_alarm")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Time to take " + med)
            .setContentText("Tap to open Med Tracker and mark it as taken")
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setPriority(Notification.PRIORITY_HIGH)
            .setDefaults(Notification.DEFAULT_ALL)
            .build();
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(code, n);
        SharedPreferences prefs = context.getSharedPreferences("med_times", Context.MODE_PRIVATE);
        int hour = prefs.getInt(med + "_hour", med.equals("Amitriptyline") ? 11 : 19);
        int minute = prefs.getInt(med + "_minute", 0);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 1);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        Intent next = new Intent(context, AlarmReceiver.class);
        next.putExtra("med", med);
        PendingIntent nextPi = PendingIntent.getBroadcast(context, code, next, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        try { am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), nextPi); }
        catch (SecurityException e) { am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), nextPi); }
    }
}
