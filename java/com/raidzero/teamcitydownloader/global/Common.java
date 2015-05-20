package com.raidzero.teamcitydownloader.global;

import com.raidzero.teamcitydownloader.activities.MainActivity;

/**
 * Created by raidzero on 7/6/14.
 */
public class Common {
    public static AppHelper appHelper;
    public static String appName = "TeamCity Downloader";
    public static String gitHubUrl = "https://github.com/nullpointerexceptionapps/TeamCityDownloader";
    public static String emailAddress = "null.pointer.exception.apps@gmail.com";

    public static final String INTENT_SERVER_UPDATE = "com.raidzero.teamcitydownloader.SERVER_UPDATE";

    public static final int REQUEST_CODE_EXIT_APP = 1000;

    public static final int REQUEST_CODE_LIST_BUILD_CONFIGS = 1001;
    public static final int REQUEST_CODE_LIST_BUILDS = 1002;
    public static final int REQUEST_CODE_LIST_INFO = 1003;
    public static final int REQUEST_CODE_LIST_ARTIFACTS = 1004;

    public static final int WELCOME_TAB_QUICK_START = 0;
    public static final int WELCOME_TAB_WHATS_NEW = 1;

    public static AppHelper getApphelper() {
        if (MainActivity.helper != null) {
            return MainActivity.helper;
        } else {
            return appHelper;
        }
    }
}
