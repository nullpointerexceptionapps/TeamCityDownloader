package com.raidzero.teamcitydownloader.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.raidzero.teamcitydownloader.R;
import com.raidzero.teamcitydownloader.adapters.ArtifactAdapter;
import com.raidzero.teamcitydownloader.data.DownloadRequest;
import com.raidzero.teamcitydownloader.data.TeamCityArtifact;
import com.raidzero.teamcitydownloader.data.TeamCityBuild;
import com.raidzero.teamcitydownloader.data.WebResponse;
import com.raidzero.teamcitydownloader.global.Debug;
import com.raidzero.teamcitydownloader.global.DialogUtility;
import com.raidzero.teamcitydownloader.global.DownloadHelper;
import com.raidzero.teamcitydownloader.global.FileUtility;
import com.raidzero.teamcitydownloader.global.QueryUtility;
import com.raidzero.teamcitydownloader.global.TeamCityXmlParser;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

/**
 * Created by posborn on 8/6/14.
 */
public class ArtifactsBrowserActivity extends TeamCityActivity implements AdapterView.OnItemClickListener {
    private static final String tag = "ArtifactsListActivity";

    private TeamCityBuild build;
    private QueryUtility queryUtility;
    private ArrayList<TeamCityArtifact> artifacts = new ArrayList<TeamCityArtifact>();
    private ArtifactAdapter adapter;
    private ListView list_artifacts;
    private TextView noArtifactsView;
    private TextView list_title;
    private DownloadHelper downloadHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.artifacts_list);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (helper.getBoolPref("pref_multiple_downloads")) {
            downloadHelper = helper.getDownloadHelper();
            helper.bindDownloadService();
        } else {
            helper.unbindDownloadService();
        }

        build = intent.getParcelableExtra("selectedBuild");
        queryUtility = new QueryUtility(this, build.getUrl());
        Debug.Log(tag, "build from intent null? " + (build == null));

        adapter = new ArtifactAdapter(this, artifacts);
        addToTitleBar("#" + build.getNumber());

        list_title = (TextView) findViewById(R.id.txt_list_title);
        list_title.setText(getTitleBar(3));

        noArtifactsView = (TextView) findViewById(R.id.txt_no_artifacts);
        list_artifacts = (ListView) findViewById(R.id.list_artifacts);

        if (list_artifacts != null) {
            list_artifacts.setAdapter(adapter);
            list_artifacts.setOnItemClickListener(this);
        } else {
            Debug.Log(tag, "list_artifacts is null.");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (downloadHelper != null) {
            if (!downloadHelper.isDownloadInProgress()) {
                helper.unbindDownloadService();
            }
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

        if (item.getItemId() == R.id.action_refresh) {
            queryUtility.queryServer(true);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onQueryComplete(final WebResponse response) {
        super.onQueryComplete(response);

        Debug.Log(tag, "onQueryComplete()");
        if (response != null) {
            String requestUrl = response.getRequestUrl();

            if (response.getResponseDocument() != null) {
                artifacts = parseArtifacts(response.getResponseDocument());

                if (artifacts.size() == 0) {
                    hideView(list_artifacts);
                    showView(noArtifactsView);
                } else {
                    if (!requestUrl.endsWith("children")) {
                        String parentPath = server.getParentUrl(requestUrl);

                        Debug.Log(tag, "Adding dots for parent: " + parentPath);

                        artifacts.add(0, new TeamCityArtifact("..", "0", parentPath, "DIR"));
                    }
                    updateList(getString(R.string.title_available_files), adapter, artifacts);
                }
            } else {
                // error dialog
                showAlert("Error", response.getStatusReason(this), true);
                Debug.Log(tag, "error: " + response.getStatusReason(this));
            }
        }
    }

    private ArrayList<TeamCityArtifact> parseArtifacts(String xml) {

        ArrayList<TeamCityArtifact> rtnData = new ArrayList<TeamCityArtifact>();

        TeamCityXmlParser parser = new TeamCityXmlParser(xml);

        NodeList rootList = parser.getNodes("files");
        Element fileNode = (Element) rootList.item(0);

        NodeList files = parser.getNodes(fileNode, "file");

        for (int i = 0; i < files.getLength(); i++) {
            Element file = (Element) files.item(i);

            String filename = parser.getAttribute(file, "name");
            String filesize = parser.getAttribute(file, "size");
            String contentUrl = null;
            String childrenUrl = null;
            String url = null;
            String type = "";

            // files can have child nodes, content and children
            NodeList childrenNodes = parser.getNodes(file, "children");
            NodeList contentNodes = parser.getNodes(file, "content");

            // are there children?
            if (childrenNodes != null && childrenNodes.getLength() > 0) {
                Element child = (Element) childrenNodes.item(0);
                childrenUrl = parser.getAttribute(child, "href");
            }

            // content? (no children)
            if (contentNodes != null && contentNodes.getLength() > 0) {
                Element content = (Element) contentNodes.item(0);
                contentUrl = parser.getAttribute(content, "href");
            }

            if ( contentUrl == null && childrenUrl != null) {
                type = "DIR";
                url = childrenUrl;
            } else  if (contentUrl != null) {
                type = "FILE";
                url = contentUrl;
            }

            rtnData.add(new TeamCityArtifact(filename, filesize, url, type));

        }
        stopSpinning();
        return rtnData;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TeamCityArtifact selected = artifacts.get(position);

        if (selected.getType().equals("FILE")) {
            Debug.Log(tag, "clicked: " + selected.getUrl());
            Debug.Log(tag, "fileSize: " + selected.getFilesize());

            String filename = selected.getFilename();

            String storageDirectory = helper.getStorageDirectory();
            if (FileUtility.handlerExists(this, filename)) {
                if (helper.isExternalStorageWritable()) {
                    if (!storageDirectory.startsWith("null")) {
                        if (helper.getBoolPref("pref_multiple_downloads")) {
                            // use DownloadService to download file
                            DownloadRequest request = new DownloadRequest();

                            request.url = serverAddress + selected.getUrl();
                            request.authStr = authStr;
                            request.fileName = filename;
                            request.fileSize = selected.getFilesize();

                            helper.serviceQueueDownload(request);
                        } else {
                            // only click one thing at a time - single download mode
                            list_artifacts.setOnItemClickListener(null);

                            Intent downloadActivity = new Intent(this, DownloadActivity.class);

                            downloadActivity.putExtra("url", serverAddress + selected.getUrl());
                            downloadActivity.putExtra("size", selected.getFilesize());
                            downloadActivity.putExtra("name", filename);
                            downloadActivity.putExtra("authStr", authStr);
                            downloadActivity.putExtra("useGuest", server.useGuest());

                            startActivity(downloadActivity);
                        }
                    } else {
                        // storage error
                        DialogUtility.showAlert(this, getString(R.string.storage_error_title), getString(R.string.storage_error_message));
                    }
                } else {
                    // storage not mounted
                    DialogUtility.showAlert(this, getString(R.string.storage_not_mounted_title), getString(R.string.storage_not_mounted_message));
                }
            } else {
                // file not supported
                DialogUtility.showAlert(this, getString(R.string.error),
                        String.format(getString(R.string.unsupported_file_type),
                                FileUtility.getExtensionFromFile(filename)));
            }
        } else {
            // directory, reload
            if (selected.getFilename().equals("..")) {
                popTitleBar();
            } else {
                addToTitleBar(selected.getFilename());
            }
            list_title.setText(getTitleBar(-1));
            queryUtility.setQuery(selected.getUrl());
            queryUtility.queryServer(false);
        }
    }
}