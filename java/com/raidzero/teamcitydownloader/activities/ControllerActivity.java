package com.raidzero.teamcitydownloader.activities;

import android.app.Activity;
import android.content.Intent;

import com.raidzero.teamcitydownloader.R;
import com.raidzero.teamcitydownloader.data.TeamCityArtifact;
import com.raidzero.teamcitydownloader.data.TeamCityBuild;
import com.raidzero.teamcitydownloader.data.TeamCityItem;
import com.raidzero.teamcitydownloader.data.TeamCityProject;
import com.raidzero.teamcitydownloader.global.Common;
import com.raidzero.teamcitydownloader.global.Debug;

/**
 * Created by posborn on 8/5/14.
 */
public class ControllerActivity extends Activity {
    private static final String tag = "ControllerActivity";

    boolean showConfigs = false;
    boolean gotoStarred = false;

    TeamCityProject selectedProject;
    TeamCityItem selectedBuildConfig;
    TeamCityBuild selectedBuild;
    TeamCityArtifact selectedArtifact;

    Intent buildConfigsListIntent = null;
    Intent buildsListIntent = null;
    Intent buildInfoListIntent = null;
    Intent artifactsListIntent = null;

    private String selectedBranch;
    private boolean showBuildInfo;
    private int activityDepth = 0;

