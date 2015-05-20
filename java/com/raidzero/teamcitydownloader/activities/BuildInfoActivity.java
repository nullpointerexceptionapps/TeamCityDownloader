package com.raidzero.teamcitydownloader.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.raidzero.teamcitydownloader.R;
import com.raidzero.teamcitydownloader.data.TeamCityBuild;
import com.raidzero.teamcitydownloader.data.WebResponse;
import com.raidzero.teamcitydownloader.global.DateUtility;
import com.raidzero.teamcitydownloader.global.Debug;
import com.raidzero.teamcitydownloader.global.QueryUtility;
import com.raidzero.teamcitydownloader.global.TeamCityXmlParser;
import com.raidzero.teamcitydownloader.global.ThemeUtility;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by raidzero on 8/14/14.
 */
public class BuildInfoActivity extends TeamCityActivity {
    private static final String tag = "BuildInfoActivity";
    private TeamCityBuild build;

    private QueryUtility queryUtility;

    private TextView txt_triggered;
    private TextView txt_agent;
    private TextView txt_project;
    private TextView txt_queued;
    private TextView txt_started;
    private TextView txt_finished;
    private TextView txt_branch;
    private TextView txt_status;

    private TextView custom_properties_header;
    private LinearLayout custom_properties_container;

    // build info container
    private class TeamCityBuildInfo {
        public String branchName;
        public String webUrl; // not displayed

        public Map<String, String> buildProperties = new HashMap<String, String>();
        public Map<String, String> customProperties = new HashMap<String, String>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        build = intent.getParcelableExtra("selectedBuild");

        queryUtility = new QueryUtility(this, build.getUrl());

        setContentView(R.layout.build_info);
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
            queryUtility.queryServer(true);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        txt_triggered = (TextView) findViewById(R.id.txt_triggered);
        txt_agent = (TextView) findViewById(R.id.txt_agent);
        txt_project = (TextView) findViewById(R.id.txt_project);
        txt_branch = (TextView) findViewById(R.id.txt_branch);
        txt_queued = (TextView) findViewById(R.id.txt_queued);
        txt_started = (TextView) findViewById(R.id.txt_started);
        txt_finished = (TextView) findViewById(R.id.txt_finished);
        txt_status = (TextView) findViewById(R.id.txt_status);

        custom_properties_header = (TextView) findViewById(R.id.txt_custom_header);
        custom_properties_container = (LinearLayout) findViewById(R.id.container_custom_properties);
    }

