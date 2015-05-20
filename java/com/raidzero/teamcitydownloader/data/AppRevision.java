package com.raidzero.teamcitydownloader.data;

import java.util.ArrayList;

/**
 * Created by raidzero on 8/14/14.
 */
public class AppRevision {
    private static final String tag = "AppRevision";

    private String versionName;
    private String date;
    private ArrayList<String> changes;

    public AppRevision(String versionName, String date, ArrayList<String> changes) {
        this.versionName = versionName;
        this.date = date;
        this.changes = changes;
    }

    public String getVersionName() {
        return versionName;
    }

    public String getDate() {
        return date;
    }

    public ArrayList<String> getChanges() {
        return changes;
    }
}
