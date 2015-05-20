package com.raidzero.teamcitydownloader.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.raidzero.teamcitydownloader.R;
import com.raidzero.teamcitydownloader.data.WebResponse;
import com.raidzero.teamcitydownloader.global.BrowserUtility;
import com.raidzero.teamcitydownloader.global.Debug;
import com.raidzero.teamcitydownloader.global.QueryUtility;
import com.raidzero.teamcitydownloader.tasks.TeamCityTask;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by posborn on 8/4/14.
 */
public class AboutServerActivity extends TeamCityActivity implements View.OnClickListener, TeamCityTask.OnWebRequestCompleteListener, QueryUtility.QueryCallbacks {

    // just something for the parser to put together
    private class ServerInfo {
        public String serverVersion;
        public String startTime;
        public String currentTime;
        public String projectsPath;
    }

    private ServerInfo serverInfo;
    private QueryUtility queryUtility;

    private TextView txtVersion;
    private TextView txtStartTime;
    private TextView txtCurrentTime;
    private TextView txtProjectsPath;
    private Button btnOpenInBrowser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        queryUtility = new QueryUtility(this, "app/rest/server");

        activity_title = getString(R.string.title_about_server);
        doExitAnimation = false;

        setContentView(R.layout.about_server);
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

    @Override
    public void onResume() {
        super.onResume();

        txtVersion = (TextView) findViewById(R.id.txt_server_version);
        txtStartTime = (TextView) findViewById(R.id.txt_server_start_time);
        txtCurrentTime = (TextView) findViewById(R.id.txt_server_current_time);
        txtProjectsPath = (TextView) findViewById(R.id.txt_server_projects_path);
        btnOpenInBrowser = (Button) findViewById(R.id.btn_open_in_browser);

        btnOpenInBrowser.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == btnOpenInBrowser.getId()) {
            BrowserUtility.startBrowser(this, serverAddress);
        }
    }

    @Override
    public void onWebRequestComplete(WebResponse response) {
        Debug.Log(tag, "onQueryComplete()");


        if (response != null) {
            if (response.getResponseDocument() != null) {
                // parse it info a ServerInfo object
                serverInfo = parseResponse(response.getResponseDocument());

                if (serverInfo != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (serverInfo.serverVersion != null) {
                                txtVersion.setText(serverInfo.serverVersion);
                            } else {
                                txtVersion.setText(getString(R.string.unknown));
                            }

                            if (serverInfo.startTime != null) {
                                txtStartTime.setText(serverInfo.startTime);
                            } else {
                                txtStartTime.setText(getString(R.string.unknown));
                            }

                            if (serverInfo.currentTime != null) {
                                txtCurrentTime.setText(serverInfo.currentTime);
                            } else {
                                txtCurrentTime.setText(getString(R.string.unknown));
                            }

                            if (serverInfo.projectsPath != null) {
                                txtProjectsPath.setText(serverInfo.projectsPath);
                            } else {
                                txtProjectsPath.setText(getString(R.string.unknown));
                            }
                        }
                    });
                }
            } else {
                // response doc is null
                showAlert(getString(R.string.error), response.getStatusReason(this), true);
            }
            stopSpinning();
            setActivityTitle(activity_title);
        }
    }

    private ServerInfo parseResponse(String xml) {
        ServerInfo rtnData = new ServerInfo();
        Document doc;

        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xml));
            doc = db.parse(is);

            // do server
            NodeList serverNodes = doc.getElementsByTagName("server");
            Element server = (Element) serverNodes.item(0); // only one server line

            String version = server.getAttribute("version");
            String startTime = server.getAttribute("startTime");
            String currentTime = server.getAttribute("currentTime");

            //                             yyyyMMddTHHmmssZ
            // format these timestamps ex: 20140717T115444-0700
            String oldTimeFormat = "yyyyMMdd'T'HHmmssZ";
            String newTimeFormat = "yyyy-MM-dd hh:mm a z";

            SimpleDateFormat sdf = new SimpleDateFormat(oldTimeFormat);

            // get Date objects from server data
            Date startDate = sdf.parse(startTime);
            Date currentDate = sdf.parse(currentTime);

            sdf.applyPattern(newTimeFormat);
            startTime = sdf.format(startDate);
            currentTime = sdf.format(currentDate);

            Debug.Log(tag, "new: " + startTime);

            // now get projects path
            NodeList projectNodes = doc.getElementsByTagName("projects");
            Element projects = (Element) projectNodes.item(0); // only one projects line

            String projectsPath = projects.getAttribute("href");

            Debug.Log(tag, "server version: " + version + ", " + startTime + ":" + currentTime + ", " + projectsPath);

            rtnData.serverVersion = version;
            rtnData.startTime = startTime;
            rtnData.currentTime = currentTime;
            rtnData.projectsPath = projectsPath;

            return rtnData;

        } catch (Exception e) {
            Debug.Log(tag, "parse error: ", e);
            return null;
        }
    }

    @Override
    public void onQueryComplete(WebResponse response) {
        onWebRequestComplete(response);
    }
}
