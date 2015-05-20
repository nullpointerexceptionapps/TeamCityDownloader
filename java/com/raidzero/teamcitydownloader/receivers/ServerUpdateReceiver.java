package com.raidzero.teamcitydownloader.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.raidzero.teamcitydownloader.global.AppHelper;
import com.raidzero.teamcitydownloader.global.Common;
import com.raidzero.teamcitydownloader.global.Debug;

/**
 * Created by raidzero on 7/6/14.
 */
public class ServerUpdateReceiver extends BroadcastReceiver {
    private static final String tag = "ServerUpdateReceiver";

    private AppHelper helper = Common.getApphelper();

    public void onReceive(Context context, Intent intent) {
        Debug.Log(tag, "onReceive(): " + intent.getAction());

        if (helper == null) {
            helper = (AppHelper) context.getApplicationContext();
        }

        helper.attemptToLoadServerFromDisk();
    }
}
