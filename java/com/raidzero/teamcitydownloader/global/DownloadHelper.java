package com.raidzero.teamcitydownloader.global;

import android.content.Context;

import com.raidzero.teamcitydownloader.data.DownloadRequest;
import com.raidzero.teamcitydownloader.tasks.DownloadTask;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by raidzero on 12/4/14.
 */
public class DownloadHelper {
    private static final String tag = "DownloadHelper";
    private Context mContext;
    private int mNotificationId = 0;
    private boolean mDownloadInProgress = false;

    // map of files downloaded under a notification id
    private HashMap<Integer, ArrayList<String>> mNotifiedFiles;

    // link a request URL to a task
    private HashMap<String, DownloadTask> mDownloadTaskMap;

    // list of current requests
    private ArrayList<DownloadRequest> mCurrentRequests;

    public DownloadHelper(Context context) {
        mContext = context;
        mNotifiedFiles = new HashMap<Integer, ArrayList<String>>();
        mDownloadTaskMap = new HashMap<String, DownloadTask>();
        mCurrentRequests = new ArrayList<DownloadRequest>();
    }

    public void addtoCurrentRequests(DownloadRequest request) {
        if (!isRequestInProgress(request)) {
            mCurrentRequests.add(request);
        }
    }

    public void removeFromCurrentRequests(DownloadRequest request) {
        if (isRequestInProgress(request)) {
            mCurrentRequests.remove(request);
        }
    }

    public void clearCurrentRequests() {
        mCurrentRequests.clear();
        mNotifiedFiles.clear();
    }

    public int getNumCurrentRequests() {
        return mCurrentRequests.size();
    }

    public DownloadRequest getRequestForFile(String fileName) {
        for (DownloadRequest r : mCurrentRequests) {
            if (r.fileName.equals(fileName)) {
                return r;
            }
        }

        return null;
    }

    public boolean isRequestInProgress(DownloadRequest request) {
        for (DownloadRequest r : mCurrentRequests) {
            if (r.url.equals(request.url)) {
                return true;
            }
        }

        return false;
    }

    public int getNextNotificationId() {
        return ++mNotificationId;
    }

    public int getNotificationId() {
        return mNotificationId;
    }

    public void addToNotifiedFiles(int notificationId, String fileName) {
        if (mNotifiedFiles.get(notificationId) == null) {
            mNotifiedFiles.put(notificationId, new ArrayList<String>());
        }

        mNotifiedFiles.get(notificationId).add(fileName);
    }

    public void removeFromNotifiedFiles(int notificationId, String fileName) {
        if (mNotifiedFiles != null) {
            if (mNotifiedFiles.containsKey(notificationId)) {
                if (mNotifiedFiles.get(notificationId).contains(fileName)) {
                    mNotifiedFiles.get(notificationId).remove(fileName);
                }
            }
        }
    }

    public boolean shouldBoldFile(String fileName, int notificationId) {
        if (mNotifiedFiles != null) {
            if (mNotifiedFiles.containsKey(notificationId)) {
                for (String f : mNotifiedFiles.get(notificationId)) {
                    if (fileName.equals(f)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void markFileSeen(String file, int notificationId) {
        if (mNotifiedFiles != null) {
            if (mNotifiedFiles.containsKey(notificationId)) {
                if (mNotifiedFiles.get(notificationId).contains(file)) {
                    mNotifiedFiles.get(notificationId).remove(file);
                }
            }
        }
    }

    public String getNotifiedFilesString(int notificationId) {
        ArrayList<String> files = mNotifiedFiles.get(notificationId);
        String rtn = "";

        for (String s : files) {
            rtn += s + "\n";
        }

        return rtn;
    }

    public void setIsDownloading(boolean downloading) {
        mDownloadInProgress = downloading;
    }

    public boolean isDownloadInProgress() {
        return mDownloadInProgress;
    }

    public void addDownloadTaskToMap(DownloadRequest request, DownloadTask task) {
        mDownloadTaskMap.put(request.url, task);
    }

    public String getNotificationContent() {
        String rtn = "";
        for (DownloadRequest r : mCurrentRequests) {
            rtn += r.fileName + "\n";
        }

        return rtn;
    }

    public boolean isFileDownloading(String fileName) {
        if (mCurrentRequests != null) {
            for (DownloadRequest r : mCurrentRequests) {
                if (fileName.equals(r.fileName)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void cancelDownload(String fileName) {
        DownloadRequest request = getRequestForFile(fileName);
        if (request != null) {
            DownloadTask task = mDownloadTaskMap.get(request.url);
            task.cancel(true);
            removeFromNotifiedFiles(request.notificationId, request.fileName);
        }
    }

    public void cancelDownload(String fileName, boolean showToast) {
        DownloadRequest request = getRequestForFile(fileName);
        if (request != null) {
            DownloadTask task = mDownloadTaskMap.get(request.url);
            task.showToast(showToast);
            task.cancel(true);
        }
    }
}
