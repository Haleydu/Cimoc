package com.hiroshi.cimoc.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Transient;

/**
 * Created by Hiroshi on 2016/9/1.
 */
@Entity
public class Task implements Parcelable {

    public static final int STATE_FINISH = 0;
    public static final int STATE_PAUSE = 1;
    public static final int STATE_PARSE = 2;
    public static final int STATE_DOING = 3;
    public static final int STATE_WAIT = 4;
    public static final int STATE_ERROR = 5;

    @Id(autoincrement = true) private Long id;
    @NotNull private long key;      // 漫画主键
    @NotNull private String path;
    @NotNull private String title;
    @NotNull private int progress;
    @NotNull private int max;

    @Transient private int source;
    @Transient private String cid;  // 漫画 ID
    @Transient private int state;

    public Task(Parcel source) {
        this.id = source.readLong();
        this.key = source.readLong();
        this.path = source.readString();
        this.title = source.readString();
        this.progress = source.readInt();
        this.max = source.readInt();
        this.source = source.readInt();
        this.cid = source.readString();
        this.state = source.readInt();
    }

    @Generated(hash = 1668809946)
    public Task(Long id, long key, @NotNull String path, @NotNull String title, int progress,
            int max) {
        this.id = id;
        this.key = key;
        this.path = path;
        this.title = title;
        this.progress = progress;
        this.max = max;
    }

    @Generated(hash = 733837707)
    public Task() {
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Task && ((Task) o).id.equals(id);
    }

    @Override
    public int hashCode() {
        return id == null ? super.hashCode() : id.hashCode();
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getKey() {
        return this.key;
    }

    public void setKey(long key) {
        this.key = key;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getProgress() {
        return this.progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getMax() {
        return this.max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getState() {
        return this.state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getSource() {
        return this.source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public boolean isFinish() {
        return max != 0 && progress == max;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(key);
        dest.writeString(path);
        dest.writeString(title);
        dest.writeInt(progress);
        dest.writeInt(max);
        dest.writeInt(source);
        dest.writeString(cid);
        dest.writeInt(state);
    }

    public final static Parcelable.Creator<Task> CREATOR = new Parcelable.Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel source) {
            return new Task(source);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };

}
