package com.raidzero.teamcitydownloader.global;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Created by raidzero on 8/15/14.
 */
public class BrowserUtility {
    private static final String tag = "BrowserUtility";

    public static final void startBrowser(Context context, String url) {
        Debug.Log(tag, "starting browser with url: " + url);
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(browserIntent);
    }
}
