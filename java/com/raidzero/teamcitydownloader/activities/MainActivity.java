package com.raidzero.teamcitydownloader.activities;

import android.app.AlarmManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;

import com.raidzero.teamcitydownloader.R;
import com.raidzero.teamcitydownloader.activities.settings.SettingsMainActivity;
import com.raidzero.teamcitydownloader.data.NavigationMenuItem;
import com.raidzero.teamcitydownloader.data.TeamCityItem;
import com.raidzero.teamcitydownloader.fragments.NavigationDrawerFragment;
import com.raidzero.teamcitydownloader.fragments.ProjectsListFragment;
import com.raidzero.teamcitydownloader.global.AppHelper;
import com.raidzero.teamcitydownloader.global.Common;
import com.raidzero.teamcitydownloader.global.Debug;
import com.raidzero.teamcitydownloader.global.DialogUtility;
import com.raidzero.teamcitydownloader.global.ThemeUtility;
import com.raidzero.teamcitydownloader.services.NotificationService;

public class MainActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private static final String tag = "MainActivity";

    public static AppHelper helper;

    private long lastBackPressTime = 0;

    private NavigationDrawerFragment mNavigationDrawerFragment;

    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtility.setAppTheme(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        int holoDialogTheme = ThemeUtility.getHoloDialogTheme(this);

        helper = ((AppHelper) getApplication());
        helper.setDialogTheme(holoDialogTheme);

        int versionCode = getVersionCode();
        helper.setCurrentVersionCode(versionCode);

        // first run?
        if (helper.isFirstRun()) {
            Debug.Log(tag, "first run");
            startActivity(new Intent(this, WelcomeActivity.class));
        }

        //Fragment managing the behaviors, interactions and presentation of the navigation drawer.
         mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        showHomeFragment();

    }

    @Override
    public void onResume() {
        ThemeUtility.setAppTheme(this);

        try {
            boolean isNonPlayAppAllowed = Settings.Secure.getInt(getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS) == 1;
            if (!isNonPlayAppAllowed) {
                Intent i = new Intent(this, UnknownSourcesActivity.class);
                startActivityForResult(i, Common.REQUEST_CODE_EXIT_APP);
            }
        } catch (Settings.SettingNotFoundException e) {
            // do nothing
        }

        // set up pending intent
        Intent notificationServiceIntent = new Intent(this, NotificationService.class);
        //startService(notificationServiceIntent);

        pendingIntent = PendingIntent.getService(this, 0, notificationServiceIntent, 0);
        alarmManager.cancel(pendingIntent);

        // set alarm for notifications if desired
        if (helper.getBoolPref("pref_enable_notifications")) {
            // get interval
            long interval = helper.getCheckInterval();
            Debug.Log(tag, "alarm interval: " + interval);

            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + interval, interval, pendingIntent);
        }

        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Debug.Log(tag, "onActivityResult(" + requestCode + ", " + resultCode + ")");

        if (requestCode == Common.REQUEST_CODE_EXIT_APP) {
            if (resultCode == RESULT_CANCELED) {
                finish();
            }
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(NavigationMenuItem item) {
        if (item != null && item.getName() != null) {

            if (item.getName().equalsIgnoreCase(getString(R.string.your_projects))) {
                showHomeFragment();
            } else if (item.getName().equalsIgnoreCase(getString(R.string.settings_main_title))) {
                Intent settingsIntent = new Intent(this, SettingsMainActivity.class);
                startActivity(settingsIntent);
            } else if (item.getName().equalsIgnoreCase(getString(R.string.action_downloaded_files))) {
                Intent viewDownloadsIntent = new Intent(this, DownloadsListActivity.class);
                startActivity(viewDownloadsIntent);
            } else if (item.getName().equalsIgnoreCase(getString(R.string.title_about_server))) {
                Intent aboutServerIntent = new Intent(this, AboutServerActivity.class);
                startActivity(aboutServerIntent);
            } else if (item.getName().equalsIgnoreCase(getString(R.string.action_open_welcome))) {
                Intent welcomeIntent = new Intent(this, WelcomeActivity.class);
                welcomeIntent.putExtra("showQuickStart", true);
                startActivity(welcomeIntent);
            } else {
                // favorite item
                TeamCityItem tcItem = (TeamCityItem) item.getObj();
                Intent i = new Intent(this, ControllerActivity.class);
                i.putExtra("gotoStarred", true);
                i.putExtra("selectedBuildConfig", tcItem);
                startActivity(i);
            }
        } else {
            Debug.Log(tag, "Null item clicked");
        }
    }

    @Override
    public void onBackPressed() {
        if (mNavigationDrawerFragment.isDrawerOpen()) {
            mNavigationDrawerFragment.hideDrawer();
            return;
        }

        if (isHomeFragment()) {
            // toast already home. one more press within 2 secs to exit
            if (lastBackPressTime < (System.currentTimeMillis() - 2000)) {
                lastBackPressTime = System.currentTimeMillis();
                DialogUtility.makeToast(this, getResources().getString(R.string.main_back_to_exit));
            } else {
                helper.clearResponseCache();
                finish();
            }
        } else {
            super.onBackPressed();
        }
    }

    private int getVersionCode() {
        int versionCode = 0;

        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionCode = packageInfo.versionCode;
        } catch (Exception e) {
            Debug.Log(tag, "Couldn't get package info", e);
        }

        return versionCode;
    }

    public void showHomeFragment() {
        Fragment displayFragment = new ProjectsListFragment();

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, displayFragment, "HOME_FRAGMENT")
                .commit();

        setActionBarTitle(getResources().getString(R.string.title_main));
    }

    private boolean isHomeFragment() {
        Fragment f = getFragmentManager().findFragmentByTag("HOME_FRAGMENT");
        return (f.isVisible());
    }

    // used by fragments to set navigation drawer selection highlight
    public void setNavItemSelection(int position) {
        if (mNavigationDrawerFragment != null) {
            mNavigationDrawerFragment.setSelectedItem(position);
        }
    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent e) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {

            mNavigationDrawerFragment.showDrawer();
            return true;
        }

        return super.onKeyDown(keyCode, e);
    }
}
