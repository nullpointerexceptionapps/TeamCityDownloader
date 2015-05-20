package com.raidzero.teamcitydownloader.data;

/**
 * Created by posborn on 11/26/14.
 */

// just a simple container for everything a download task needs to start
public class DownloadRequest {
    public String url;
    public String fileName;
    public String authStr;
    public long fileSize;
    public long bytesDownloaded;
    public int notificationId;
}
