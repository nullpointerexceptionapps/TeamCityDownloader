package com.raidzero.teamcitydownloader.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.raidzero.teamcitydownloader.R;
import com.raidzero.teamcitydownloader.data.TeamCityItem;

import java.util.ArrayList;

/**
 * Created by raidzero on 5/8/14 1:17 PM
 */
public class ItemAdapter extends ArrayAdapter<TeamCityItem> {

    private static final String tag="ConfigsListAdapter";

    public ItemAdapter(Context context, ArrayList<TeamCityItem> configs) {
        super(context, R.layout.item_row, configs);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TeamCityItem config = getItem(position);

        // if we arent reusing a view, inflate one
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_row, parent, false);
        }

        // get the views
        TextView txtName = (TextView) convertView.findViewById(R.id.txt_itemName);

        // set their values
        txtName.setText(config.getName());

        // return completed view
        return convertView;
    }
}