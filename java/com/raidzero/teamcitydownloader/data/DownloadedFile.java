package com.raidzero.teamcitydownloader.data;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.raidzero.teamcitydownloader.global.Debug;
import com.raidzero.teamcitydownloader.global.FileUtility;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * Created by posborn on 11/24/14.
 */
public class DownloadedFile implements Comparable<DownloadedFile> {
    private static final String tag = "DownloadedFile";

    private String filePath;
    private String fileName;
    private Bitmap bmpIcon;
    private Date lastModDate;
    private boolean isNew = false;
    private boolean isDownloading = false;
    private Context context;
    private File f;

    public DownloadedFile(Context context, File f) {
        this.context = context;
        this.f = f;
        filePath = f.getPath();
        lastModDate = new Date(f.lastModified());
        fileName = f.getName();

        updateIcon();
    }

    public void updateIcon() {
        if (isDownloading) {
            bmpIcon = ((BitmapDrawable) context.getResources().getDrawable(android.R.drawable.ic_menu_help)).getBitmap();
            return;
        }

        if (fileName.endsWith(".apk") || fileName.endsWith("APK")) {
            PackageInfo packageInfo = context.getPackageManager().getPackageArchiveInfo(filePath, PackageManager.GET_ACTIVITIES);
            if(packageInfo != null) {
                ApplicationInfo appInfo = packageInfo.applicationInfo;

                appInfo.sourceDir = filePath;
                appInfo.publicSourceDir = filePath;

                Drawable icon = appInfo.loadIcon(context.getPackageManager());
                bmpIcon = ((BitmapDrawable) icon).getBitmap();

                if (bmpIcon.getByteCount() == 0) {
                    bmpIcon = ((BitmapDrawable) context.getResources().getDrawable(android.R.drawable.ic_menu_help)).getBitmap();
                }
            }
        } else {
            // get the default icon for this file type
            final Intent i = new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(Uri.fromFile(f), FileUtility.getFileMimeType(fileName));

            final List<ResolveInfo> matches = context.getPackageManager().queryIntentActivities(i, 0);

            // use the first available app icon
            if (matches.size() > 0) {
                CharSequence label = matches.get(0).loadLabel(context.getPackageManager());
                Debug.Log(tag, "label: " + label);

                final Drawable icon = matches.get(0).loadIcon(context.getPackageManager());
                bmpIcon = ((BitmapDrawable) icon).getBitmap();
            }
        }

        if (bmpIcon == null) {
            // take a question mark
            bmpIcon = ((BitmapDrawable) context.getResources().getDrawable(android.R.drawable.ic_menu_help)).getBitmap();
        }
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public Bitmap getBmpIcon() {
        return bmpIcon;
    }

    public Date getLastModDate() {
        return lastModDate;
    }

    public void setIsNew(boolean n) {
        isNew = n;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setIsDownloading(boolean b) {
        isDownloading = b;
    }

    public boolean isDownloading() {
        return isDownloading;
    }

    public void rename(String newName) {
        fileName = newName;
    }

    @Override
    public int compareTo(DownloadedFile another) {
        return another.getLastModDate().compareTo(this.lastModDate);
    }
}
