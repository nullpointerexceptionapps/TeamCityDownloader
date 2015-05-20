package com.raidzero.teamcitydownloader.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.raidzero.teamcitydownloader.R;
import com.raidzero.teamcitydownloader.activities.settings.SettingsServerActivity;
import com.raidzero.teamcitydownloader.adapters.WelcomeTabsPagerAdapter;
import com.raidzero.teamcitydownloader.global.AppHelper;
import com.raidzero.teamcitydownloader.global.Common;
import com.raidzero.teamcitydownloader.global.ThemeUtility;

/**
 * Created by raidzero on 7/6/14.
 */
public class WelcomeActivity  extends ActionBarActivity implements
        ActionBar.TabListener, View.OnClickListener {

    private static final String tag = "WelcomeActivity";

    private ViewPager viewPager;
    private WelcomeTabsPagerAdapter tabsAdapter;
    private ActionBar actionBar;
    private Button okButton;
    private Button cancelButton;
    private AppHelper helper;
    private boolean showQuickStart;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeUtility.setAppTheme(this);
        setContentView(R.layout.welcome);
        Intent intent = getIntent();
        showQuickStart = intent.getBooleanExtra("showQuickStart", false);

        helper = Common.getApphelper();

        // Initilization
        viewPager = (ViewPager) findViewById(R.id.pager);
        actionBar = getSupportActionBar();
        tabsAdapter = new WelcomeTabsPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(tabsAdapter);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Adding Tabs - in order they will appear
        actionBar.addTab(actionBar.newTab().setText(getString(R.string.quick_start))
                .setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(getString(R.string.whats_new))
                .setTabListener(this));

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                // on changing the page
                // make respected tab selected
                actionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });

        okButton = (Button) findViewById(R.id.welcome_ok_button);
        cancelButton = (Button) findViewById(R.id.welcome_cancel_button);

        okButton.setOnClickListener(this);

        if (helper.isFirstRun()) {
            cancelButton.setOnClickListener(this);
        } else {
            cancelButton.setVisibility(View.GONE);
            okButton.setGravity(Gravity.CENTER_HORIZONTAL);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!helper.isFirstRunEver()) {
            viewPager.setCurrentItem(Common.WELCOME_TAB_WHATS_NEW);
        }

        // this takes precendence
        if (showQuickStart) {
            viewPager.setCurrentItem(Common.WELCOME_TAB_QUICK_START);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            leave(false);
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onClick(View v) {
        boolean writePref = false;

        if (v.getId() == okButton.getId()) {
            writePref = true;
        }

        leave(writePref);
    }

    private void leave(boolean writePref) {
        if (helper.isFirstRunEver()) {
            helper.writePref("version_code", helper.getVersionCode());
            launchServerConfig();
        } else {
            if (writePref) {
                helper.writePref("version_code", helper.getVersionCode());
            }
        }

        finish();
    }

    public void leave() {
        finish();
    }

    public void launchServerConfig() {
        Intent i = new Intent(this, SettingsServerActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) {
        // nothing
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) {
        // nothing
    }
}
