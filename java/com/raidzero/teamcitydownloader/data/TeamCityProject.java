package com.raidzero.teamcitydownloader.data;


import android.os.Parcel;
import android.os.Parcelable;

import com.raidzero.teamcitydownloader.global.Debug;

import org.json.JSONArray;

/**
 * Created by posborn on 6/24/14.
 */
public class TeamCityProject implements Parcelable {

    private static final String tag = "TeamCityProject";

    private String id;
    private String name;
    private String url;

    public TeamCityProject(String id, String name, String url) {
        this.id = id;
        this.name = name;
        this.url = url;

        //Debug.Log(tag, "instance created. Name: " + name + " ID: " + id + " URL: " + url);
    }

    public String getName() {
        return this.name;
    }

    public String getId() {
        return this.id;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(id);
        dest.writeString(url);
    }

    // this is needed to make the parcel
    public TeamCityProject(Parcel source) {
        name = source.readString();
        id = source.readString();
        url = source.readString();
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator<TeamCityProject>() {
        public TeamCityProject createFromParcel(Parcel source) {
            return new TeamCityProject(source);
        }
        public TeamCityProject[] newArray(int size) {
            return new TeamCityProject[size];
        }
    };

    public String serialize() {

        JSONArray array = new JSONArray();

        try {
            array.put(0, name);
            array.put(1, id);
            array.put(2, url);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return array.toString();
    }

    public static TeamCityProject deserialize(String jsonData) {
        try {
            JSONArray jsonArray = new JSONArray(jsonData);

            return new TeamCityProject(jsonArray.getString(1), jsonArray.getString(0), jsonArray.getString(2));

        } catch (Exception e) {
            Debug.Log(tag, "deserialize error", e);
        }

        return null;
    }
}
