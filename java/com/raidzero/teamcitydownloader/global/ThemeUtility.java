package com.raidzero.teamcitydownloader.global;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.TypedValue;

import com.raidzero.teamcitydownloader.R;

/**
 * Created by posborn on 6/27/14.
 */
public class ThemeUtility {
    private static final String tag = "ThemeUtility";

    public static void setAppTheme(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String themeName = prefs.getString("theme", "");

        // if it doesnt exist in prefs, create it, as dark
        if (prefs.getString("theme", "").isEmpty()) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("theme", "dark");
            editor.apply();
            themeName = "dark";
        }

        if (themeName.equalsIgnoreCase("light")) {
            context.setTheme(R.style.Light);
        } else {
            context.setTheme(R.style.Dark);
        }
    }

    public static int getDialogActivityTheme(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String themeName = prefs.getString("theme", "");

        // if it doesnt exist in prefs, create it, as dark
        if (prefs.getString("theme", "").isEmpty()) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("theme", "dark");
            editor.apply();
            themeName = "dark";
        }

        if (themeName.equalsIgnoreCase("light")) {
            return R.style.LightDialogActivity;
        } else {
            return R.style.DarkDialogActivity;
        }
    }
    public static int getThemeId(Context context) {
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.themeName, outValue, true);
        if ("Dark".equals(outValue.string)) {
            return R.style.Dark;
        } else {
            return R.style.Light;
        }
    }

    public static int getHoloDialogTheme(Context context) {
        if (getThemeId(context) == R.style.Dark) {
            Debug.Log(tag, "returning dark theme");
            return AlertDialog.THEME_HOLO_DARK;
        } else {
            Debug.Log(tag, "returning light theme");
            return AlertDialog.THEME_HOLO_LIGHT;
        }
    }

    public static Drawable getThemedDrawable(Context context, int resId) {
        TypedArray a = context.getTheme().obtainStyledAttributes(getThemeId(context), new int[]{resId});

        if (a != null) {
            int attributeResourceId = a.getResourceId(0, 0);
            return context.getResources().getDrawable(attributeResourceId);
        }

        return null;
    }

    public static int getThemedColor(Context context, int resId) {
        TypedArray a = context.getTheme().obtainStyledAttributes(getThemeId(context), new int[]{resId});

        if (a != null) {
            int attributeResourceId = a.getResourceId(0, 0);
            return context.getResources().getColor(attributeResourceId);
        }

        return -1;
    }
}
