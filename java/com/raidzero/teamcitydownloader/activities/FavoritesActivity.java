package com.raidzero.teamcitydownloader.activities;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.raidzero.teamcitydownloader.R;
import com.raidzero.teamcitydownloader.data.TeamCityItem;
import com.raidzero.teamcitydownloader.global.AppHelper;
import com.raidzero.teamcitydownloader.global.ThemeUtility;

import java.util.ArrayList;

/**
 * Created by posborn on 12/2/14.
 */
public class FavoritesActivity extends ListActivity implements AdapterView.OnItemClickListener {
    private static final String tag = "FavoritesActivity";
    private ListView list;
    private ArrayList<String> configList;
    private AppHelper helper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        int dialogTheme = ThemeUtility.getDialogActivityTheme(this);
        setTheme(dialogTheme);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorites_layout);

        helper = (AppHelper) getApplicationContext();
        list = (ListView) findViewById(android.R.id.list);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String name = configList.get(position);

        TeamCityItem selectedItem = helper.getFavoriteByName(name);
        startBuildsActivity(selectedItem);
    }

    @Override
    public void onResume() {
        super.onResume();

        Intent intent = getIntent();

        configList = intent.getStringArrayListExtra("configs");

        if (configList.size() == 1) {
            // go right to the builds for this config if there is only one
            TeamCityItem selectedItem = helper.getFavoriteByName(configList.get(0));
            startBuildsActivity(selectedItem);
            finish();
        }

        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.list_item_text, configList);
        list.setAdapter(adapter);

        list.setOnItemClickListener(this);
    }

    private void startBuildsActivity(TeamCityItem selectedItem) {
        Intent i = new Intent(this, ControllerActivity.class);
        i.putExtra("gotoStarred", true);
        i.putExtra("selectedBuildConfig", selectedItem);
        i.putExtra("forceRefresh", true);
        startActivity(i);
    }
}
