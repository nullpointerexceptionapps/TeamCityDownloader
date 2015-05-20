package com.raidzero.teamcitydownloader.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.raidzero.teamcitydownloader.R;
import com.raidzero.teamcitydownloader.data.TeamCityProject;
import com.raidzero.teamcitydownloader.global.AppHelper;
import com.raidzero.teamcitydownloader.global.Common;
import com.raidzero.teamcitydownloader.global.Debug;
import com.raidzero.teamcitydownloader.global.GlobalUtil;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by raidzero on 5/8/14 1:17 PM
 */
public class ProjectAdapter extends ArrayAdapter<TeamCityProject> {

    private static final String tag="ProjectAdapter";

    private boolean enableCheckbox = false;
    private boolean checkboxes[] = null;
    private AppHelper helper;

    public ProjectAdapter(Context context, ArrayList<TeamCityProject> projects, boolean checkEnabled) {
        super(context, R.layout.project_row, projects);

        helper = Common.getApphelper();
        this.enableCheckbox = checkEnabled;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // get AppDevice for this position
        TeamCityProject p = getItem(position);

        // if we arent reusing a view, inflate one
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.project_row, parent, false);
        }

        // get the views
        TextView txtName = (TextView) convertView.findViewById(R.id.txt_projectName);
        final CheckBox chkBox = (CheckBox) convertView.findViewById(R.id.chk_projectEnable);

        if (enableCheckbox) {
            if (checkboxes.length > 0) {
                chkBox.setChecked(checkboxes[position]);
            }

            chkBox.setTag(position);

            chkBox.setOnClickListener(new CompoundButton.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkboxes[(Integer) v.getTag()] = chkBox.isChecked();
                }
            });

        } else {
            // hide checkbox
            chkBox.setVisibility(View.GONE);
        }

        // set their values
        txtName.setText(p.getName());
        Debug.Log(tag, "displaying project: " + p.getName());

        // return completed view
        return convertView;
    }

    @Override
    public void addAll(Collection<? extends TeamCityProject> projects) {
        super.addAll(projects);

        int numProjects = projects.size();
        checkboxes = new boolean[numProjects];

        // get saved projects straight from activity
        ArrayList<TeamCityProject> alreadySavedProjects = helper.getSavedProjects();

        int i = 0;
        for (TeamCityProject current : projects) {
            checkboxes[i] = GlobalUtil.isProjectSaved(current, alreadySavedProjects);
            i++;
        }
    }

    public ArrayList<TeamCityProject> getCheckedProjects() {
        ArrayList<TeamCityProject> rtn = new ArrayList<TeamCityProject>();

        if (checkboxes != null) {
            int numCheckboxes = checkboxes.length;

            for (int i = 0; i < numCheckboxes; i++) {
                if (checkboxes[i]) {
                    rtn.add(getItem(i));
                }
            }
        }
        return rtn;
    }
}