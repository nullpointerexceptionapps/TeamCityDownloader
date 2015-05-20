package com.raidzero.teamcitydownloader.fragments.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.raidzero.teamcitydownloader.R;
import com.raidzero.teamcitydownloader.activities.settings.SettingsMainActivity;

/**
 * Created by posborn on 11/24/14.
 */
public class SettingsFragmentMain extends PreferenceFragment {

    private static final String tag = "SettingsFragmentMain";

    PreferenceScreen general_screen;
    Preference pref_version;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings_main);

        general_screen = (PreferenceScreen) findPreference(("pref_screen_general"));

        pref_version = findPreference("pref_version");
        pref_version.setSummary(((SettingsMainActivity) getActivity()).getVersionName());
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        //launch general
        if (preference.getKey().equals("pref_launch_general")) {
            ((SettingsMainActivity) getActivity()).launchGeneral();
        }

        // launch server
        else
        if (preference.getKey().equals("pref_launch_server")) {
            ((SettingsMainActivity) getActivity()).launchServer();
        }

        // launch appearance
        else
        if (preference.getKey().equals("pref_launch_appearance")) {
            ((SettingsMainActivity) getActivity()).launchAppearance();
        }

        // launch play store
        else
        if (preference.getKey().equals("pref_play_store")) {
            ((SettingsMainActivity) getActivity()).launchPlayStore();
        }

        // launch github
        else
        if (preference.getKey().equals("pref_github")) {
            ((SettingsMainActivity) getActivity()).launchGithub();
        }

        // launch feedback
        else
        if (preference.getKey().equals("pref_contact")) {
            ((SettingsMainActivity) getActivity()).launchContact();
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
