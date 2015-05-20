package com.raidzero.teamcitydownloader.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.raidzero.teamcitydownloader.global.Debug;

import org.json.JSONArray;

/**
 * Created by posborn on 6/26/14.
 */
public class TeamCityItem implements Parcelable {
    private static final String tag = "TeamCityConfig";

    private String name;
    private String url;
    private String webUrl;

    public TeamCityItem(String name, String url, String webUrl) {

        this.name = name;
        this.url = url;
        this.webUrl = webUrl;

        //Debug.Log(tag, "instance created. Name: " + name + " URL: " + url);
    }

    public String getName() {
        return this.name;
    }
    public String getUrl() {
        return this.url;
    }
    public String getWebUrl() {
        return this.webUrl;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String serialize() {

        JSONArray array = new JSONArray();

        try {
            array.put(0, name);
            array.put(1, url);
            array.put(2, webUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return array.toString();
    }

    public static TeamCityItem deserialize(String jsonData) {
        try {
            JSONArray jsonArray = new JSONArray(jsonData);

            return new TeamCityItem(jsonArray.getString(0), jsonArray.getString(1), jsonArray.getString(2));

        } catch (Exception e) {
            Debug.Log(tag, "deserialize error", e);
        }

        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(url);
        dest.writeString(webUrl);
    }

    // this is needed to make the parcel
    public TeamCityItem(Parcel source) {
        name = source.readString();
        url = source.readString();
        webUrl = source.readString();
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator<TeamCityItem>() {
        public TeamCityItem createFromParcel(Parcel source) {
            return new TeamCityItem(source);
        }
        public TeamCityItem[] newArray(int size) {
            return new TeamCityItem[size];
        }
    };
}
