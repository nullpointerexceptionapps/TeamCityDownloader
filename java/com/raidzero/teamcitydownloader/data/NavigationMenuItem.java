package com.raidzero.teamcitydownloader.data;

import com.raidzero.teamcitydownloader.global.Debug;

/**
 * Created by posborn on 6/30/14.
 */
public class NavigationMenuItem {
    private static final String tag = "NavigationMenuItem";

    private String displayName;
    private Object obj;

    private boolean isDivider = false;
    private boolean isFavorite = false;

    public NavigationMenuItem(Object o, String name) {
        this.obj = o;
        this.displayName = name;
    }

    public void setDivider(boolean isDivider) {
        this.isDivider = isDivider;
    }

    public void setFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public String getName() {
        return displayName;
    }

    public Object getObj() {
        return obj;
    }

    public boolean isDivider() {
        return isDivider;
    }

    public boolean isFavorite() {
        Debug.Log(tag, displayName + " isFavorite? " + isFavorite );
        return isFavorite;
    }
}