    // assume we just want to start the build configs list
    @Override
    public void onResume() {
        super.onResume();
        Debug.Log(tag, "onResume()");

        Intent intent = getIntent();
        showConfigs = intent.getBooleanExtra("showConfigs", false);
        gotoStarred = intent.getBooleanExtra("gotoStarred", false);

        Debug.Log(tag, "showConfigs: " + showConfigs + " gotoStarred: " + gotoStarred);
        if (showConfigs) {
            selectedProject = intent.getParcelableExtra("selectedProject");
            if (selectedProject != null) {
                Debug.Log(tag, "starting BuildConfigsListActivity");
                buildConfigsListIntent = new Intent(this, BuildConfigsListActivity.class);
                buildConfigsListIntent.putExtra("selectedProject", selectedProject);
                animateStartActivity(buildConfigsListIntent, Common.REQUEST_CODE_LIST_BUILD_CONFIGS, false);
                intent.removeExtra("selectedProject");
            }
        }

        if (gotoStarred) {
            selectedBuildConfig = intent.getParcelableExtra("selectedBuildConfig");
            boolean forceRefresh = intent.getBooleanExtra("forceRefresh", false);
            if (selectedBuildConfig != null) {
                gotoStarred = false;
                intent.removeExtra("gotoStarred");
                buildsListIntent = new Intent(this, BuildsListActivity.class);
                buildsListIntent.putExtra("selectedBuildConfig", selectedBuildConfig);
                buildsListIntent.putExtra("cameFromHome", true);
                buildsListIntent.putExtra("forceRefresh", forceRefresh);
                animateStartActivity(buildsListIntent, Common.REQUEST_CODE_LIST_BUILDS, false);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Debug.Log(tag, "onActivityResult(" + requestCode + ", " + resultCode +")");
        switch (requestCode) {
            case Common.REQUEST_CODE_LIST_BUILD_CONFIGS:
                Debug.Log(tag, "onActivityResult(Common.REQUEST_CODE_LIST_BUILD_CONFIGS" +  ", " + resultCode +")");
                if (resultCode == RESULT_OK) {
                    selectedBuildConfig = data.getParcelableExtra("selectedBuildConfig");
                    Debug.Log(tag, "starting BuildsListActivity for url " + selectedBuildConfig.getUrl());

                    // start build list activity
                    if (buildsListIntent == null) {
                        buildsListIntent = new Intent(this, BuildsListActivity.class);
                    }
                    buildsListIntent.putExtra("selectedBuildConfig", selectedBuildConfig);
                    buildsListIntent.putExtra("selectedBranch", "");
                    animateStartActivity(buildsListIntent, Common.REQUEST_CODE_LIST_BUILDS, false);
                } else if (resultCode == RESULT_CANCELED) {
                    Debug.Log(tag, "returning to MainActivity from config list");
                    finish();
                    overridePendingTransition(R.anim.left_in, R.anim.right_out);
                }
                break;
            case Common.REQUEST_CODE_LIST_BUILDS:
                Debug.Log(tag, "onActivityResult(Common.REQUEST_CODE_LIST_BUILDS" + ", " + resultCode +")");
                if (resultCode == RESULT_OK) {
                    showBuildInfo = data.getBooleanExtra("showBuildInfo", false);
                    selectedBuild = data.getParcelableExtra("selectedBuild");
                    selectedBranch = data.getStringExtra("selectedBranch");

                    if (showBuildInfo) {
                        if (buildInfoListIntent == null) {
                            buildInfoListIntent = new Intent(this, BuildInfoActivity.class);
                        }
                        buildInfoListIntent.putExtra("selectedBuild", selectedBuild);
                        animateStartActivity(buildInfoListIntent, Common.REQUEST_CODE_LIST_INFO, false);
                    } else {
                        Debug.Log(tag, "starting ArtifactsListActivity for url " + selectedBuild.getUrl());
                        Debug.Log(tag, "selectedBranch: " + selectedBranch);
                        // start artifact list activity
                        if (artifactsListIntent == null) {
                            artifactsListIntent = new Intent(this, ArtifactsBrowserActivity.class);
                        }
                        artifactsListIntent.putExtra("selectedBuild", selectedBuild);
                        animateStartActivity(artifactsListIntent, Common.REQUEST_CODE_LIST_ARTIFACTS, false);
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    // start build config list activity
                    if (buildConfigsListIntent == null) {
                        buildConfigsListIntent = new Intent(this, BuildConfigsListActivity.class);
                    }
                    animateStartActivity(buildConfigsListIntent, Common.REQUEST_CODE_LIST_BUILD_CONFIGS, true);
                }
                break;
            case Common.REQUEST_CODE_LIST_INFO:
                // this always goes back to the builds list
                showBuildInfo = false;
                buildsListIntent.putExtra("selectedBranch", selectedBranch);
                animateStartActivity(buildsListIntent, Common.REQUEST_CODE_LIST_BUILDS, true);
                break;
            case Common.REQUEST_CODE_LIST_ARTIFACTS:
                Debug.Log(tag, "onActivityResult(Common.REQUEST_CODE_LIST_ARTIFACTS, " + resultCode + ")");
                if (resultCode == RESULT_OK) {
                    selectedArtifact = data.getParcelableExtra("selectedArtifact");
                    if (selectedArtifact != null) {
                        Debug.Log(tag, "starting ArtifactList on dir");
                        artifactsListIntent.putExtra("selectedArtifact", selectedArtifact);
                        animateStartActivity(artifactsListIntent, Common.REQUEST_CODE_LIST_ARTIFACTS, false);
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    // start builds list activity
                    if (buildsListIntent == null) {
                        buildsListIntent = new Intent(this, BuildsListActivity.class);
                    }
                    Debug.Log(tag, "returning to builds with branch: " + selectedBranch);
                    buildsListIntent.putExtra("selectedBranch", selectedBranch);
                    animateStartActivity(buildsListIntent, Common.REQUEST_CODE_LIST_BUILDS, true);
                }
                break;
        }
    }

    private void animateStartActivity(Intent intent, int requestCode, boolean reverseAnimation) {
        if (reverseAnimation) {
            activityDepth--;
        } else {
            activityDepth++;
        }

        if (activityDepth < 1) {
            Debug.Log(tag, "No activities left, returning to main");
            finish();
        } else {
            startActivityForResult(intent, requestCode);
        }
        if (!reverseAnimation) {
            overridePendingTransition(R.anim.right_in, R.anim.left_out);
        } else {
            overridePendingTransition(R.anim.left_in, R.anim.right_out);
        }
    }
}
