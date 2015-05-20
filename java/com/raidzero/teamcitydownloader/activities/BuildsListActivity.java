package com.raidzero.teamcitydownloader.activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.raidzero.teamcitydownloader.R;
import com.raidzero.teamcitydownloader.adapters.BuildAdapter;
import com.raidzero.teamcitydownloader.data.TeamCityBuild;
import com.raidzero.teamcitydownloader.data.TeamCityItem;
import com.raidzero.teamcitydownloader.data.WebResponse;
import com.raidzero.teamcitydownloader.global.BrowserUtility;
import com.raidzero.teamcitydownloader.global.Debug;
import com.raidzero.teamcitydownloader.global.DialogUtility;
import com.raidzero.teamcitydownloader.global.QueryUtility;
import com.raidzero.teamcitydownloader.global.TeamCityXmlParser;
import com.raidzero.teamcitydownloader.global.ThemeUtility;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by posborn on 8/5/14.
 */
public class BuildsListActivity extends TeamCityActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener, View.OnCreateContextMenuListener {
    private static final String tag = "BuildsListActivity";

    private ArrayList<TeamCityBuild> allBuilds = new ArrayList<TeamCityBuild>();
    private BuildAdapter adapter;

    private Menu menu;
    private boolean isConfigStarred = false;
    private ListView list_builds;
    private TextView txt_no_builds;
    private TextView list_title;

    private ArrayList<TeamCityItem> starred_items;
    private TeamCityItem currentConfig;
    private QueryUtility queryUtility;

    private ArrayList<String> allBranches = new ArrayList<String>();
    private ArrayList<TeamCityBuild> filteredBuilds = new ArrayList<TeamCityBuild>();
    private ArrayList<TeamCityBuild> displayedBuilds = new ArrayList<TeamCityBuild>();
    private ArrayAdapter<String> spinnerAdapter;
    private Spinner spinner_filter;
    private String selectedBranch;

