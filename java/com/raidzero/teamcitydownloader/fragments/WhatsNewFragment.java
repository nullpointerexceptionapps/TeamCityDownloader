package com.raidzero.teamcitydownloader.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.raidzero.teamcitydownloader.R;
import com.raidzero.teamcitydownloader.adapters.AppRevisionAdapter;
import com.raidzero.teamcitydownloader.data.AppRevision;
import com.raidzero.teamcitydownloader.global.Debug;
import com.raidzero.teamcitydownloader.global.TeamCityXmlParser;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by raidzero on 8/14/14.
 */
public class WhatsNewFragment extends Fragment {
    private static final String tag = "WhatsNewFragment";

    private ListView revisionsList;
    private AppRevisionAdapter adapter;
    private ArrayList<AppRevision> revisions = new ArrayList<AppRevision>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_whats_new, container, false);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        adapter = new AppRevisionAdapter(getActivity(), revisions);
        revisionsList = (ListView) getActivity().findViewById(R.id.list_revisions);

        revisionsList.setAdapter(adapter);

        revisions = parseChanges();

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.clear();
                adapter.addAll(revisions);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private ArrayList<AppRevision> parseChanges() {
        String xml = "";
        ArrayList<AppRevision> rtnData = new ArrayList<AppRevision>();

        try {
            InputStream inputStream = getActivity().getAssets().open("whats_new.xml");

            // get size
            int size = inputStream.available();

            // make a byte buffer of this size
            byte[] buffer = new byte[size];

            // read the file into buffer
            inputStream.read(buffer);
            inputStream.close();

            xml = new String(buffer);

        } catch (Exception e) {
            // nothing
        }

        if (!xml.isEmpty()) {
            // start parsing
            TeamCityXmlParser parser = new TeamCityXmlParser(xml);

            // one root node
            NodeList rootList = parser.getNodes("whats-new");
            Element rootNode = (Element) rootList.item(0);

            NodeList revisions = parser.getNodes(rootNode, "revision");

            for (int i = 0; i < revisions.getLength(); i++) {
                Element revision = (Element) revisions.item(i);
                String version = parser.getAttribute(revision, "version");
                String date = parser.getAttribute(revision, "date");
                ArrayList<String> changes = new ArrayList<String>();

                NodeList changeNodes = parser.getNodes(revision, "change");
                for (int j = 0; j < changeNodes.getLength(); j++) {
                    Element c = (Element) changeNodes.item(j);
                    String changeData = c.getTextContent();
                    changes.add(changeData);
                }

                Debug.Log(tag, "got " + changes.size() + " changes for version " + version);
                rtnData.add(new AppRevision(version, date, changes));
            }
        }

    return rtnData;
    }
}
