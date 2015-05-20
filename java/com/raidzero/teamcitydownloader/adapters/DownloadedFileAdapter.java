package com.raidzero.teamcitydownloader.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.raidzero.teamcitydownloader.R;
import com.raidzero.teamcitydownloader.data.DownloadedFile;

import java.util.ArrayList;

/**
 * Created by raidzero on 5/8/14 1:17 PM
 */
public class DownloadedFileAdapter extends ArrayAdapter<DownloadedFile> {

    private static final String tag="DownloadedFileAdapter";

    public DownloadedFileAdapter(Context context, ArrayList<DownloadedFile> files, int numToBold) {
        super(context, R.layout.downloaded_file_row, files);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        DownloadedFile file = getItem(position);

        // if we arent reusing a view, inflate one
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.downloaded_file_row, parent, false);
        }

        // get the views
        TextView txtName = (TextView) convertView.findViewById(R.id.downloaded_file_name);
        TextView txtDate = (TextView) convertView.findViewById(R.id.downloaded_file_date);
        ImageView iconView = (ImageView) convertView.findViewById(R.id.downloaded_file_icon);
        ProgressBar progressBar = (ProgressBar) convertView.findViewById(R.id.downloaded_file_progress);

        // set their values
        txtName.setText(file.getFileName());
        txtDate.setText(file.getLastModDate().toString());

        if (file.isNew()) {
            txtName.setTypeface(null, Typeface.BOLD_ITALIC);
            txtDate.setTypeface(null, Typeface.BOLD_ITALIC);
        } else {
            txtName.setTypeface(null, Typeface.NORMAL);
            txtDate.setTypeface(null, Typeface.NORMAL);
        }

        if (file.isDownloading()) {
            progressBar.setVisibility(View.VISIBLE);
            txtDate.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            txtDate.setVisibility(View.VISIBLE);
        }

        Bitmap icon = file.getBmpIcon();
        if (icon != null) {
            iconView.setImageBitmap(icon);
        }

        // return completed view
        return convertView;
    }
}