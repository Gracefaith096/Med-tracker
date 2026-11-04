package com.medtracker.app;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.util.Calendar;
public class MainActivity extends Activity {
    SharedPreferences prefs;
    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        prefs = getSharedPreferences("med_times", MODE_PRIVATE);
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel("med_alarm", "Medication Alarms", NotificationManager.IMPORTANCE_HIGH);
            ch.setDescription("Daily medication reminders");
            ch.enableVibration(true);
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nm.createNotificationChannel(ch);
        }
        if (Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }
        WebView w = new WebView(this);
        setContentView(w);
        WebSettings st = w.getSettings();
        st.setJavaScriptEnabled(true);
        st.setDomStorageEnabled(true);
        st.setDatabaseEnabled(true);
        w.setWebViewClient(new WebViewClient());
        w.addJavascriptInterface(new AlarmBridge(), "Android");
        w.loadUrl("file:///android_asset/index.html");
    }
    public class AlarmBridge {
        @JavascriptInterface
        public void setAlarm(String med, int hour, int minute) {
            prefs.edit().putInt(med + "_hour", hour).putInt(med + "_minute", minute).apply();
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            cal.set(Calendar.SECOND, 0);
            if (cal.getTimeInMillis() <= System.currentTimeMillis()) cal.add(Calendar.DAY_OF_YEAR, 1);
            Intent i = new Intent(MainActivity.this, AlarmReceiver.class);
            i.putExtra("med", med);
            int code = med.equals("Amitriptyline") ? 1001 : 1002;
            PendingIntent pi = PendingIntent.getBroadcast(MainActivity.this, code, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            try { am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi); }
            catch (SecurityException e) { am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi); }
        }
        @JavascriptInterface
        public String getTime(String med) {
            int h = prefs.getInt(med + "_hour", med.equals("Amitriptyline") ? 11 : 19);
            int m = prefs.getInt(med + "_minute", 0);
            return h + ":" + m;
        }
    }
}
