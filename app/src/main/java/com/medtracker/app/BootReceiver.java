package com.medtracker.app;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import java.util.Calendar;
public class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            SharedPreferences prefs = context.getSharedPreferences("med_times", Context.MODE_PRIVATE);
            schedule(context, prefs, "Amitriptyline", 1001, 11);
            schedule(context, prefs, "Gabapentin", 1002, 19);
        }
    }
    void schedule(Context context, SharedPreferences prefs, String med, int code, int defHour) {
        int hour = prefs.getInt(med + "_hour", defHour);
        int minute = prefs.getInt(med + "_minute", 0);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        if (cal.getTimeInMillis() <= System.currentTimeMillis()) cal.add(Calendar.DAY_OF_YEAR, 1);
        Intent i = new Intent(context, AlarmReceiver.class);
        i.putExtra("med", med);
        PendingIntent pi = PendingIntent.getBroadcast(context, code, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        try { am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi); }
        catch (SecurityException e) { am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi); }
    }
}
