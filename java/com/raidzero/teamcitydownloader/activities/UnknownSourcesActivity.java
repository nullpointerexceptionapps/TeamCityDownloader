package com.raidzero.teamcitydownloader.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.raidzero.teamcitydownloader.R;
import com.raidzero.teamcitydownloader.global.Debug;

/**
 * Created by raidzero on 7/6/14.
 */
public class UnknownSourcesActivity extends Activity implements View.OnClickListener{
    private static final String tag = "UnknownSourcesActivity";

    private Button okButton;
    private Button cancelButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.unknown);

        okButton = (Button) findViewById(R.id.btn_unknown_ok);
        cancelButton = (Button) findViewById(R.id.btn_unknown_cancel);
    }

    @Override
    public void onResume() {
        super.onResume();

        okButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == okButton.getId()) {
            Debug.Log(tag, "OK clicked.");

            Intent i = new Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS);
            startActivity(i);

            Intent returnIntent = new Intent();
            setResult(RESULT_OK, returnIntent);
            finish();
        } else if (v.getId() == cancelButton.getId()) {
            Debug.Log(tag, "cancel clicked");

            Intent returnIntent = new Intent();
            setResult(RESULT_CANCELED, returnIntent);
            finish();
        }

    }
}
