package com.hiroshi.cimoc.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Hiroshi on 2016/7/2.
 */
public class Chapter implements Parcelable {

    String title;
    String path;

    public static final Creator<Chapter> CREATOR = new Creator<Chapter>() {
        @Override
        public Chapter createFromParcel(Parcel source) {
            return new Chapter(source);
        }
        @Override
        public Chapter[] newArray(int size) {
            return new Chapter[0];
        }
    };

    public Chapter(String title, String path) {
        this.title = title;
        this.path = path;
    }

    public Chapter(Parcel src) {
        this.title = src.readString();
        this.path = src.readString();
    }

    public String getTitle() {
        return title;
    }

    public String getPath() {
        return path;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(path);
    }

}