    @Override
    public void onQueryComplete(WebResponse response) {
        Debug.Log(tag, "onQueryComplete");
        super.onQueryComplete(response);
        final TeamCityBuildInfo buildInfo = parseBuildInfo(response.getResponseDocument());

        if (buildInfo != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // display main properties
                    Debug.Log(tag, "runOnUiThread() started");

                    txt_triggered.setText(buildInfo.buildProperties.get("Triggered By"));
                    txt_agent.setText(buildInfo.buildProperties.get("Agent"));
                    txt_project.setText(buildInfo.buildProperties.get("Project Name"));
                    txt_branch.setText(buildInfo.buildProperties.get("Branch Name"));
                    txt_queued.setText(buildInfo.buildProperties.get("Queued Date"));
                    txt_started.setText(buildInfo.buildProperties.get("Start Date"));
                    txt_finished.setText(buildInfo.buildProperties.get("Finish Date"));
                    txt_status.setText(build.getStatus());

                    if (build.getStatus().equalsIgnoreCase("SUCCESS")) {
                        txt_status.setTextColor(getResources().getColor(R.color.color_build_success));
                    } else {
                        txt_status.setTextColor(getResources().getColor(R.color.color_build_failed));
                    }

                    if (buildInfo.customProperties.size() > 0) {

                        custom_properties_container.removeAllViews();

                        // display custom properties

                        custom_properties_header.setVisibility(View.VISIBLE);
                        for (Map.Entry<String, String> entry : buildInfo.customProperties.entrySet()) {
                            TextView nameView = new TextView(BuildInfoActivity.this);
                            nameView.setText(entry.getKey() + ":");
                            nameView.setTextColor(ThemeUtility.getThemedColor(BuildInfoActivity.this, R.attr.themedBuildPropertyColor));

                            custom_properties_container.addView(nameView);

                            TextView valueView = new TextView(BuildInfoActivity.this);
                            valueView.setText(entry.getValue());

                            custom_properties_container.addView(valueView);
                        }
                    }
                }
            });
        }

        Debug.Log(tag, "runOnUiThread done");
        stopSpinning();
        setActivityTitle("#" + build.getNumber());
    }

    private TeamCityBuildInfo parseBuildInfo(String xml) {
        TeamCityBuildInfo rtnData = new TeamCityBuildInfo();
        TeamCityXmlParser parser = new TeamCityXmlParser(xml);

        // one root node
        NodeList rootNode = parser.getNodes("build");
        Element build = (Element) rootNode.item(0);

        rtnData.webUrl = build.getAttribute("webUrl");

        String branchName = build.getAttribute("branchName");
        if (branchName.isEmpty()) {
            branchName = getString(R.string.default_branch);
        }

        rtnData.branchName = branchName;
        rtnData.buildProperties.put("Branch Name", branchName);

        NodeList buildTypeNode = parser.getNodes(build, "buildType");
        Element buildType = (Element) buildTypeNode.item(0);
        rtnData.buildProperties.put("Project Name", buildType.getAttribute("projectName"));

        NodeList queuedNode = parser.getNodes(build, "queuedDate");
        Element queuedDate = (Element) queuedNode.item(0);
        String rawQueuedDate = queuedDate.getTextContent();
        String friendlyQueued = rawQueuedDate;
        try {
            friendlyQueued = DateUtility.friendlyDate(rawQueuedDate);
        } catch (ParseException e) {
            // nothing
        }
        rtnData.buildProperties.put("Queued Date", friendlyQueued);

        NodeList startedNode = parser.getNodes(build, "startDate");
        Element startedDate = (Element) startedNode.item(0);
        String rawStartDate = startedDate.getTextContent();
        String friendlyStartDate = rawStartDate;
        try {
            friendlyStartDate = DateUtility.friendlyDate(rawStartDate);
        } catch (ParseException e) {
            // nothing
        }
        rtnData.buildProperties.put("Start Date", friendlyStartDate);

        NodeList finishedNode = parser.getNodes(build, "finishDate");
        Element finishedDate = (Element) finishedNode.item(0);
        String rawFinishDate = finishedDate.getTextContent();
        String friendlyFinishDate = rawFinishDate;
        try {
            friendlyFinishDate = DateUtility.friendlyDate(rawFinishDate);
        } catch (ParseException e) {
            // nothing
        }
        rtnData.buildProperties.put("Finish Date", friendlyFinishDate);

        NodeList triggeredNode = parser.getNodes(build, "triggered");
        Element triggered = (Element) triggeredNode.item(0);

        // what type?
        String triggerType = triggered.getAttribute("type");
        String username = "";
        String user = "";

        if (triggerType.equalsIgnoreCase("vcs") || triggerType.equalsIgnoreCase("unknown")) {
            username = triggerType;
            user = triggered.getAttribute("details");
        } else if (triggerType.equalsIgnoreCase("user")) {
            NodeList userNode = parser.getNodes(triggered, "user");
            Element userName = (Element) userNode.item(0);

            username = userName.getAttribute("username");
            user = userName.getAttribute("name");
        } else {
            username = triggerType;
            user = "";
        }

        if (username.isEmpty()) {
            username = "Unknown";
        }

        if (!user.isEmpty()) {
            rtnData.buildProperties.put("Triggered By",
                    username + " (" + user + ")");
        } else {
            rtnData.buildProperties.put("Triggered By",
                    username);
        }

        NodeList agentNode = parser.getNodes(build, "agent");
        Element agent = (Element) agentNode.item(0);
        rtnData.buildProperties.put("Agent", agent.getAttribute("name"));

        // properties may not exist
        NodeList propertiesNode = parser.getNodes(build, "properties");
        try {
            Element propertiesElement = (Element) propertiesNode.item(0);
            NodeList properties = propertiesElement.getChildNodes();

            for (int i = 0; i < properties.getLength(); i++) {
                Element prop = (Element) properties.item(i);
                String propName = prop.getAttribute("name");
                String propValue = prop.getAttribute("value");
                Debug.Log(tag, "prop: " + propName + ": " + propValue);
                rtnData.customProperties.put(propName, propValue);
            }
        } catch (NullPointerException e) {
            // ignore, no properties here
        }

        return rtnData;
    }
}
