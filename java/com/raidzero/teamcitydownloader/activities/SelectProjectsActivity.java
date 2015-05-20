package com.raidzero.teamcitydownloader.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.raidzero.teamcitydownloader.R;
import com.raidzero.teamcitydownloader.adapters.ProjectAdapter;
import com.raidzero.teamcitydownloader.data.TeamCityProject;
import com.raidzero.teamcitydownloader.data.TeamCityServer;
import com.raidzero.teamcitydownloader.data.WebResponse;
import com.raidzero.teamcitydownloader.global.Debug;
import com.raidzero.teamcitydownloader.global.DialogUtility;
import com.raidzero.teamcitydownloader.global.QueryUtility;
import com.raidzero.teamcitydownloader.global.TeamCityXmlParser;
import com.raidzero.teamcitydownloader.tasks.TeamCityTask;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

/**
 * Created by posborn on 8/4/14.
 */
public class SelectProjectsActivity extends TeamCityActivity implements TeamCityTask.OnWebRequestCompleteListener, QueryUtility.QueryCallbacks {
    private static final String tag = "SelectProjectsActivity";

    private ArrayList<TeamCityProject> available_projects = new ArrayList<TeamCityProject>();
    private ArrayList<TeamCityProject> saved_projects = new ArrayList<TeamCityProject>();
    private ProjectAdapter adapter;
    private QueryUtility queryUtility;
    boolean queryError = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_projects);
        activity_title = getString(R.string.action_add_project);
        doExitAnimation = false;
    }

    @Override
    public void onResume() {
        super.onResume();

        queryUtility = new QueryUtility(this, "app/rest/projects");

        saved_projects = helper.getSavedProjects();
        adapter = new ProjectAdapter(this, available_projects, true);

        Button saveButton = (Button) findViewById(R.id.projects_save_button);
        ListView list_projects = (ListView) findViewById(R.id.list_available_projects);

        saveButton.setOnClickListener(saveButtonListener);
        list_projects.setAdapter(adapter);

        Debug.Log(tag, "server is null? " + (server == null));
        if (server != null) {
            server.setContext(this);
        } else {
            TeamCityServer.serverMisconfigured(this);
        }
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == refreshItem) {
            Debug.Log(tag, "refresh selected");
            queryUtility.queryServer(true);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    View.OnClickListener saveButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Debug.Log(tag, "save button clicked");
            saveProjects();
        }
    };

    @Override
    public void onPause() {
        super.onPause();

        if (queryUtility.isQuerying()) {
            helper.setSavedProjects(saved_projects);
        } else {
            saveProjects();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (queryUtility.isQuerying()) {
            helper.setSavedProjects(saved_projects);
        } else {
            saveProjects();
        }
    }

    @Override
    public void onWebRequestComplete(final WebResponse response) {
        if (response != null) {
            if (response.getResponseDocument() != null) {
                available_projects = parseProjects(response.getResponseDocument());

                if (available_projects.size() == 0) {
                    DialogUtility.showAlert(this, "It's lonely here.", "No projects found");
                } else {
                    updateList(activity_title, adapter, available_projects);
                }
            } else {
                // error dialog
                Debug.Log(tag, "going to save " + saved_projects.size() + " projects before erroring");
                helper.setSavedProjects(saved_projects);
                queryError = true;
                showAlert("Error", response.getStatusReason(this), true);
                Debug.Log(tag, "error: " + response.getStatusReason(this));
            }
        }
        stopSpinning();
    }

    private ArrayList<TeamCityProject> parseProjects(String xml) {

        ArrayList<TeamCityProject> rtnData = new ArrayList<TeamCityProject>();
        boolean skipArchived = server_prefs.getBoolean("pref_ignore_archived", false);

        TeamCityXmlParser parser = new TeamCityXmlParser(xml);

        NodeList rootList = parser.getNodes("projects");

        // only one projects node
        Element projectsNode = (Element) rootList.item(0);
        NodeList projectsList = parser.getNodes(projectsNode, "project");

        for (int i = 0; i < projectsList.getLength(); i++) {
            Element project = (Element) projectsList.item(i);

            String name = parser.getAttribute(project, "name");
            String id = parser.getAttribute(project, "id");
            String url = parser.getAttribute(project, "href");
            String archived = parser.getAttribute(project, "archived");

            Debug.Log(tag, "name: " + name);

            // no root project
            if (name.equals("<Root project>")) {
                continue;
            }

            // skip archived projects if necessary
            if (archived != null && archived.equalsIgnoreCase("true")) {
                if (skipArchived) {
                    continue;
                }
            }

            rtnData.add(new TeamCityProject(id, name, url));
        }

        return rtnData;
    }

    private void saveProjects() {
        if (!queryError) {
            ArrayList<TeamCityProject> selected_projects = adapter.getCheckedProjects();
            Debug.Log(tag, "saving " + selected_projects.size() + " projects...");

            helper.setSavedProjects(selected_projects);
            finish();
        }
    }

    @Override
    public void onQueryComplete(WebResponse response) {
        onWebRequestComplete(response);
    }
}
