package com.raidzero.teamcitydownloader.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.raidzero.teamcitydownloader.R;

import java.util.ArrayList;

/**
 * Created by raidzero on 5/8/14 1:17 PM
 */
public class ConfigErrorAdapter extends ArrayAdapter<String> {

    private static final String tag="ConfigsListAdapter";

    public ConfigErrorAdapter(Context context, ArrayList<String> errors) {
        super(context, R.layout.item_row, errors);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        String error = getItem(position);

        // if we arent reusing a view, inflate one
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.error_row, parent, false);
        }

        // get the views
        TextView txtName = (TextView) convertView.findViewById(R.id.txt_itemName);

        // set their values
        txtName.setText("• " + error);

        // return completed view
        return convertView;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }
}