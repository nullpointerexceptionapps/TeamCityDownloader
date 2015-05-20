package com.raidzero.teamcitydownloader.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.raidzero.teamcitydownloader.R;
import com.raidzero.teamcitydownloader.data.TeamCityBuild;

import java.util.ArrayList;

/**
 * Created by raidzero on 5/8/14 1:17 PM
 */
public class BuildAdapter extends ArrayAdapter<TeamCityBuild> {

    private static final String tag="BuildAdapter";
    private boolean displayedDifferentBranches = false;
    private Context context;

    public BuildAdapter(Context context, ArrayList<TeamCityBuild> builds) {
        super(context, R.layout.build_row, builds);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TeamCityBuild build = getItem(position);

        // if we arent reusing a view, inflate one
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.build_row, parent, false);
        }

        // get the views
        TextView txtName = (TextView) convertView.findViewById(R.id.txt_buildNumber);
        TextView txtBranch = (TextView) convertView.findViewById(R.id.txt_branchName);
        TextView txtStatus = (TextView) convertView.findViewById(R.id.txt_buildStatus);

        if (build.getStatus() != null) {
            if (build.getStatus().equalsIgnoreCase("FAILURE")) {
                txtStatus.setTextColor(getContext().getResources().getColor(R.color.color_build_failed));
            } else {
                txtStatus.setTextColor(getContext().getResources().getColor(R.color.color_build_success));
            }
            txtStatus.setText(build.getStatus());
        }

        // set their values
        txtName.setText("#" + build.getNumber());
        String branchName = build.getBranch();
        if (!branchName.isEmpty()) {
            txtBranch.setText(branchName);
            displayedDifferentBranches = true;
        } else if (displayedDifferentBranches) {
            txtBranch.setText(context.getResources().getString(R.string.default_branch));
        }

        // return completed view
        return convertView;
    }
}