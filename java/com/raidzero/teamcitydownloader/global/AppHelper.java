package com.raidzero.teamcitydownloader.global;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.raidzero.teamcitydownloader.R;
import com.raidzero.teamcitydownloader.data.DownloadRequest;
import com.raidzero.teamcitydownloader.data.NavigationMenuItem;
import com.raidzero.teamcitydownloader.data.TeamCityItem;
import com.raidzero.teamcitydownloader.data.TeamCityProject;
import com.raidzero.teamcitydownloader.data.TeamCityServer;
import com.raidzero.teamcitydownloader.data.WebResponse;
import com.raidzero.teamcitydownloader.fragments.NavigationDrawerFragment;
import com.raidzero.teamcitydownloader.services.DownloadService;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by posborn on 7/4/14.
 */
public class AppHelper extends Application {
    private static final String tag = "Application";
    private static final String starredConfigsFile = "starredConfigs.txt";

    private ArrayList<TeamCityItem> starred_configs = new ArrayList<TeamCityItem>();
    private ArrayList<NavigationMenuItem> navigationItems = new ArrayList<NavigationMenuItem>();

    private NavigationDrawerFragment navFragment;
    private int dialogThemeId;

    private SharedPreferences shared_prefs;
    private TeamCityServer teamCityServer;

    private SharedPreferences favoriteBuildIds;
    private SharedPreferences.Editor favoriteBuildIdsEditor;

    public SharedPreferences responseMap;

    private static final String savedProjectsFile = "savedProjects.txt";
    private ArrayList<TeamCityProject> saved_projects = new ArrayList<TeamCityProject>();

    private PackageInfo packageInfo;
    private int currentVersionCode;

    private String storageDirectory;

    private DownloadService downloadService;
    private Intent serviceIntent;
    boolean isBound = false;

    private DownloadHelper downloadHelper;

    @Override
    public void onCreate() {
        super.onCreate();

        shared_prefs = PreferenceManager.getDefaultSharedPreferences(this);
        favoriteBuildIds = getSharedPreferences("favoriteBuildIds", Context.MODE_PRIVATE);
        responseMap = getSharedPreferences("webResponses", Context.MODE_PRIVATE);

        favoriteBuildIdsEditor = favoriteBuildIds.edit();


        storageDirectory = getExternalFilesDir(null) + "/" + Environment.DIRECTORY_DOWNLOADS;

        // clear the cache
        clearResponseCache();

        attemptToLoadServerFromDisk();

        // load starred configs
        starred_configs = getStarredConfigs();

        // update nav items
        updateNavItems();
    }


    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public void setCurrentVersionCode(int versionCode) {
        this.currentVersionCode = versionCode;
        Debug.Log(tag, "set versionCode: " + versionCode);
    }

    public int getVersionCode() {
        return currentVersionCode;
    }

    public boolean isFirstRun() {
        int saved_versionCode = shared_prefs.getInt("version_code", 0);
        Debug.Log(tag, "savedVersionCode: " + saved_versionCode);

        return saved_versionCode != currentVersionCode;
    }

    public boolean isFirstRunEver() {
        int saved_versionCode = shared_prefs.getInt("version_code", 0);
        return saved_versionCode == 0;
    }

