package com.hiroshi.cimoc.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Hiroshi on 2016/7/2.
 */
public class Chapter implements Parcelable {

    private String title;
    private String path;
    private int count;

    public Chapter(String title, String path) {
        this.title = title;
        this.path = path;
        this.count = 0;
    }

    public Chapter(Parcel source) {
        this.title = source.readString();
        this.path = source.readString();
        this.count = source.readInt();
    }

    public String getTitle() {
        return title;
    }

    public String getPath() {
        return path;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(path);
        dest.writeInt(count);
    }

    public final static Parcelable.Creator<Chapter> CREATOR = new Parcelable.Creator<Chapter>() {
        @Override
        public Chapter createFromParcel(Parcel source) {
            return new Chapter(source);
        }

        @Override
        public Chapter[] newArray(int size) {
            return new Chapter[size];
        }
    };

}
