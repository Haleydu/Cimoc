package com.hiroshi.cimoc.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by Hiroshi on 2016/7/2.
 * fixed by Haleydu on 2020/8/25.
 */
@Entity
public class Chapter implements Parcelable {

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
    @Id(autoincrement = true)
    private Long id;
    @NotNull
    private Long sourceComic;
    private String title;
    private String path;
    private int count;
    private boolean complete;
    private boolean download;
    private long tid;

    public Chapter(Long id,Long sourceComic,String title, String path, long tid) {
        this(id,sourceComic,title, path, 0, false, false, tid);
    }

    public Chapter(Long id,Long sourceComic,String title, String path) {
        this(id,sourceComic,title, path, 0, false, false, -1);
    }

    public Chapter(String title, String path) {
        this.title = title;
        this.path = path;
        this.count = 0;
        this.complete = false;
        this.download = false;
        this.tid = -1;
    }
    
    public Chapter(Parcel source) {
        this(source.readLong(), source.readLong(),source.readString(), source.readString(), source.readInt(), source.readByte() == 1, source.readByte() == 1, source.readLong());
    }

    @Generated(hash = 342970835)
    public Chapter(Long id, @NotNull Long sourceComic, String title, String path, int count, boolean complete, boolean download, long tid) {
        this.id = id;
        this.sourceComic = sourceComic;
        this.title = title;
        this.path = path;
        this.count = count;
        this.complete = complete;
        this.download = download;
        this.tid = tid;
    }

    @Generated(hash = 393170288)
    public Chapter() {
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

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public boolean isDownload() {
        return download;
    }

    public void setDownload(boolean download) {
        this.download = download;
    }

    public long getTid() {
        return tid;
    }

    public void setTid(long tid) {
        this.tid = tid;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSourceComic() {
        return sourceComic;
    }

    public void setSourceComic(Long sourceComic) {
        this.sourceComic = sourceComic;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Chapter && ((Chapter) o).path.equals(path);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id !=null) {
            dest.writeLong(id);
        }else {
            dest.writeLong(0L);
        }
        if (sourceComic !=null){
            dest.writeLong(sourceComic);
        } else {
            dest.writeLong(0L);
        }
        dest.writeString(title);
        dest.writeString(path);
        dest.writeInt(count);
        dest.writeByte((byte) (complete ? 1 : 0));
        dest.writeByte((byte) (download ? 1 : 0));
        dest.writeLong(tid);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean getComplete() {
        return this.complete;
    }

    public boolean getDownload() {
        return this.download;
    }

}
