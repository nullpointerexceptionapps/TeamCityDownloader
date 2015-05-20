package com.raidzero.teamcitydownloader.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by posborn on 6/26/14.
 */
public class TeamCityBuild implements Parcelable {
    private static final String tag = "TeamCityBuild";

    private String number;
    private String status;
    private String url;
    private String webUrl;
    private String branch;

    public TeamCityBuild(String number, String status, String url, String branch, String weburl) {
        this.number = number;
        this.status = status;
        this.url = url;
        this.branch = branch;
        this.webUrl = weburl;

        //Debug.Log(tag, "instance created. Number: " + number + " Status: " + status + " URL: " + url);
    }

    public String getNumber() {
        return this.number;
    }
    public String getStatus() {
        return this.status;
    }
    public String getUrl() {
        return this.url;
    }
    public String getWebUrl() {
        return this.webUrl;
    }

    public String getBranch() {
        if (branch.isEmpty()) {
            return "";
        } else {
            return branch;
        }
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
        dest.writeString(number);
        dest.writeString(status);
        dest.writeString(url);
        dest.writeString(branch);
        dest.writeString(webUrl);
    }

    // this is needed to make the parcel
    public TeamCityBuild(Parcel source) {
        number = source.readString();
        status = source.readString();
        url = source.readString();
        branch = source.readString();
        webUrl = source.readString();
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator<TeamCityBuild>() {
        public TeamCityBuild createFromParcel(Parcel source) {
            return new TeamCityBuild(source);
        }
        public TeamCityBuild[] newArray(int size) {
            return new TeamCityBuild[size];
        }
    };
}
