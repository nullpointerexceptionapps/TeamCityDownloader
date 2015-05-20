package com.raidzero.teamcitydownloader.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by posborn on 6/26/14.
 */
public class TeamCityArtifact implements Parcelable {
    private static final String tag = "TeamCityArtifact";


    private String filename;
    private int filesize;
    private String url;
    private String type;

    public TeamCityArtifact(String filename, String filesize, String url, String type) {

        this.filename = filename;
        if (filesize != null && !filesize.isEmpty()) {
            this.filesize = Integer.valueOf(filesize);
        }
        this.url = url;
        this.type = type;

        //Debug.Log(tag, "instance created. filename: " + filename + "Size: " + filesize + " URL: " + url);
    }

    public String getFilename() {
        return this.filename;
    }
    public int getFilesize() {
        return this.filesize;
    }
    public String getUrl() {
        return this.url;
    }
    public String getType() {
        return type;
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
        dest.writeString(filename);
        dest.writeInt(filesize);
        dest.writeString(url);
        dest.writeString(type);
    }

    // this is needed to make the parcel
    public TeamCityArtifact(Parcel source) {
        filename = source.readString();
        filesize = source.readInt();
        url = source.readString();
        type = source.readString();
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator<TeamCityArtifact>() {
        public TeamCityArtifact createFromParcel(Parcel source) {
            return new TeamCityArtifact(source);
        }
        public TeamCityArtifact[] newArray(int size) {
            return new TeamCityArtifact[size];
        }
    };
}
