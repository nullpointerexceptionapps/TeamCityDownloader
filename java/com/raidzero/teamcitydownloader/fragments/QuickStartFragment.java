package com.raidzero.teamcitydownloader.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.raidzero.teamcitydownloader.R;
import com.raidzero.teamcitydownloader.activities.WelcomeActivity;
import com.raidzero.teamcitydownloader.global.AppHelper;
import com.raidzero.teamcitydownloader.global.Common;
import com.raidzero.teamcitydownloader.global.Debug;

import java.io.InputStream;

/**
 * Created by raidzero on 8/14/14.
 */
public class QuickStartFragment extends Fragment implements View.OnClickListener {
    private static final String tag = "QuickStartFragment";

    private TextView welcomeTextView;
    private String welcomeText;
    private TextView txt_contact_email;
    private String emailSubject;
    private TextView txt_footer;
    private AppHelper helper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_quick_start, container, false);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        helper = Common.getApphelper();
        welcomeText = readWelcomeFile();
        welcomeTextView = (TextView) getActivity().findViewById(R.id.txt_welcome_text);
        txt_contact_email = (TextView) getActivity().findViewById(R.id.txt_contact_email);

        txt_footer = (TextView) getActivity().findViewById(R.id.txt_quick_start_footer);
        txt_contact_email.setText(Html.fromHtml("<u>" + Common.emailAddress + "</u>"));

        txt_contact_email.setOnClickListener(this);
        if (welcomeText != null) {
            welcomeTextView.setText(welcomeText);
        }

        if (!helper.isFirstRun()) {
            txt_footer.setVisibility(View.GONE);
        }

    }

    private String readWelcomeFile() {
        try {
            InputStream inputStream = getActivity().getAssets().open("quick_start.txt");

            // get size
            int size = inputStream.available();

            // make a byte buffer of this size
            byte[] buffer = new byte[size];

            // read the file into buffer
            inputStream.read(buffer);
            inputStream.close();

            // return the string
            return new String(buffer);
        } catch (Exception e) {
            Debug.Log(tag, "assets load error: ", e);
        }

        return null;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == txt_contact_email.getId()) {
            emailSubject = getActivity().getString(R.string.contact_email_subject);
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);

            emailIntent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
            emailIntent.setData(Uri.parse("mailto:" + Common.emailAddress));
            emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(emailIntent);
            ((WelcomeActivity) getActivity()).leave();
        }
    }
}
