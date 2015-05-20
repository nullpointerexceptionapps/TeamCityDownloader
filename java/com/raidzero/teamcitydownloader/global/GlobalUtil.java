package com.raidzero.teamcitydownloader.global;

import com.raidzero.teamcitydownloader.data.TeamCityProject;

import java.util.ArrayList;

/**
 * Created by posborn on 6/28/14.
 */
public class GlobalUtil {

    private static final String tag = "GlobalUtil";

    public static boolean isProjectSaved(TeamCityProject needle, ArrayList<TeamCityProject> haystack) {
        for (TeamCityProject hay : haystack) {
            if (hay.getUrl().equals(needle.getUrl())) {
                return true;
            }
        }

        return false;
    }
}
