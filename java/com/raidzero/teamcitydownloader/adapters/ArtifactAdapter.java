package com.raidzero.teamcitydownloader.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.raidzero.teamcitydownloader.R;
import com.raidzero.teamcitydownloader.data.TeamCityArtifact;
import com.raidzero.teamcitydownloader.global.Debug;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by raidzero on 5/8/14 1:17 PM
 */
public class ArtifactAdapter extends ArrayAdapter<TeamCityArtifact> {

    private static final String tag="ArtifactAdapter";

    public ArtifactAdapter(Context context, ArrayList<TeamCityArtifact> artifacts) {
        super(context, R.layout.artifact_row, artifacts);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TeamCityArtifact artifact = getItem(position);
        String type = artifact.getType();

        Debug.Log(tag, "item: " + artifact.getFilename());

        // if we arent reusing a view, inflate one
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.artifact_row, parent, false);
        }

        // get the views
        TextView txtFilename = (TextView) convertView.findViewById(R.id.txt_filename);
        TextView txtFilesize = (TextView) convertView.findViewById(R.id.txt_filesize);

        int bytes = artifact.getFilesize();
        String displaySize;

        if (type.equals("FILE")) {
            Float mb = (float) bytes / 1024 / 1024;
            Float kb = (float) bytes / 1024;

            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits(2);

            String mbText = df.format(mb) + " MB";
            String kbText = df.format(kb) + " KB";
            String bText = bytes + " B";

            displaySize = mbText;

            if (mb < 1) {
                displaySize = kbText;
            }

            if (kb < 1) {
                displaySize = bText;
            }
        } else {
            // directory
            displaySize = "Directory";
        }

        if (artifact.getFilename().equals("..")) {
            displaySize = "Up to Parent Directory";
        }

        // set their values
        txtFilename.setText(artifact.getFilename());
        txtFilesize.setText(displaySize);

        // return completed view
        return convertView;
    }
}