    public void writePref(String key, boolean value) {
        SharedPreferences.Editor editor = shared_prefs.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public void writePref(String key, String value) {
        SharedPreferences.Editor editor = shared_prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void writePref(String key, int value) {
        SharedPreferences.Editor editor = shared_prefs.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public String getStringPref(String key) {
        return shared_prefs.getString(key, "");
    }

    public String getStringPref(String key, String defaultValue) {
        return shared_prefs.getString(key, defaultValue);
    }

    public boolean getBoolPref(String key) {
        return shared_prefs.getBoolean(key, false);
    }

    public void setNavFragment(NavigationDrawerFragment fragment) {
        this.navFragment = fragment;
    }

    public void updateNavItems() {
        // add main nav items to nav objects
        ArrayList<NavigationMenuItem> newNavigationItems = new ArrayList<NavigationMenuItem>();

        newNavigationItems.add(new NavigationMenuItem(String.class, getString(R.string.your_projects)));

        // add downloads item if files are present
        if (FileUtility.filesExist(storageDirectory)) {
            newNavigationItems.add(new NavigationMenuItem(String.class, getString(R.string.action_downloaded_files)));
        }

        // add server info if server is available
        if (teamCityServer != null) {
            newNavigationItems.add(new NavigationMenuItem(String.class, getString(R.string.title_about_server)));
        }

        newNavigationItems.add(new NavigationMenuItem(String.class, getString(R.string.settings_main_title)));
        newNavigationItems.add(new NavigationMenuItem(String.class, getString(R.string.action_open_welcome)));

        // do we have any favorites?
        starred_configs = getStarredConfigs();

        if (starred_configs.size() > 0) {
            // add a divider and then the favorites
            NavigationMenuItem divider = new NavigationMenuItem(null, "Favorites");
            divider.setDivider(true);
            newNavigationItems.add(divider);

            // now add the starred configs
            for (TeamCityItem starred : starred_configs) {
                NavigationMenuItem favItem = new NavigationMenuItem(starred, starred.getName());
                favItem.setFavorite(true);
                newNavigationItems.add(favItem);
                Debug.Log(tag, "added favorite: " + favItem.getName());
            }
        }

        Debug.Log(tag, "getNavItems loaded " + newNavigationItems.size() + " items.");

        navigationItems = newNavigationItems;
    }

    public ArrayList<NavigationMenuItem> getNavItems() {
        return navigationItems;
    }

    public int getDialogTheme() {
        return dialogThemeId;
    }

    public void setDialogTheme(int themeId) {
        this.dialogThemeId = themeId;
    }

    public void addFavorite(TeamCityItem buildConfig) {
        if (!isConfigStarred(buildConfig)) {
            starred_configs.add(buildConfig);
        }
        writeStarredConfigsFile();
    }

    public void removeFavorite(TeamCityItem buildConfig) {
        Debug.Log(tag, "removeFavorite()");
        starred_configs = getStarredConfigs();

        if (isConfigStarred(buildConfig)) {
            removeStarredConfig(buildConfig);
            Debug.Log(tag, "removed favorite: " + buildConfig.getName());
        }
        writeStarredConfigsFile();
    }

    private void removeStarredConfig(TeamCityItem buildConfig) {
        int i = 0;
        boolean found = false;
        for (TeamCityItem item : starred_configs) {
            if (item.getUrl().equals(buildConfig.getUrl())) {
                found = true;
                break;
            }
            i++;
        }

        if (found) {
            starred_configs.remove(i);
            removeFavoriteBuildId(buildConfig.getUrl());
        }
    }

    public boolean isConfigStarred(TeamCityItem config) {
        if (config != null) {
            Debug.Log(tag, "isConfigStarred()? " + config.getName());
            starred_configs = getStarredConfigs();

            for (TeamCityItem item : starred_configs) {
                if (item.getUrl().equals(config.getUrl())) {
                    Debug.Log(tag, "yes");
                    return true;
                }
            }
            Debug.Log(tag, "np");
        }
        return false;
    }

    public ArrayList<TeamCityItem> getStarredConfigs() {

        if (starred_configs.size() == 0) {
            starred_configs = loadStarredConfigsFromFile();
        }

        Debug.Log(tag, "returning " + starred_configs.size() + " starred configs");
        return starred_configs;
    }

    private void writeStarredConfigsFile() {
        try {
            FileOutputStream fos = openFileOutput(starredConfigsFile, Context.MODE_PRIVATE);

            for (TeamCityItem item : starred_configs) {
                Debug.Log(tag, "writeStarredConfigsFile() writing " + item.getName());
                String data = item.serialize();
                fos.write((data + "\n").getBytes());
            }

            fos.close();
        } catch (Exception e) {
            Debug.Log(tag, "writeStarredConfigsFile error.", e);
        }
        updateNavItems();
        navFragment.updateNavItemsAdapter();
    }

    public ArrayList<TeamCityItem> loadStarredConfigsFromFile() {
        ArrayList<TeamCityItem> configs = new ArrayList<TeamCityItem>();

        try {
            FileInputStream fis = openFileInput(starredConfigsFile);

            InputStreamReader inputreader = new InputStreamReader(fis);
            BufferedReader buffreader = new BufferedReader(inputreader);

            String line;

            do {
                line = buffreader.readLine();
                if (line != null && !line.isEmpty()) {
                    TeamCityItem item = TeamCityItem.deserialize(line);
                    if (item != null) {
                        configs.add(item);
                    }
                }
            } while (line != null);

        } catch (Exception e) {
            // ignore
        }

        return configs;
    }

    public void refreshNavItems() {
        updateNavItems();
        navFragment.updateNavItemsAdapter();
    }

    public void attemptToLoadServerFromDisk() {
        // make one from preferences
        String serverAddress, username, password;
        Boolean useGuest;

        serverAddress = shared_prefs.getString("pref_server_address", "");

        Debug.Log(tag, "server being created from preferences: " + serverAddress );
        if (serverAddress.isEmpty()) {
            teamCityServer = null;
            return;
        }

        teamCityServer = new TeamCityServer(serverAddress);

        useGuest = shared_prefs.getBoolean("pref_enable_guest", false);

        if (!useGuest) {
            // try to get username and password
            username = shared_prefs.getString("pref_server_username", "");
            password = shared_prefs.getString("pref_server_password", "");

            Debug.Log(tag, "server being created from preferences: " + username );
            if (username.isEmpty() || password.isEmpty()) {
                Debug.Log(tag , "no credentials...");
                teamCityServer = null;
                return;
            }

            teamCityServer.setLoginData(username, password);

        } else {
            teamCityServer.setLoginData(useGuest);
        }

        if (navFragment != null) {
            navFragment.updateNavItemsAdapter();
        }
    }

    public TeamCityServer getTcServer() {
        return teamCityServer;
    }

    public void clearRequest(String url) {
        SharedPreferences.Editor editor = responseMap.edit();
        if (responseMap.contains(url)) {
            editor.remove(url);
            editor.apply();
        }
    }

    public void clearResponseCache() {
        SharedPreferences.Editor editor = responseMap.edit();
        editor.clear();
        editor.apply();
    }

    public void addResponseToCache(String requestUrl, String responseString) {
        requestUrl = requestUrl.replaceAll("^.*/(http|guest)Auth/", "");

        SharedPreferences.Editor editor = responseMap.edit();

        editor.putString(requestUrl, responseString);
        editor.apply();

        Debug.Log(tag, "added to cache: \"" + requestUrl + "\"");
    }

    public WebResponse getResponseFromCache(String requestUrl) {

        WebResponse rtn = null;
        requestUrl = requestUrl.replaceAll("^.*/(http|guest)Auth/", "");

        if (teamCityServer != null) {
            //String fullRequestUrl = teamCityServer.getServerAddress() + requestUrl;
            String doc = null;
            if (responseMap.contains(requestUrl)) {
                doc = responseMap.getString(requestUrl, "");
            }

            if (doc != null) {
                rtn = new WebResponse(null, requestUrl, doc);
                Debug.Log(tag, "returning cached response for \"" + requestUrl + "\": " + doc);
            } else {
                Debug.Log(tag, "no cached response for: \"" + requestUrl + "\"");
            }
        }
        return rtn;
    }

    public void removeReponseFromCache(String requestUrl) {
        clearRequest(requestUrl);
    }

    private void writeProjectsToFile() {
        try {
            FileOutputStream fos = openFileOutput(savedProjectsFile, Context.MODE_PRIVATE);

            for (TeamCityProject p : saved_projects) {
                String data = p.serialize();
                fos.write((data + "\n").getBytes());
            }

            fos.close();
        } catch (Exception e) {
            Debug.Log(tag, "writeProjectsFile error.");
        }
    }

    private ArrayList<TeamCityProject> loadProjectsFromFile() {
        ArrayList<TeamCityProject> projects = new ArrayList<TeamCityProject>();

        try {
            FileInputStream fis = openFileInput(savedProjectsFile);

            InputStreamReader inputreader = new InputStreamReader(fis);
            BufferedReader buffreader = new BufferedReader(inputreader);

            String line;

            do {
                line = buffreader.readLine();
                if (line != null && !line.isEmpty()) {
                    TeamCityProject p = TeamCityProject.deserialize(line);
                    if (p != null) {
                        projects.add(p);
                    }
                }
            } while (line != null);

        } catch (Exception e) {
            // ignore
        }

        return projects;
    }

    public void setSavedProjects(ArrayList<TeamCityProject> projects) {
        int numProjects = projects.size();
        saved_projects = projects;

        Debug.Log(tag, "got " + numProjects + " projects");

        writeProjectsToFile();
    }

    public ArrayList<TeamCityProject> getSavedProjects() {
        ArrayList<TeamCityProject> rtn;

        rtn = loadProjectsFromFile();

        Debug.Log(tag, "getSavedProjects() returning " + rtn.size() + " projects.");
        return rtn;
    }

    public String getStorageDirectory() {
        return storageDirectory;
    }

    // Service connection stuff
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DownloadService.MyLocalBinder binder = (DownloadService.MyLocalBinder) service;
            downloadService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // this may not get called when disconnecting
            isBound = false;
        }
    };

    public void bindDownloadService() {
        // bind to download service
        if (!isBound) {
            serviceIntent = new Intent(getApplicationContext(), DownloadService.class);
            isBound = getApplicationContext().bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    public void unbindDownloadService() {
        if (isBound && downloadService != null) {
            getApplicationContext().unbindService(serviceConnection);
            isBound = false;
        }
    }

    public void serviceQueueDownload(DownloadRequest request) {
        if (downloadService != null) {
            downloadService.queueDownload(request);
        }
    }

    public void serviceCancelDownload(String fileName) {
        if (downloadHelper != null) {
            downloadHelper.cancelDownload(fileName);
        }
    }

    public void serviceCancelDownload(String fileName, boolean showToast) {
        if (downloadService != null) {
            downloadHelper.cancelDownload(fileName, showToast);
        }
    }

    public void serviceRemoveAllNotifications() {
        if (downloadService != null) {
            downloadService.removeAllNotifications();
        }
    }

    public boolean shouldBoldFile(String fileName, int notificationId) {
        return downloadHelper.shouldBoldFile(fileName, notificationId);
    }

    public void markFileSeen(String file, int notificationId) {
        if (downloadService != null) {
            downloadHelper.markFileSeen(file, notificationId);
        }
    }

    public boolean isFileDownloading(String fileName) {
        if (downloadService != null) {
            return downloadHelper.isFileDownloading(fileName);
        } else {
            return false;
        }
    }

    public DownloadHelper getDownloadHelper() {
        if (downloadHelper == null) {
            downloadHelper = new DownloadHelper(this);
        }

        return downloadHelper;
    }

    public long getCheckInterval() {
        String intervalStr = getStringPref("pref_build_check_interval", "2:0");
        int hours = Integer.valueOf(intervalStr.split(":")[0]);
        int mins = Integer.valueOf(intervalStr.split(":")[1]);

        return (hours * 60 * 60 * 1000) + (mins * 60 * 1000);
    }

    public void removeFavoriteBuildId(String url) {
        String key = url + "/builds";

        if (favoriteBuildIds.getInt(key, -1) != -1) {
            favoriteBuildIdsEditor.remove(key);
            favoriteBuildIdsEditor.apply();
        }
    }

    public void addFavoriteBuildId(String url, int newId) {
        favoriteBuildIdsEditor.putInt(url, newId);
        favoriteBuildIdsEditor.apply();

    }

    public int getLastBuildId(String url) {
        String key = url + "/builds";
        return favoriteBuildIds.getInt(key, -1);
    }

    public TeamCityItem getFavoriteByName(String name) {
        for (TeamCityItem i : starred_configs) {
            if (i.getName().equals(name)) {
                return i;
            }
        }

        return null;
    }
}