    private boolean cameFromHome, forceRefresh;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.builds_list);
    }

    @Override
    public void onResume() {
        super.onResume();
        currentConfig = (TeamCityItem) intent.getParcelableExtra("selectedBuildConfig");
        selectedBranch = intent.getStringExtra("selectedBranch");

        cameFromHome = intent.getBooleanExtra("cameFromHome", false);
        forceRefresh = intent.getBooleanExtra("forceRefresh", false);

        if (cameFromHome) {
            clearTitleBar();
        }

        Debug.Log(tag, "resuming activity with selectedBranch: " + selectedBranch);
        addToTitleBar(currentConfig.getName());
        adapter = new BuildAdapter(this, allBuilds);

        spinner_filter = (Spinner) findViewById(R.id.spinner_branch_filter);
        spinner_filter.setOnItemSelectedListener(this);

        list_title = (TextView) findViewById(R.id.txt_list_title);
        list_title.setText(getTitleBar(2));

        txt_no_builds = (TextView) findViewById(R.id.txt_no_builds);
        list_builds = (ListView) findViewById(R.id.list_builds);

        list_builds.setAdapter(adapter);
        list_builds.setOnItemClickListener(this);
        list_builds.setOnCreateContextMenuListener(this);

        starred_items = helper.getStarredConfigs();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.refresh_star, menu);

        refreshItem = menu.findItem(R.id.action_refresh);

        Drawable icon = getThemedStarIcon();

        menu.getItem(0).setIcon(icon);

        String configUrl = currentConfig.getUrl();

        // we dont keep /builds at the end of url anymore, but just in case...
        if (configUrl.endsWith("/builds")) {
            configUrl = configUrl.replaceAll("/builds$", "");
        }

        queryUtility = new QueryUtility(this, configUrl + "/builds/?locator=branch:default:any");
        queryUtility.queryServer(forceRefresh);

        return super.onCreateOptionsMenu(menu);
    }

    private Drawable getThemedStarIcon() {
        Drawable filledStar = ThemeUtility.getThemedDrawable(this, R.attr.themedFilledStarIcon);
        Drawable emptyStar = ThemeUtility.getThemedDrawable(this, R.attr.themedEmptyStarIcon);

        if (helper.isConfigStarred(currentConfig)) {
            isConfigStarred = true;
            Debug.Log(tag, "filled star");
            return filledStar;
        } else {
            isConfigStarred = false;
            Debug.Log(tag, "empty star");
            return emptyStar;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_favorite) {

            TeamCityItem selected = currentConfig;

            if (isConfigStarred) {
                helper.removeFavorite(selected);
                //showToast("remove favorite: " + selected.getName());
                item.setIcon(getThemedStarIcon());
            } else {
                helper.addFavorite(selected);
                //showToast("favorited: " + selected.getName());
                item.setIcon(getThemedStarIcon());
            }

            return true;
        }

        if (item.getItemId() == R.id.action_refresh) {
            queryUtility.queryServer(true);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TeamCityBuild selected;

        if (filteredBuilds.size() > 0) {
            selected = filteredBuilds.get(position);
        } else {
            selected = allBuilds.get(position);
        }

        String url = selected.getUrl() + "/artifacts/children";

        selected.setUrl(url);
        Debug.Log(tag, "clicked: " + url);

        Intent resultIntent = new Intent();
        resultIntent.putExtra("selectedBuild", selected);
        resultIntent.putExtra("selectedBranch", selectedBranch);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId()==R.id.list_builds) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            menu.setHeaderTitle("#" + displayedBuilds.get(info.position).getNumber() + " - " +
                    displayedBuilds.get(info.position).getStatus());
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.build_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        TeamCityBuild selectedBuild = displayedBuilds.get(info.position);
        Debug.Log(tag, "context info selected on build: " + selectedBuild.getNumber());

        switch (item.getItemId()) {
            case R.id.context_build_info:
                Intent i = new Intent();
                i.putExtra("showBuildInfo", true);
                i.putExtra("selectedBuild", selectedBuild);
                i.putExtra("selectedBranch", selectedBranch);
                setResult(RESULT_OK, i);
                finish();
                break;
            case R.id.context_open_in_browser:
                BrowserUtility.startBrowser(this, selectedBuild.getWebUrl());
                break;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public void onQueryComplete(final WebResponse response) {
        super.onQueryComplete(response);
        if (response != null) {
            if (response.getResponseDocument() != null) {
                allBuilds = parseBuilds(response.getResponseDocument());

                if (allBuilds.size() == 0) {
                    hideView(list_builds);
                    showView(txt_no_builds);
                } else {
                    // more than just getString(R.string.all_branches) and "default branch"?
                    if (allBranches.size() > 2) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                spinner_filter.setVisibility(View.VISIBLE);
                            }
                        });
                        for (String branch : allBranches) {
                            Debug.Log(tag, "branch: " + branch);
                        }

                        updateBranchSpinner(allBranches);
                        updateList(getResources().getString(R.string.title_available_builds), adapter, filteredBuilds);
                        displayedBuilds = filteredBuilds;
                        if (selectedBranch != null) {
                            setSelectedBranch(selectedBranch, true);
                        }
                    } else {
                        Debug.Log(tag, "no interesting branches to show.");
                        updateList(getResources().getString(R.string.title_available_builds), adapter, allBuilds);
                        displayedBuilds = allBuilds;
                    }
                }
            } else {
                // error dialog
                showAlert("Error", response.getStatusReason(this), true);
                Debug.Log(tag, "error: " + response.getStatusReason(this));
            }
        }
    }

    private void updateBranchSpinner(ArrayList<String> branches) {
        final String[] branchArray = branches.toArray(new String[branches.size()]);

        spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, branchArray);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                spinner_filter.setAdapter(spinnerAdapter);
            }
        });

    }

    private ArrayList<TeamCityBuild> parseBuilds(String xml) {

        ArrayList<TeamCityBuild> rtnData = new ArrayList<TeamCityBuild>();
        TeamCityXmlParser parser = new TeamCityXmlParser(xml);

        // one root node
        NodeList rootList = parser.getNodes("builds");
        Element projectNode = (Element) rootList.item(0);

        NodeList builds = parser.getNodes(projectNode, "build");
        boolean defaultFound = false;

        for (int i = 0; i < builds.getLength(); i++) {
            Element build = (Element) builds.item(i);
            String number = parser.getAttribute(build, "number");
            String status = parser.getAttribute(build, "status");
            String url = parser.getAttribute(build, "href");
            String branch = parser.getAttribute(build, "branchName");
            String webUrl = parser.getAttribute(build, "webUrl");

            rtnData.add(new TeamCityBuild(number, status, url, branch, webUrl));

            // save this branch in the list
            if (!allBranches.contains(branch) && !branch.isEmpty()) {
                allBranches.add(branch);
            } else if (branch.isEmpty()) {
                defaultFound = true;
            }
        }

        Collections.sort(allBranches);

        if (!allBranches.contains(getString(R.string.all_branches))) {
            allBranches.add(0, getString(R.string.all_branches));
        }
        if (!allBranches.contains(getString(R.string.default_branch)) && defaultFound) {
            allBranches.add(1, getString(R.string.default_branch));
        }

        stopSpinning();
        return rtnData;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String selectedBranch = allBranches.get(position);
        this.selectedBranch = selectedBranch;

        setSelectedBranch(selectedBranch, false);
    }

    private void setSelectedBranch(String selectedBranch, boolean setSpinner) {
        boolean showAllBuilds = false;
        if (selectedBranch.equals(getString(R.string.all_branches))) {
            showAllBuilds = true;
        }

        if (selectedBranch.equals("Default Branch")) {
            selectedBranch = "";
        }

        Debug.Log(tag, "Selected branch: " + selectedBranch);

        filteredBuilds.clear();

        for (TeamCityBuild build : allBuilds) {
            if (build.getBranch().equals(selectedBranch) || showAllBuilds) {
                filteredBuilds.add(build);
            }
        }

        if (setSpinner) {
            // find index of selected branch
            final int spinnerIndex = allBranches.indexOf(selectedBranch);
            if (spinnerIndex > 0) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        spinner_filter.setSelection(spinnerIndex);
                    }
                });
            }
        } else {
            updateList(getString(R.string.title_available_builds), adapter, filteredBuilds);
            displayedBuilds = filteredBuilds;
        }
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // nothing?
    }
}
