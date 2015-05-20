package com.raidzero.teamcitydownloader.fragments.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.raidzero.teamcitydownloader.R;
import com.raidzero.teamcitydownloader.global.ThemeUtility;
import com.raidzero.teamcitydownloader.global.TimePreference;

/**
 * Created by posborn on 11/24/14.
 */
public class SettingsFragmentGeneral extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String tag = "SettingsFragmentGeneral";

    PreferenceScreen general_screen;
    CheckBoxPreference pref_multiple_downloads;
    CheckBoxPreference pref_enable_notifications;
    TimePreference pref_build_check_interval;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings_general);

        general_screen = (PreferenceScreen) findPreference(("pref_screen_general"));

        pref_multiple_downloads = (CheckBoxPreference) findPreference("pref_multiple_downloads");

        pref_enable_notifications = (CheckBoxPreference) findPreference("pref_enable_notifications");
        pref_build_check_interval = (TimePreference) findPreference("pref_build_check_interval");

        if (pref_multiple_downloads.isChecked()) {
            pref_multiple_downloads.setSummary(getString(R.string.pref_summary_multiple_downloads_enabled));
        } else {
            pref_multiple_downloads.setSummary(getString(R.string.pref_summary_multiple_downloads_disabled));
        }

        if (pref_enable_notifications.isChecked()) {
            pref_enable_notifications.setSummary(getString(R.string.pref_summary_notifications_enabled));
            general_screen.addPreference(pref_build_check_interval);
        } else {
            pref_enable_notifications.setSummary(getString(R.string.pref_summary_notifications_disabled));
            general_screen.removePreference(pref_build_check_interval);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        // update multiple downloads summary
        if (key.equals("pref_multiple_downloads")) {
            if (sharedPreferences.getBoolean(key, false)) {
                pref_multiple_downloads.setSummary(getString(R.string.pref_summary_multiple_downloads_enabled));
            } else {
                pref_multiple_downloads.setSummary(getString(R.string.pref_summary_multiple_downloads_disabled));
            }
        }

        // update notifications summary
        if (key.equals("pref_enable_notifications")) {
            if (sharedPreferences.getBoolean(key, false)) {
                pref_enable_notifications.setSummary(getString(R.string.pref_summary_notifications_enabled));
                general_screen.addPreference(pref_build_check_interval);
            } else {
                pref_enable_notifications.setSummary(getString(R.string.pref_summary_notifications_disabled));
                general_screen.removePreference(pref_build_check_interval);
            }
        }
    }

    @Override
    public void onResume() {
        ThemeUtility.setAppTheme(getActivity());
        super.onResume();

        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }
}
