package com.raidzero.teamcitydownloader.global;

import android.content.Context;
import android.os.FileObserver;

/**
 * Created by raidzero on 11/29/14.
 */
public class DownloadObserver extends FileObserver{
    private static final String tag = "DownloadObserver";
    private String path;
    private DownloadObserverListener listener;

    public interface DownloadObserverListener {
        void onFileCreated(String path);
        void onFileClosed(String path);
    }

    public DownloadObserver(Context context, String pathToWatch) {
        super(pathToWatch, FileObserver.CREATE | FileObserver.CLOSE_WRITE);
        listener = (DownloadObserverListener) context;
        path = pathToWatch;
    }

    @Override
    public void onEvent(int event, String path) {
        Debug.Log(tag, String.format("onEvent(%d, %s)", event, path));

        if (path != null) {
            if ((FileObserver.CREATE & event)!=0) {
                listener.onFileCreated(path);
            }

            if ((FileObserver.CLOSE_WRITE & event)!=0) {
                listener.onFileClosed(path);
            }
        }

    }
}
