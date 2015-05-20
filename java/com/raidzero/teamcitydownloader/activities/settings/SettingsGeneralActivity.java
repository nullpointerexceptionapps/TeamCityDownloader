package com.raidzero.teamcitydownloader.activities.settings;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import com.raidzero.teamcitydownloader.R;
import com.raidzero.teamcitydownloader.fragments.settings.SettingsFragmentGeneral;
import com.raidzero.teamcitydownloader.global.ThemeUtility;

/**
 * Created by raidzero on 1/11/15.
 */
public class SettingsGeneralActivity extends ActionBarActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ThemeUtility.setAppTheme(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings);

        getFragmentManager().beginTransaction().replace(R.id.frameLayout_settings,
                new SettingsFragmentGeneral()).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
