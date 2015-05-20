package com.raidzero.teamcitydownloader.global;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by posborn on 6/24/14.
 */
public class Regex {
    private static final String tag = "Regex";

    private String line;
    private Pattern pattern;

    public Regex(String line, String strPattern) {
        this.line = line;
        pattern = Pattern.compile(strPattern);
    }

    public Matcher returnMatches() {
        Matcher m = pattern.matcher(line);

        if (m.find()) {
            return m;
        }

        return null;
    }

    public String replaceAll(String replacement) {
        Matcher m = pattern.matcher(line);
        String rtn = m.replaceAll(replacement);

        Debug.Log(tag, "replaceAll: " + rtn);
        return rtn;
    }

}
