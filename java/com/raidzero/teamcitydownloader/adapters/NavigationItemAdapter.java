package com.raidzero.teamcitydownloader.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.raidzero.teamcitydownloader.R;
import com.raidzero.teamcitydownloader.data.NavigationMenuItem;
import com.raidzero.teamcitydownloader.global.Debug;

import java.util.ArrayList;

/**
 * Created by posborn on 6/30/14.
 */
public class NavigationItemAdapter extends ArrayAdapter<NavigationMenuItem> {

    private static final String tag = "NavigationFavoritesAdapter";

    private ArrayList<String> dividersShown = new ArrayList<String>();

    public NavigationItemAdapter(Context context, int resource, int textViewResourceId, ArrayList<NavigationMenuItem> objects) {
        super(context, resource, textViewResourceId, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        NavigationMenuItem item = getItem(position);

        // if we arent reusing a view, inflate one
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_activated_1, parent, false);
        }

        TextView txtName = (TextView) convertView.findViewById(android.R.id.text1);

        if (!item.isDivider()) {
            Debug.Log(tag, "about to load item: " + item.getName() + ", " + item.getObj().toString());
            if (txtName != null) {
                txtName.setText(item.getName());
            } else {
                Debug.Log(tag, "txtName is null for " + item.getName());
            }
        } else {
            // why is this necessary?
            if (!dividersShown.contains(item.getName())) {
                Debug.Log(tag, "about to load divider");

                // return the divider view
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.divider, parent, false);
                TextView txtHeader = (TextView) convertView.findViewById(R.id.nav_divider_header);
                txtHeader.setText(item.getName());
                dividersShown.add(item.getName());

                // dividers should not be clickable
                convertView.setEnabled(false);
                convertView.setOnClickListener(null);
            } else {
                Debug.Log(tag, "divider already shown for " + item.getName());
            }
        }

        Debug.Log(tag, "loaded name: " + item.getName());

        return convertView;
    }
}
