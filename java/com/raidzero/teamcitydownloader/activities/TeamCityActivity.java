package com.raidzero.teamcitydownloader.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.raidzero.teamcitydownloader.R;
import com.raidzero.teamcitydownloader.data.TeamCityServer;
import com.raidzero.teamcitydownloader.data.WebResponse;
import com.raidzero.teamcitydownloader.global.AppHelper;
import com.raidzero.teamcitydownloader.global.Debug;
import com.raidzero.teamcitydownloader.global.DialogUtility;
import com.raidzero.teamcitydownloader.global.QueryUtility;
import com.raidzero.teamcitydownloader.global.ThemeUtility;

import java.util.ArrayList;

/**
 * Created by posborn on 8/4/14.
 */
public abstract class TeamCityActivity extends ActionBarActivity implements QueryUtility.QueryCallbacks {
    protected static final String tag = "TeamCityActivity";

    protected String activity_title;

    protected AppHelper helper;
    protected SharedPreferences server_prefs;
    protected Intent intent;

    protected MenuItem refreshItem;
    protected View refreshActionView;
    protected boolean spinning = false;

    protected TeamCityServer server;
    protected String serverAddress;
    protected ActionBar actionBar;

    protected String authStr;
    protected static ArrayList<String> titleContents = new ArrayList<String>();

    protected boolean doExitAnimation = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ThemeUtility.setAppTheme(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        intent = getIntent();

        helper = (AppHelper) getApplication();
        server_prefs = PreferenceManager.getDefaultSharedPreferences(this);

        actionBar = getSupportActionBar();

        server = helper.getTcServer();
        if (server == null) {
            TeamCityServer.serverMisconfigured(this);
        } else {
            authStr = server.getAuthStr();
            serverAddress = server.getServerAddress();
        }
    }

    // interface methods
    public void setActivityTitle(final String title) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                actionBar.setTitle(title);
            }
        });
    }

    public static void addToTitleBar(String s) {
        if (!titleContents.contains(s)) {
            titleContents.add(s);
        }
    }

    public static String getTitleBar(int depth) {
        StringBuilder sb = new StringBuilder();

        int i = 0;
        for (String s : titleContents) {
            if (i < depth || depth == -1) {
                sb.append(s).append("> ");
            } else {
                break;
            }
            i++;
        }

        // now kill off everything in titleContents past i
        trimTitleBar(i);
        return sb.toString();
    }

    private static void trimTitleBar(int index) {
        int size = titleContents.size();

        for (int i = index + 1; i < size; i++) {
            try {
                titleContents.remove(i);
            } catch (IndexOutOfBoundsException e) {
                break;
            }
        }
    }

    public static void popTitleBar() {
        if (titleContents.size() > 0) {
            titleContents.remove(titleContents.size() - 1);
        }
    }

    public static void clearTitleBar() {
        titleContents.clear();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            Debug.Log(tag, "Up button clicked");
            onBackPressed();
        }

        return super.onOptionsItemSelected(menuItem);
    }


    public void startSpinning() {
        if (refreshItem != null && !spinning) {
            Debug.Log(tag, "startSpinning()");

            LayoutInflater inflater = (LayoutInflater) this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            int refreshId;

            if (ThemeUtility.getThemeId(this) == R.style.Light) {
                refreshId = R.layout.action_refresh_light;
            } else {
                refreshId = R.layout.action_refresh_dark;
            }

            Debug.Log(tag, "refreshId: " + String.format("%xd", refreshId));

            final ImageView iv = (ImageView) inflater.inflate(refreshId,
                    null);

            Debug.Log(tag, "iv null? " + (iv == null));

            Animation rotation = AnimationUtils.loadAnimation(getApplication(),
                    R.anim.refresh_rotate);
            rotation.setRepeatCount(Animation.INFINITE);

            iv.startAnimation(rotation);
            iv.setClickable(false);

            refreshActionView = iv;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    refreshItem.setActionView(iv);

                    if (refreshItem.getActionView() != null) {
                        spinning = true;
                    }
                }
            });

        } else {
            Debug.Log(tag, "refreshItem is null");
        }
    }

    public void stopSpinning() {
        Debug.Log(tag, "stopSpinning called");
        if (refreshItem != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Debug.Log(tag, "RefreshItem: " + refreshItem.toString());
                    View actionView = refreshActionView;
                    if (actionView != null) {
                        Debug.Log(tag, "clearing animation");
                        actionView.clearAnimation();
                    } else {
                        Debug.Log(tag, "actionView null");
                    }
                    refreshItem.setActionView(null);
                }
            });
            spinning = false;
        } else {
            Debug.Log(tag, "refreshItem null");
        }
    }

    protected void showAlert(final String title, final String msg, final boolean showOk) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (showOk) {
                    DialogUtility.showAlertWithButton(TeamCityActivity.this, title, msg, okListener);
                } else {
                    DialogUtility.showAlert(TeamCityActivity.this, title, msg);
                }
            }
        });
    }

    protected void updateList(String title, final ArrayAdapter adapter, final ArrayList<?> content) {
        setActivityTitle(title);

        runOnUiThread(new Runnable() {
            @Override
            public void run () {
                adapter.clear();
                adapter.addAll(content);
                Debug.Log(tag, "added " + content.size() +  " items to adapter");
                adapter.notifyDataSetChanged();
            }
        });
    }

    protected void hideView(final View v) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                v.setVisibility(View.GONE);
            }
        });
    }

    protected void showView(final View v) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                v.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onQueryComplete(WebResponse response) {
        setActivityTitle(activity_title);
        if (response != null && response.getResponseDocument() != null) {
            // cache it
            Debug.Log(tag, "caching response for " + response.getRequestUrl());
            helper.addResponseToCache(response.getRequestUrl(), response.getResponseDocument());
        }
    }

    @Override
    public void onBackPressed() {
        popTitleBar();
        setResult(RESULT_CANCELED);
        finish();
        if (doExitAnimation) {
            overridePendingTransition(R.anim.left_in, R.anim.right_out);
        }
    }

    DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            // this is only used to show errors, so just finish when acknowledged
            finish();
        }
    };
}
