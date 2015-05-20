package com.raidzero.teamcitydownloader.fragments.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.widget.EditText;

import com.raidzero.teamcitydownloader.R;
import com.raidzero.teamcitydownloader.activities.settings.SettingsServerActivity;
import com.raidzero.teamcitydownloader.global.Common;
import com.raidzero.teamcitydownloader.global.Debug;
import com.raidzero.teamcitydownloader.global.DialogUtility;
import com.raidzero.teamcitydownloader.global.ThemeUtility;

/**
 * Created by posborn on 11/24/14.
 */
public class SettingsFragmentServer extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String tag = "SettingsFragmentServer";

    PreferenceCategory credentials_category;
    EditTextPreference pref_serverAddress;
    CheckBoxPreference pref_use_guest;
    EditTextPreference pref_username;
    EditTextPreference pref_password;
    PreferenceScreen server_screen;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings_server);

        credentials_category = (PreferenceCategory) findPreference("pref_key_credentials");

        pref_serverAddress = (EditTextPreference) findPreference("pref_server_address");
        pref_use_guest = (CheckBoxPreference) findPreference("pref_enable_guest");
        pref_username = (EditTextPreference) findPreference("pref_server_username");
        pref_password = (EditTextPreference) findPreference("pref_server_password");

        server_screen = (PreferenceScreen) findPreference("pref_screen_server");

        if (pref_use_guest.isChecked()) {
            server_screen.removePreference(credentials_category);
        }

        String serverAddress = pref_serverAddress.getText();
        String username = pref_username.getText();
        String password = pref_password.getText();

        if (serverAddress != null) {
            pref_serverAddress.setSummary(serverAddress);
        }

        if (username != null) {
            pref_username.setSummary(username);
        }

        if (password != null) {
            EditText edit = pref_password.getEditText();
            String maskedPassword = pref_password.getEditText().getTransformationMethod().getTransformation(password, edit).toString();
            pref_password.setSummary(maskedPassword);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        // handle guest login option
        if (key.equals("pref_enable_guest")) {
            server_screen = (PreferenceScreen) findPreference("pref_screen_server");

            if (sharedPreferences.getBoolean(key, false)) {
                server_screen.removePreference(credentials_category);
            } else {
                server_screen.addPreference(credentials_category);
            }
        }

        // update summary of text preferences
        if (key.equals("pref_server_address")) {
            String serverAddress = sharedPreferences.getString("pref_server_address", "");

            // what if http prefix was left out?
            if (!serverAddress.isEmpty() && !serverAddress.startsWith("http")) {
                serverAddress = "http://" + serverAddress;
                DialogUtility.makeToast(getActivity(), "Notice: HTTP prefix automatically added");
            }

            pref_serverAddress.setSummary(serverAddress);
            pref_serverAddress.setText(serverAddress);
        }

        if (key.equals("pref_server_username")) {
            pref_username.setSummary(sharedPreferences.getString("pref_server_username", ""));
        }

        if (key.equals("pref_server_password")) {
            String newPassword = sharedPreferences.getString("pref_server_password", "");

            EditText edit = pref_password.getEditText();
            String maskedPassword = pref_password.getEditText().getTransformationMethod().getTransformation(newPassword, edit).toString();
            pref_password.setSummary(maskedPassword);
        }

        // broadcast this event
        Debug.Log(tag, "broadcasting intent to refresh server data");
        Intent i = new Intent(Common.INTENT_SERVER_UPDATE);
        getActivity().sendBroadcast(i);
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

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        // test server
        if (preference.getKey().equals("pref_test_connection")) {
            ((SettingsServerActivity) getActivity()).testConnection();
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
