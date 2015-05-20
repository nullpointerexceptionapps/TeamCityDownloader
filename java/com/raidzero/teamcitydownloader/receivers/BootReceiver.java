package com.raidzero.teamcitydownloader.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.raidzero.teamcitydownloader.global.AppHelper;
import com.raidzero.teamcitydownloader.services.NotificationService;

/**
 * Created by raidzero on 11/30/14.
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String tag = "com.raidzero.teamcitydownloader.receivers.BootReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {


        AppHelper helper = (AppHelper) context.getApplicationContext();
        if (helper.getBoolPref("pref_enable_notifications")) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent notificationServiceIntent = new Intent(context, NotificationService.class);
            PendingIntent pendingIntent = PendingIntent.getService(context, 0, notificationServiceIntent, 0);

            alarmManager.cancel(pendingIntent);

            long interval = helper.getCheckInterval();
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + interval, interval, pendingIntent);

            // log this even without debuggable
            Log.d(tag, "Boot has completed, scheduling next server query...");
        }
    }
}
