package com.raidzero.teamcitydownloader.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.raidzero.teamcitydownloader.R;
import com.raidzero.teamcitydownloader.adapters.ItemAdapter;
import com.raidzero.teamcitydownloader.data.TeamCityItem;
import com.raidzero.teamcitydownloader.data.TeamCityProject;
import com.raidzero.teamcitydownloader.data.WebResponse;
import com.raidzero.teamcitydownloader.global.BrowserUtility;
import com.raidzero.teamcitydownloader.global.Debug;
import com.raidzero.teamcitydownloader.global.QueryUtility;
import com.raidzero.teamcitydownloader.global.TeamCityXmlParser;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

/**
 * Created by posborn on 8/4/14.
 */
public class BuildConfigsListActivity extends TeamCityActivity implements AdapterView.OnItemClickListener, View.OnCreateContextMenuListener {
    private static final String tag = "BuildConfigsListActivity";

    private ArrayList<TeamCityItem> configs = new ArrayList<TeamCityItem>();
    private ItemAdapter adapter;
    private TextView txt_no_configs;
    private ListView list_items;
    private TeamCityProject project;
    private TextView list_title;

    private QueryUtility queryUtility;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity_title = getString(R.string.title_available_configs);

        setContentView(R.layout.items_list);
    }

    @Override
    public void onResume() {
        super.onResume();
        project = intent.getParcelableExtra("selectedProject");
        Debug.Log(tag, "project from intent? " + (project != null));
        clearTitleBar();
        addToTitleBar(project.getName());

        queryUtility = new QueryUtility(this, project.getUrl());

        adapter = new ItemAdapter(this, configs);
        txt_no_configs = (TextView) findViewById(R.id.txt_no_configs);

        list_title = (TextView) findViewById(R.id.txt_list_title);
        list_title.setText(getTitleBar(1));

        list_items = (ListView) findViewById(R.id.list_items);
        list_items.setAdapter(adapter);
        list_items.setOnItemClickListener(this);
        list_items.setOnCreateContextMenuListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Debug.Log(tag, "onCreateOptionsMenu()");
        getMenuInflater().inflate(R.menu.refresh_only, menu);

        refreshItem = menu.findItem(R.id.action_refresh);

        queryUtility.queryServer(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TeamCityItem selected = configs.get(position);

        Debug.Log(tag, "selected: " + selected.getName());
        String configUrl = selected.getUrl();
        selected.setUrl(configUrl);

        Debug.Log(tag, "clicked: " + configUrl);

        Intent resultIntent = new Intent();
        resultIntent.putExtra("selectedBuildConfig", selected);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_refresh) {
            queryUtility.queryServer(true);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId()==R.id.list_items) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            menu.setHeaderTitle(configs.get(info.position).getName());
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.open_in_browser, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        TeamCityItem selectedConfig = configs.get(info.position);
        Debug.Log(tag, "context info selected on build: " + selectedConfig.getName());

        switch (item.getItemId()) {
            case R.id.context_open_in_browser:
                BrowserUtility.startBrowser(this, selectedConfig.getWebUrl());
                break;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public void onQueryComplete(final WebResponse response) {
        super.onQueryComplete(response);

        if (response != null) {
            if (response.getResponseDocument() != null) {
                configs = parseProjects(response.getResponseDocument());

                if (configs.size() == 0) {
                    hideView(list_items);
                    showView(txt_no_configs);
                } else {
                    updateList(getString(R.string.title_available_configs), adapter, configs);
                }
            } else {
                // error dialog
                showAlert("Error", response.getStatusReason(this), true);
                Debug.Log(tag, "error: " + response.getStatusReason(this));
            }
        }
    }

    private ArrayList<TeamCityItem> parseProjects(String xml) {

        ArrayList<TeamCityItem> rtnData = new ArrayList<TeamCityItem>();
        TeamCityXmlParser parser = new TeamCityXmlParser(xml);

        // one root node
        NodeList rootList = parser.getNodes("project");
        Element projectNode = (Element) rootList.item(0);

        NodeList buildTypes = parser.getNodes(projectNode, "buildTypes");

        for (int i = 0; i < buildTypes.getLength(); i++) {
            Element e = (Element) buildTypes.item(i);

            NodeList configs = parser.getNodes(e, "buildType");

            for (int j = 0; j < configs.getLength(); j++) {
                Element config = (Element) configs.item(j);

                String url = parser.getAttribute(config, "href");
                String name = parser.getAttribute(config, "name");
                String webUrl = parser.getAttribute(config, "webUrl");
                rtnData.add(new TeamCityItem(name, url, webUrl));
            }
        }

        stopSpinning();
        return rtnData;
    }
}
