package com.raidzero.teamcitydownloader.global;

import android.util.Log;

import com.raidzero.teamcitydownloader.BuildConfig;

/**
 * Created by posborn on 6/28/14.
 */
public class Debug {
    //private static final boolean debug = false;
    //private static final boolean debug = true;

    private static final boolean debug = BuildConfig.DEBUG;

    public static void Log(String tag, String msg) {
        if (debug) {
            Log.d(tag, msg);
        }
    }

    public static void Log(String tag, String msg, Exception e) {
        if (debug) {
            Log.d(tag, msg);
            e.printStackTrace();
        }
    }
}
