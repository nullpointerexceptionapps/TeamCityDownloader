package com.raidzero.teamcitydownloader.global;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import java.io.File;

/**
 * Created by posborn on 6/28/14.
 */
public class FileUtility {

    private static final String tag = "FileUtility";
    public static void openFile(Context context, String fileName) {

        Debug.Log(tag, "openFile(" + fileName + ")...");

        // determine file type
        String type = getFileMimeType(fileName);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(new File(fileName));

        if (type == null) {
            intent.setData(uri);
        } else {
            intent.setDataAndType(uri, type);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            context.startActivity(intent);
        } catch (Exception e) {
            DialogUtility.makeToast(context, "Unsupported file type: " + getExtensionFromFile(fileName));
            Debug.Log(tag, "File error", e);
        }
    }

    public static boolean handlerExists(Context context, String filename) {
        PackageManager pm = context.getPackageManager();
        Intent i = new Intent(android.content.Intent.ACTION_VIEW);

        String mimeType = getFileMimeType(filename);
        i.setDataAndType(Uri.fromFile(new File(filename)), mimeType);
        return i.resolveActivity(pm) != null;
    }

    public static String getExtensionFromFile(String filename) {
        return MimeTypeMap.getFileExtensionFromUrl(filename);
    }

    public static String getFileMimeType(String filename) {

        String ext = getExtensionFromFile(filename);

        Debug.Log(tag, "file extension: " + ext);

        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String mimeType = mime.getMimeTypeFromExtension(ext);
        Debug.Log(tag, "returning mime type: " + mimeType);
        return mimeType;
    }

    public static boolean filesExist(String path) {
        File storage = new File(path);
        File[] files = storage.listFiles();
        if (files != null) {
            return files.length > 0;
        }

        return false;
    }

    public static int deleteAllFiles(String path) {
        File storage = new File(path);
        File[] files = storage.listFiles();

        int count = 0;
        for (File f : files) {
            f.delete();
            count++;
        }

        return count;
    }

    public static void deleteFile(String path) {
        Debug.Log(tag, "deleteFile: " + path);
        File f = new File(path);
        f.delete();
    }

    public static void renameFile(String fromPath, String toPath) {
        Debug.Log(tag, "renameFile(" + fromPath + ", " + toPath + ")");
        File from = new File(fromPath);
        File to = new File(toPath);

        from.renameTo(to);
    }

}
