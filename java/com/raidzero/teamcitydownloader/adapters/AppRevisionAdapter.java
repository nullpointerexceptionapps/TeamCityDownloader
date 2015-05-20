package com.raidzero.teamcitydownloader.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.raidzero.teamcitydownloader.R;
import com.raidzero.teamcitydownloader.data.AppRevision;

import java.util.ArrayList;

/**
 * Created by raidzero on 5/8/14 1:17 PM
 */
public class AppRevisionAdapter extends ArrayAdapter<AppRevision> {

    private static final String tag="ConfigsListAdapter";
    private Context context;
    
    public AppRevisionAdapter(Context context, ArrayList<AppRevision> revisions) {
        super(context, R.layout.item_row, revisions);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        AppRevision revision = getItem(position);
        // if we arent reusing a view, inflate one
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.whats_new_revision, parent, false);
        }

        // get the views
        TextView txtVersion = (TextView) convertView.findViewById(R.id.txt_revision_version);
        TextView txtDate = (TextView) convertView.findViewById(R.id.txt_revision_date);
        TextView changesList = (TextView) convertView.findViewById(R.id.txt_revision_changes);
            
        // set their values
        txtVersion.setText("v" + revision.getVersionName());
        txtDate.setText(revision.getDate());

        StringBuilder sb = new StringBuilder();
        ArrayList<String> changes = revision.getChanges();

        int numChanges = changes.size();
        int changeNum = 0;
        for (String c : revision.getChanges()) {
            sb.append("• ").append(c);
            // only add new line if not the last change in the list
            if (changeNum++ < numChanges) {
                sb.append("\n");
            }
        }

        changesList.setText(sb.toString());

        // return completed view
        return convertView;
    }

    // make view not clickable
    @Override
    public boolean isEnabled(int position) {
        return false;
    }
}