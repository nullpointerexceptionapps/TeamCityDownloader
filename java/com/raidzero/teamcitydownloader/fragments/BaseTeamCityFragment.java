package com.raidzero.teamcitydownloader.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import com.raidzero.teamcitydownloader.R;
import com.raidzero.teamcitydownloader.data.TeamCityProject;
import com.raidzero.teamcitydownloader.data.TeamCityServer;
import com.raidzero.teamcitydownloader.data.WebResponse;
import com.raidzero.teamcitydownloader.global.AppHelper;
import com.raidzero.teamcitydownloader.global.Debug;
import com.raidzero.teamcitydownloader.tasks.TeamCityTask;

import java.util.ArrayList;

/**
 * Created by posborn on 6/28/14.
 */
public class BaseTeamCityFragment extends Fragment implements TeamCityTask.OnWebRequestCompleteListener {

    private static final String tag = "BaseFragment";
    protected String fragment_title = "";

    protected SharedPreferences server_prefs;

    protected AppHelper helper;

    // server params
    protected String serverAddress;
    protected String authStr;

    private String currentQuery;
    private Fragment currentFragment;
    protected TeamCityServer server;

    protected Activity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        activity = getActivity();
        helper = ((AppHelper) activity.getApplication());

        server_prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        this.server = getTcServer();

        if (server != null) {
            this.serverAddress = server.getServerAddress();
            this.authStr = server.getAuthStr();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Debug.Log(tag, "optionsItemSelected: " + item.getItemId() + " refresh: " + R.id.action_refresh);

        if (item.getItemId() == R.id.action_refresh) {
            queryServer(currentQuery, currentFragment, true);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void queryServer(String query, Fragment listener, boolean forceRefresh) {
        Debug.Log(tag, "queryServer(" + query + ")...");

        // save for the refresh button :)
        currentQuery = query;
        currentFragment = listener;

        // refresh button
        if (forceRefresh) {
            Debug.Log(tag, "forcing query for " + query);
            // clear this cached request
            clearResponseFromCache(query);

            runTask(query, listener);
            return;
        }

        // see if we have cached this request
        WebResponse cachedResponse = getResponseFromCache(query);

        if (cachedResponse != null) {
            if (cachedResponse.getResponseDocument() != null) {
                Debug.Log(tag, "got cached response for queryUrl " + query);

                // invoke listener with cached response
                onWebRequestComplete(cachedResponse);
                return;
            }
        }

        // if we got here there is no cached response
        runTask(query, listener);
    }

    private void runTask(String queryUrl, Fragment listener) {
        Debug.Log(tag, "runTask on query " + queryUrl);
        Debug.Log(tag, "server is null? " + (server == null));
        if (server != null) {
            // set context
            server.setContext(getActivity());

            // run task
            TeamCityTask task = server.getRequestTask(queryUrl);
            Debug.Log(tag, "setting active task for query " + queryUrl);
            task.execute();
        } else {
            serverMisconfigured();
        }
    }

    @Override
    public void onWebRequestComplete(WebResponse response) {

        if (response != null) {
            if (response.getResponseDocument() != null) {
                addResponseToCache(response.getRequestUrl(), response.getResponseDocument());
                Debug.Log(tag, "cached " + response.getRequestUrl() + ": " + response.getResponseDocument());
            }
        }
    }

    protected ArrayList<TeamCityProject> getSavedProjects() {
        return helper.getSavedProjects();
    }

    protected void addResponseToCache(String requestUrl, String responseString) {
        helper.addResponseToCache(requestUrl, responseString);
    }

    protected WebResponse getResponseFromCache(String requestUrl) {
        return helper.getResponseFromCache(requestUrl);
    }

    protected void clearResponseFromCache(String requestUrl) {
        helper.removeReponseFromCache(requestUrl);
    }

    protected TeamCityServer getTcServer() {
        return helper.getTcServer();
    }

    protected void serverMisconfigured() {
        TeamCityServer.serverMisconfigured(activity);
    }

}
