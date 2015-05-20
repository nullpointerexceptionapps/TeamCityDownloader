package com.raidzero.teamcitydownloader.tasks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.widget.Toast;

import com.raidzero.teamcitydownloader.R;
import com.raidzero.teamcitydownloader.data.DownloadRequest;
import com.raidzero.teamcitydownloader.global.AppHelper;
import com.raidzero.teamcitydownloader.global.Debug;
import com.raidzero.teamcitydownloader.global.DialogUtility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by posborn on 11/26/14.
 */
public class DownloadTask extends AsyncTask<DownloadRequest, Integer, String> {
    private static final String tag = "DownloadTask";
    private Context context;
    private AppHelper helper;

    private DownloadTaskListener listener;
    private DownloadRequest request;
    private boolean showCancelToast = true;

    public interface DownloadTaskListener {
        void onDownloadBegin();
        void updateProgress(long bytesDownloaded);
        void onDownloadComplete(String filePath, DownloadRequest request);
        void onDownloadCancel(DownloadRequest request);
    }

    public DownloadTask(Context context) {
        this.context = context;

        listener = (DownloadTaskListener) context;

        this.helper = (AppHelper) context.getApplicationContext();
    }

    @SuppressLint("WorldReadableFiles") // files need to be world-readable so other apps can open
    @Override
    protected String doInBackground(DownloadRequest... requests) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            request = requests[0];
            String downloadUrl = request.url;
            String fileName = request.fileName;

            URL url = new URL(downloadUrl);
            String basicAuth = request.authStr;
            long size = request.fileSize;

            connection = (HttpURLConnection) url.openConnection();

            boolean useGuest = (basicAuth == null);

            if (!useGuest) {
                basicAuth = "Basic " + new String(Base64.encode(basicAuth.getBytes(), Base64.NO_WRAP));
                connection.setRequestProperty("Authorization", basicAuth);
            }

            connection.connect();

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage();
            }

            // this will be useful to display download percentage
            // might be -1: server did not report the length
            long fileLength = connection.getContentLength();

            Debug.Log(tag, "File Length: " + fileLength);
            if (fileLength == -1) {
                fileLength = size;
                Debug.Log(tag, "passed fileLength: " + fileLength);
            }

            // download the file
            input = connection.getInputStream();

            String downloadPath = helper.getStorageDirectory();

            File file = new File(downloadPath);

            file.mkdirs();

            file.setExecutable(true, false); // world executable
            file.setReadable(true, false); // world-readable

            // delete the destination file if it exists
            File destFile = new File(downloadPath + "/" + fileName);

            if (destFile.exists()) {
                destFile.delete();
            }

            //noinspection deprecation
            output = new FileOutputStream(downloadPath + "/" + fileName, true);

            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                // allow canceling with back button
                if (isCancelled()) {
                    input.close();
                    return null;
                }
                request.bytesDownloaded += count;

                total += count;
                // publishing the progress....
                if (fileLength > 0) { // only if total length is known
                    listener.updateProgress(count);
                    //publishProgress((int) (total * 100 / fileLength));
                }
                output.write(data, 0, count);
            }
        } catch (Exception e) {
            return e.toString();
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
                if (input != null)
                    input.close();
            } catch (Exception ignored) {

            }

            if (connection != null)
                connection.disconnect();
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        listener.onDownloadBegin();
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
    }

    @Override
    protected void onPostExecute(String result) {

        String fileLocation = helper.getStorageDirectory() + "/" + request.fileName;

        if (result != null) {
            Toast.makeText(context, "Download error: " + result, Toast.LENGTH_LONG).show();
            Debug.Log(tag, result);
            cancel(true);
            listener.onDownloadCancel(request);
        } else {
            listener.onDownloadComplete(fileLocation, request);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        Debug.Log(tag, "DownloadTask cancelled");

        if (showCancelToast) {
            if (request != null) {
                DialogUtility.makeToast(context, String.format(
                        context.getResources().getString(R.string.download_canceled), request.fileName));
            } else {
                DialogUtility.makeToast(context, context.getString(R.string.download_canceled_generic));
            }
        }

        listener.onDownloadCancel(request);
    }

    public void showToast(boolean showCancelToast) {
        this.showCancelToast = showCancelToast;
    }
}
