package com.raidzero.teamcitydownloader.global;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.ContextThemeWrapper;
import android.widget.Toast;

/**
 * Created by posborn on 6/26/14.
 */
public class DialogUtility {
    public static final String tag = "DialogUtility";

    public static void showAlert(Context context, String title, String message) {
        int dialogTheme = ThemeUtility.getHoloDialogTheme(context);

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, dialogTheme));

        builder.setTitle(title);
        builder.setMessage(message);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void showAlertWithButton(Context context, String title, String message, DialogInterface.OnClickListener clickListener) {
        int dialogTheme = ThemeUtility.getHoloDialogTheme(context);

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, dialogTheme));

        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(context.getResources().getString(android.R.string.ok), clickListener);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void makeToast(Context context, String msg) {
        Toast toast;
        // use getApplicationContext so the toast is styled as default even when on the light theme
        toast = Toast.makeText(context.getApplicationContext(), msg, Toast.LENGTH_SHORT);
        toast.show();
    }
}
