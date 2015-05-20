package com.raidzero.teamcitydownloader.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.raidzero.teamcitydownloader.R;
import com.raidzero.teamcitydownloader.activities.FavoritesActivity;
import com.raidzero.teamcitydownloader.data.TeamCityItem;
import com.raidzero.teamcitydownloader.global.AppHelper;
import com.raidzero.teamcitydownloader.global.Debug;
import com.raidzero.teamcitydownloader.tasks.QueryBuildIdsTask;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by raidzero on 11/30/14.
 */
public class NotificationService extends Service implements QueryBuildIdsTask.OnQueryBuildIdsCompleteListener {
    private static final String tag = "NotificationService";

    private AppHelper mHelper;

    private int mNotificationId = 0;

    private ArrayList<String> notifiedConfigs = new ArrayList<String>();
    private HashMap<String, Integer> currentIds = new HashMap<String, Integer>();
    private ArrayList<String> configUrls = new ArrayList<String>();
    private HashMap<String, String> configNameMap = new HashMap<String, String>();

    @Override
    public void onCreate() {
        super.onCreate();
        mHelper = (AppHelper) getApplicationContext();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Debug.Log(tag, "onStartCommand()");
        startQuery();
        return START_NOT_STICKY;
    }

    private void startQuery() {
        Debug.Log(tag, "startQuery()");

        ArrayList<TeamCityItem> starredConfigs = mHelper.getStarredConfigs();

        for (TeamCityItem i : starredConfigs) {
            configNameMap.put(i.getUrl(), i.getName());
            configUrls.add(i.getUrl());
            currentIds.put(i.getUrl(), mHelper.getLastBuildId(i.getUrl()));
        }

        if (configUrls.size() > 0) {
            QueryBuildIdsTask task = new QueryBuildIdsTask(this);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, configUrls);
        } else {
            stopSelf();
        }
    }

    @Override
    public void onQueryComplete(HashMap<String, Integer> values) {
        int numNewBuilds = 0;
        notifiedConfigs.clear();

        if (values != null && values.size() > 0) {
            for (String url : configUrls) {
                int oldId = currentIds.get(url);
                int newId = 0;

                // there may not be any builds in here, if not just leave id as 0
                if (values.containsKey(url)) {
                    newId = values.get(url);
                }

                String name = configNameMap.get(url);
                Debug.Log(tag, String.format("%s: lastId: %d newId: %d", name, oldId, newId));

                // always save new id
                if (newId > oldId) {
                    mHelper.addFavoriteBuildId(url + "/builds", newId);

                    // but only display notification if we had a real id saved before
                    if (oldId > -1) {
                        notifiedConfigs.add(name);
                        numNewBuilds++;
                    }
                }
            }
        }

        if (numNewBuilds > 0) {
            showNotification(numNewBuilds);
        } else {
            stopSelf();
        }
    }

    private void showNotification(int numNewBuilds) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent resultIntent = new Intent(getApplicationContext(), FavoritesActivity.class);
        resultIntent.putStringArrayListExtra("configs", notifiedConfigs);
        final PendingIntent launchAppIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT);

        String notificationContent;

        if (numNewBuilds < 2) {
            // display single
            notificationContent = String.format(getString(R.string.notify_content_text_single), notifiedConfigs.get(0));
        } else {
            // display multiple
            notificationContent = getString(R.string.notify_content_text_multiple) + "\n";
            for (String name : notifiedConfigs) {
                notificationContent += name + "\n";
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(getString(R.string.notify_content_title))
                .setContentText(notificationContent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationContent))
                .setContentIntent(launchAppIntent);

        notificationManager.notify(mNotificationId, builder.build());

        stopSelf();
    }
}
