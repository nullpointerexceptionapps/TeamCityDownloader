package com.raidzero.teamcitydownloader.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.raidzero.teamcitydownloader.R;
import com.raidzero.teamcitydownloader.activities.ControllerActivity;
import com.raidzero.teamcitydownloader.activities.MainActivity;
import com.raidzero.teamcitydownloader.adapters.ProjectAdapter;
import com.raidzero.teamcitydownloader.data.TeamCityProject;
import com.raidzero.teamcitydownloader.global.Debug;

import java.util.ArrayList;

/**
 * Created by posborn on 6/26/14.
 */
public class ProjectsListFragment extends BaseTeamCityFragment implements AdapterView.OnItemClickListener {
    private static final String tag = "ProjectsListFragment";

    private ArrayList<TeamCityProject> projects = new ArrayList<TeamCityProject>();
    private ProjectAdapter adapter;

    private ListView list_projects;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragment_title = getActivity().getString(R.string.title_main);
        return inflater.inflate(R.layout.projects_list, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.home, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        super.onResume();

        adapter = new ProjectAdapter(getActivity(), projects, false);

        ((MainActivity) activity).setNavItemSelection(0);

        list_projects = (ListView) activity.findViewById(R.id.list_projects);
        TextView txtNoprojects = (TextView) activity.findViewById(R.id.txt_no_projects);

        list_projects.setAdapter(adapter);
        list_projects.setOnItemClickListener(this);

        boolean projectsFound = displayProjects();
        Debug.Log(tag, "projectsFound: " + projectsFound);

        if (projectsFound) {
            list_projects.setVisibility(View.VISIBLE);
            txtNoprojects.setVisibility(View.GONE);
        } else {
            list_projects.setVisibility(View.GONE);
            txtNoprojects.setVisibility(View.VISIBLE);
        }
    }

    private boolean displayProjects() {
        Debug.Log(tag, "displayProjects()");
        projects = getSavedProjects();
        if (projects != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.clear();
                    adapter.addAll(projects);
                    adapter.notifyDataSetChanged();
                }
            });
            Debug.Log(tag, "displaying " + projects.size() + " projects");
        } else {
            return false;
        }

        return (projects.size() > 0);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TeamCityProject selected = projects.get(position);

        Debug.Log(tag, "clicked: " + selected.getName());

        Intent i = new Intent(activity, ControllerActivity.class);
        i.putExtra("showConfigs", true);
        i.putExtra("selectedProject", selected);

        startActivity(i);
    }


}
