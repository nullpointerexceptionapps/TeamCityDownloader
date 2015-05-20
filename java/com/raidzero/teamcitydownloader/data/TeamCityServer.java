package com.raidzero.teamcitydownloader.data;

import android.content.Context;
import android.content.Intent;

import com.raidzero.teamcitydownloader.R;
import com.raidzero.teamcitydownloader.activities.ConfigErrorActivity;
import com.raidzero.teamcitydownloader.global.AppHelper;
import com.raidzero.teamcitydownloader.global.Debug;
import com.raidzero.teamcitydownloader.global.QueryUtility;
import com.raidzero.teamcitydownloader.global.Regex;
import com.raidzero.teamcitydownloader.tasks.TeamCityTask;

import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;

import java.util.ArrayList;

/**
 * Created by posborn on 7/4/14.
 */
public class TeamCityServer {
    private static final String tag = "TeamCityServer";

    private String serverAddress;
    private String username;
    private String password;
    private String serverInfo;

    private boolean useGuest = false;
    private static boolean onSettingsScreen = false;

    private Context context;
    private QueryUtility queryUtility;

    // constructor only needs server address
    public TeamCityServer(String address) {
        this.serverAddress = address;
    }

    public String getServerAddress() {
        return this.serverAddress;
    }

    public void setServerInfo(String info) {
        this.serverInfo = info;
    }

    public String getServerInfo() {
        return this.serverInfo;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setContext(QueryUtility q) {
        this.queryUtility = q;
    }

    public void setOnSettingsScreen(boolean b) {
        onSettingsScreen = b;
    }

    // this is for setting credentials
    public void setLoginData(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // this one for setting guest mode
    public void setLoginData(boolean useGuest) {
        this.useGuest = useGuest;
    }

    public boolean useGuest() {
        return useGuest;
    }

    public String getAuthStr() {
        if (username != null && password != null) {
            return username + ":" + password;
        } else {
            return null;
        }
    }

    // takes a path - like app/rest/projects
    public TeamCityTask getRequestTask(String path) {
        Debug.Log(tag, "path: " + path);

        String urlRoot = this.serverAddress;
        String authStr = useGuest ? "guestAuth" : "httpAuth";

        // the setContext MUST have been called at this point.
        TeamCityTask task = new TeamCityTask(queryUtility);

        // strip the path of any /httpAuth or /guestAuth
        path = new Regex(path, "^/(http|guest)Auth/").replaceAll("");

        String requestUrl = String.format("%s/%s/%s", urlRoot, authStr, path);

        // if no http prefix was entered just assume http://
        if (!requestUrl.startsWith("http")) {
            requestUrl = "http://" + requestUrl;
        }

        Debug.Log(tag, "requestUrl: " + requestUrl);

        // build httpGet object
        HttpGet httpGet = new HttpGet(requestUrl);

        // add in authentication if needed
        if (!useGuest) {
            httpGet.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials(username, password), "UTF-8", false));
        }

        task.setHttpGet(httpGet, requestUrl);

        // return complete task
        return task;
    }

    public static void serverMisconfigured(Context context, boolean alreadyOnSettings) {
        onSettingsScreen = alreadyOnSettings;
        serverMisconfigured(context);
    }

    public static void serverMisconfigured(Context context) {
        Debug.Log(tag, "config error");
        ArrayList<String> errors = new ArrayList<String>();

        AppHelper helper = (AppHelper) context.getApplicationContext();
        // lets see what the errors are... no server address, no user or password (and guest is disabled)
        String address = helper.getStringPref("pref_server_address");
        String user = helper.getStringPref("pref_server_username");
        String password = helper.getStringPref("pref_server_password");
        Boolean useGuest = helper.getBoolPref("pref_enable_guest");

        if (address.isEmpty()) {
            errors.add(context.getResources().getString(R.string.config_error_no_server));
        }

        if (!useGuest) {
            if (user.isEmpty()) {
                errors.add(context.getResources().getString(R.string.config_error_no_username));
            }
            if (password.isEmpty()) {
                errors.add(context.getResources().getString(R.string.config_error_no_password));
            }
        }

        Intent i = new Intent(context, ConfigErrorActivity.class);
        i.putStringArrayListExtra("errors", errors);
        i.putExtra("onServerScreen", onSettingsScreen);
        context.startActivity(i);
    }

    public String getParentUrl(String url) {
        url = new Regex(url, serverAddress + "/(http|guest)Auth/").replaceAll("");
        return trimUrl(url);
    }

    private String trimUrl(String url) {
        String[] data = url.split("/");
        int pieces = data.length;
        StringBuilder sb = new StringBuilder();

        int i = 0;
        for (String s : data) {
            if (i < pieces - 1) {
                if (i > 0) {
                    sb.append("/");
                }
                sb.append(s);
                Debug.Log(tag, "added " + s);
                i++;
            }
        }

        return sb.toString();
    }
}
