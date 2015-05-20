package com.raidzero.teamcitydownloader.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.raidzero.teamcitydownloader.data.DownloadRequest;
import com.raidzero.teamcitydownloader.global.AppHelper;
import com.raidzero.teamcitydownloader.global.Debug;
import com.raidzero.teamcitydownloader.global.FileUtility;
import com.raidzero.teamcitydownloader.tasks.DownloadTask;

/**
 * Created by posborn on 6/27/14.
 */
public class DownloadActivity extends Activity  implements DownloadTask.DownloadTaskListener {
    private static final String tag = "DownloadActivity";

    private AppHelper helper;
    String fileName;
    boolean useGuest;
    private ProgressDialog mProgressDialog;
    private long bytesDownloaded = 0;
    private long totalBytes = 0;
    private int currentPercent = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        helper = (AppHelper) getApplicationContext();

        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        fileName = intent.getStringExtra("name");
        String authStr = intent.getStringExtra("authStr");
        int size = intent.getIntExtra("size", 0);
        totalBytes = size;
        useGuest = intent.getBooleanExtra("useGuest", false);

        int dialogTheme = helper.getDialogTheme();

        mProgressDialog = new ProgressDialog(this, dialogTheme);
        mProgressDialog.setMessage("Downloading " + fileName + "...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressNumberFormat(null);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);

        // log some stuff out
        Debug.Log(tag, "url: " + url);

        DownloadRequest request = new DownloadRequest();

        request.url = url;
        request.fileName = fileName;
        request.authStr = authStr;
        request.fileSize = size;

        final DownloadTask task = new DownloadTask(this);

        task.execute(request);

        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                task.cancel(true);
                finish();
            }
        });
    }

    @Override
    public void onDownloadBegin() {
        mProgressDialog.show();
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setMax(100);
    }

    @Override
    public void updateProgress(long currentBytes) {
        bytesDownloaded += currentBytes;

        int percent = (int) (((float) bytesDownloaded / (float) totalBytes) * 100);

        // only publish progress if the percentage has changed
        if (currentPercent < percent) {
            currentPercent = percent;
            mProgressDialog.setProgress(percent);
        }
    }

    @Override
    public void onDownloadComplete(String filePath, DownloadRequest request) {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        FileUtility.openFile(this, filePath);
        finish();
    }

    @Override
    public void onDownloadCancel(DownloadRequest canceledRequest) {
        if (canceledRequest != null) {
            FileUtility.deleteFile(helper.getStorageDirectory() + "/" + canceledRequest.fileName);
        }
        finish();
    }

}
