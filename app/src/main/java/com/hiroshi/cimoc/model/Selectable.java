package com.hiroshi.cimoc.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public class Selectable implements Parcelable, Cloneable {

    private boolean disable;
    private boolean checked;
    private long id;
    private String title;

    public Selectable(Parcel source) {
        this.disable = source.readByte() == 1;
        this.checked = source.readByte() == 1;
        this.id =  source.readLong();
        this.title = source.readString();
    }

    public Selectable(boolean disable, boolean checked, long id, String title) {
        this.disable = disable;
        this.checked = checked;
        this.id = id;
        this.title = title;
    }

    public Selectable(boolean disable, boolean checked, String title) {
        this(disable, checked, -1, title);
    }

    public boolean isDisable() {
        return disable;
    }

    public void setDisable(boolean disable) {
        this.disable = disable;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (disable ? 1 : 0));
        dest.writeByte((byte) (checked ? 1 : 0));
        dest.writeLong(id);
        dest.writeString(title);
    }

    public final static Parcelable.Creator<Selectable> CREATOR = new Parcelable.Creator<Selectable>() {
        @Override
        public Selectable createFromParcel(Parcel source) {
            return new Selectable(source);
        }

        @Override
        public Selectable[] newArray(int size) {
            return new Selectable[size];
        }
    };

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
