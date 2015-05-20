package com.raidzero.teamcitydownloader.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.raidzero.teamcitydownloader.R;
import com.raidzero.teamcitydownloader.activities.settings.SettingsServerActivity;
import com.raidzero.teamcitydownloader.adapters.ConfigErrorAdapter;
import com.raidzero.teamcitydownloader.global.Debug;
import com.raidzero.teamcitydownloader.global.ThemeUtility;

import java.util.ArrayList;

/**
 * Created by raidzero on 7/6/14.
 */
public class ConfigErrorActivity extends Activity implements View.OnClickListener{
    private static final String tag = "ConfigErrorActivity";

    private Button okButton;
    private ListView list_errors;
    private TextView launchSettings;
    private ConfigErrorAdapter adapter;
    private boolean onServerScreen;
    private ArrayList<String> errors = new ArrayList<String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(ThemeUtility.getDialogActivityTheme(this));

        setContentView(R.layout.config_errors);

        Intent data = getIntent();

        errors = data.getStringArrayListExtra("errors");
        onServerScreen = data.getBooleanExtra("onServerScreen", false);

        okButton = (Button) findViewById(R.id.btn_unknown_ok);
        list_errors = (ListView) findViewById(R.id.list_config_errors);
        launchSettings = (TextView) findViewById(R.id.tap_ok_to_launch_settings);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (onServerScreen) {
            launchSettings.setVisibility(View.GONE);
        } else {
            launchSettings.setVisibility(View.VISIBLE);
        }

        adapter = new ConfigErrorAdapter(this, errors);
        list_errors.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        okButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == okButton.getId()) {
            Debug.Log(tag, "OK clicked.");

            finish();

            if (!onServerScreen) {
                Intent i = new Intent(this, SettingsServerActivity.class);
                i.putExtra("cameFromError", true);
                startActivity(i);
            }
        }
    }
}
