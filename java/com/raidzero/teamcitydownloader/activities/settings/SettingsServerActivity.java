package com.raidzero.teamcitydownloader.activities.settings;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import com.raidzero.teamcitydownloader.R;
import com.raidzero.teamcitydownloader.activities.MainActivity;
import com.raidzero.teamcitydownloader.data.WebResponse;
import com.raidzero.teamcitydownloader.fragments.settings.SettingsFragmentServer;
import com.raidzero.teamcitydownloader.global.DialogUtility;
import com.raidzero.teamcitydownloader.global.QueryUtility;
import com.raidzero.teamcitydownloader.global.ThemeUtility;

/**
 * Created by raidzero on 1/11/15.
 */
public class SettingsServerActivity extends ActionBarActivity implements QueryUtility.QueryCallbacks {
    private QueryUtility queryUtility;
    private boolean cameFromError;
    private ProgressDialog progDialog;
    public static boolean querying = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ThemeUtility.setAppTheme(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings);

        cameFromError = getIntent().getBooleanExtra("cameFromError", false);
        getFragmentManager().beginTransaction().replace(R.id.frameLayout_settings,
                new SettingsFragmentServer()).commit();

        progDialog = new ProgressDialog(this, ThemeUtility.getHoloDialogTheme(this));
        progDialog.setIndeterminate(true);
        progDialog.setMessage(getString(R.string.querying_please_wait));
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

    @Override
    public void onBackPressed() {
        if (cameFromError) {
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityForResult(i, 0);
        }

        super.onBackPressed();
    }

    public void testConnection() {
        if (!querying) {
            querying = true;
            queryUtility = new QueryUtility(this, "app/rest/server");
            queryUtility.setOnSettingsScreen(true);
            queryUtility.queryServer(true);
        }
    }

    @Override
    public void onQueryComplete(final WebResponse response) {
        querying = false;
        if (response != null) {
            String statusCode = String.valueOf(response.getStatusCode());
            String statusReason = response.getStatusReason(this);

            if (!statusCode.equals("200")) {
                statusCode = String.format("%s: %s", getString(R.string.error), statusCode);
            } else {
                statusCode = getString(R.string.success);
                statusReason = getString(R.string.server_connection_success);
            }

            if (response.getStatusCode() == 0) {
                statusCode = getString(R.string.error);
            }

            final String finalStatusCode = statusCode;
            final String finalStatusReason = statusReason;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DialogUtility.showAlert(SettingsServerActivity.this, finalStatusCode, finalStatusReason);
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DialogUtility.showAlert(SettingsServerActivity.this,
                            getString(R.string.error), getString(R.string.unknown_error_has_occurred));
                }
            });
        }
    }

    @Override
    public void startSpinning() {
        if (!progDialog.isShowing()) {
            progDialog.show();
        }
    }

    @Override
    public void stopSpinning() {
        if (progDialog.isShowing()) {
            progDialog.dismiss();
        }
    }

    @Override
    public void setActivityTitle(String title) {
        // nothing
    }
}
