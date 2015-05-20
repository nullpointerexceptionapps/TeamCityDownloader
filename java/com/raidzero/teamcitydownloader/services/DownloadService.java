package com.raidzero.teamcitydownloader.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.raidzero.teamcitydownloader.R;
import com.raidzero.teamcitydownloader.activities.DownloadsListActivity;
import com.raidzero.teamcitydownloader.data.DownloadRequest;
import com.raidzero.teamcitydownloader.global.AppHelper;
import com.raidzero.teamcitydownloader.global.Debug;
import com.raidzero.teamcitydownloader.global.DialogUtility;
import com.raidzero.teamcitydownloader.global.DownloadHelper;
import com.raidzero.teamcitydownloader.tasks.DownloadTask;

import java.io.File;
import java.io.IOException;

/**
 * Created by posborn on 11/26/14.
 */
public class DownloadService extends Service implements DownloadTask.DownloadTaskListener {
    private static final String tag = "DownloadService";

    private final IBinder myBinder = new MyLocalBinder();

    private long mTotalBytes, mCurrentBytes = 0;
    private int mNumDownloads, mMaxDownloads, mCurrentPercent = 0;

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotificationBuilder = null;

    private boolean mNotificationShown = false;
    private int mNotificationId = 0;

    private Intent resultIntent;

    private boolean mContentChanged = false;
    private AppHelper mHelper;
    private DownloadHelper mDownloadHelper;

    public class MyLocalBinder extends Binder {
        public DownloadService getService() {
            return DownloadService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mHelper = (AppHelper) getApplicationContext();
        mDownloadHelper = mHelper.getDownloadHelper();
        resultIntent = new Intent(getApplicationContext(), DownloadsListActivity.class);
        mNotificationId = mDownloadHelper.getNotificationId();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Debug.Log(tag, "Service Bound!");
        return myBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Debug.Log(tag ,"Service unbound!");
    }

    public void queueDownload(DownloadRequest request) {
        if (!mDownloadHelper.isRequestInProgress(request)) {
            mNumDownloads++;
            mDownloadHelper.addtoCurrentRequests(request);

            if (mNumDownloads > mMaxDownloads) {
                mMaxDownloads = mNumDownloads;
            }

            mTotalBytes += request.fileSize;

            mDownloadHelper.addToNotifiedFiles(mNotificationId, request.fileName);
            request.notificationId = mDownloadHelper.getNotificationId();

            File f = new File(mHelper.getStorageDirectory() + "/" + request.fileName);
            try {
                f.createNewFile();
            } catch (IOException ignored) {

            }

            // start task
            DownloadTask task = new DownloadTask(this);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, request);

            mDownloadHelper.addDownloadTaskToMap(request, task);
        } else {
            DialogUtility.makeToast(getApplicationContext(), "Already downloading");
        }
    }

    @Override
    public void onDownloadBegin() {
        mDownloadHelper.setIsDownloading(true);
        if (!mNotificationShown || mNotificationBuilder == null) {
            // make a new one
            mNotificationBuilder = new NotificationCompat.Builder(this)
                    .setAutoCancel(false)
                    .setOngoing(true)
                    .setProgress(100, 0, false);

        }

        String notificationTitle = getTitleString();
        String notificationContent = mDownloadHelper.getNotificationContent();

        mNotificationBuilder.setSmallIcon(android.R.drawable.stat_sys_download);
        mNotificationBuilder.setContentTitle(notificationTitle);

        mNotificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(notificationContent));
        mNotificationBuilder.setContentText(notificationContent);

        mNotificationManager.notify(mNotificationId, mNotificationBuilder.build());

        mNotificationShown = true;
    }

    @Override
    public void updateProgress(long bytesDownloaded) {
        if (mNotificationShown) {
            mCurrentBytes += bytesDownloaded;
            int percent = (int) (((float) mCurrentBytes / (float) mTotalBytes) * 100);

            // only update notification if the percentage has actually changed (or if we need to refresh the text)
            if (mCurrentPercent < percent || mContentChanged) {
                mContentChanged = false;
                mCurrentPercent = percent;
                mNotificationBuilder.setProgress(100, mCurrentPercent, false);
                mNotificationBuilder.setOngoing(true);
                String notificationContent = mDownloadHelper.getNotificationContent();
                mNotificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(notificationContent));
                mNotificationBuilder.setContentText(notificationContent);

                mNotificationBuilder.setContentTitle(getTitleString());
                // make a pending intent
                resultIntent.removeExtra("notificationId");
                resultIntent.setAction("com.raidzero.teamcitydownloader.DownloadsListActivity.inProgress");

                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                mNotificationBuilder.setContentIntent(pendingIntent);

                mNotificationManager.notify(mNotificationId, mNotificationBuilder.build());
            }
        }
    }

    @Override
    public void onDownloadComplete(String filePath, DownloadRequest request) {
        mDownloadHelper.removeFromCurrentRequests(request);

        mNumDownloads--;
        if (mNumDownloads == 0) {
            Debug.Log(tag, "Downloads Complete!");
            // cancel existing
            mNotificationManager.cancel(mNotificationId);
            int lastNotificationId = mNotificationId;

            String notificationContent = mDownloadHelper.getNotifiedFilesString(lastNotificationId);

            // increment id
            mNotificationId = mDownloadHelper.getNextNotificationId();

            // make new one
            mNotificationBuilder = new NotificationCompat.Builder(this)
                    .setOngoing(false)
                    .setProgress(0, 0, false)
                    .setContentTitle(getString(R.string.download_complete))
                    .setSmallIcon(android.R.drawable.stat_sys_download_done)
                    .setAutoCancel(true)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationContent))
                    .setContentText(notificationContent)
            ;

            resultIntent.putExtra("notificationId", lastNotificationId);
            resultIntent.setAction("com.raidzero.teamcitydownloader.DownloadsListActivity.complete." + lastNotificationId);

            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            mNotificationBuilder.setContentIntent(pendingIntent);
            mNotificationManager.notify(mNotificationId, mNotificationBuilder.build());

            // reset everything
            mMaxDownloads = 0;
            mCurrentPercent = 0;
            mCurrentBytes = 0;
            mTotalBytes = 0;
            mNotificationShown = false;
            mNotificationBuilder = null;
            mNotificationId = mDownloadHelper.getNextNotificationId();

            mDownloadHelper.setIsDownloading(false);
            mHelper.unbindDownloadService();
        } else {
            // update the title
            mNotificationBuilder.setContentTitle(getTitleString());
            mNotificationBuilder.build();
        }
    }

    @Override
    public void onDownloadCancel(DownloadRequest canceledRequest) {

        if (canceledRequest != null) {
            mTotalBytes -= canceledRequest.fileSize;
            mCurrentBytes -= canceledRequest.bytesDownloaded;
            mContentChanged = true;
            mDownloadHelper.removeFromCurrentRequests(canceledRequest);
        }

        mNumDownloads--;
        if (mDownloadHelper.getNumCurrentRequests() == 0) {
            mNotificationManager.cancel(mNotificationId);
        }
    }

    private String getTitleString() {
        String rtn = String.format(getString(R.string.download_in_progress), mNumDownloads);
        if (mNumDownloads > 1) {
            rtn += "s";
        }

        rtn += "\u2026"; // unicode for ellipsis

        return rtn;
    }

    public void removeAllNotifications() {
        for (int i = 0; i <= mNotificationId; i++) {
            mNotificationManager.cancel(i);
        }
    }


}
