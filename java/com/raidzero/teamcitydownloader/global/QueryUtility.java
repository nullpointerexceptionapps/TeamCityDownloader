package com.raidzero.teamcitydownloader.global;

import android.content.Context;
import android.os.AsyncTask;

import com.raidzero.teamcitydownloader.R;
import com.raidzero.teamcitydownloader.activities.settings.SettingsServerActivity;
import com.raidzero.teamcitydownloader.data.TeamCityServer;
import com.raidzero.teamcitydownloader.data.WebResponse;
import com.raidzero.teamcitydownloader.tasks.TeamCityTask;

/**
 * Created by posborn on 8/4/14.
 */
public class QueryUtility implements TeamCityTask.OnWebRequestCompleteListener {
    private static final String tag = "QueryUtility";
    private Context context;
    private AppHelper helper;
    private String query;
    private TeamCityServer server;
    private QueryCallbacks callbacks;
    private boolean isQuerying;
    private boolean onSettingsScreen;

    // interface
    public interface QueryCallbacks {
        void onQueryComplete(WebResponse response);
        void startSpinning();
        void stopSpinning();
        void setActivityTitle(String title);
    }

    // constructor
    public QueryUtility(Context context, String query) {
        this.context = context;
        this.query = query;

        helper = (AppHelper) context.getApplicationContext();
        server = helper.getTcServer();

        callbacks = (QueryCallbacks) context;
    }

    public boolean isQuerying() {
        return isQuerying;
    }

    public void setQuery(String url) {
        this.query = url;
    }

    public void setOnSettingsScreen(boolean b) {
        onSettingsScreen = b;
    }

    public void queryServer(boolean forceRefresh) {
        Debug.Log(tag, "queryServer(" + query + ")...");

        // refresh button
        if (forceRefresh) {
            Debug.Log(tag, "forcing query for " + query);
            // clear this cached request
            helper.removeReponseFromCache(query);

            runTask(query);
            return;
        }

        // see if we have cached this request
        WebResponse cachedResponse = helper.getResponseFromCache(query);

        if (cachedResponse != null) {
            if (cachedResponse.getResponseDocument() != null) {
                Debug.Log(tag, "got cached response for queryUrl " + query);

                // invoke listener with cached response
                onWebRequestComplete(cachedResponse);
                return;
            }
        }

        // if we got here there is no cached response
        runTask(query);
    }

    private void runTask(String queryUrl) {
        isQuerying = true;
        Debug.Log(tag, "runTask on query " + queryUrl);
        Debug.Log(tag, "server is null? " + (server == null));
        callbacks.startSpinning();
        callbacks.setActivityTitle(context.getString(R.string.querying));

        if (server != null) {
            // run task
            server.setContext(this);
            server.setOnSettingsScreen(onSettingsScreen);
            TeamCityTask task = server.getRequestTask(queryUrl);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            TeamCityServer.serverMisconfigured(context, onSettingsScreen);
            if (onSettingsScreen) {
                SettingsServerActivity.querying = false;
            }

            callbacks.stopSpinning();
        }
    }

    @Override
    public void onWebRequestComplete(WebResponse response) {
        Debug.Log(tag, "onWebRequestComplete");
        callbacks.stopSpinning();
        isQuerying = false;
        callbacks.onQueryComplete(response);
    }
}
