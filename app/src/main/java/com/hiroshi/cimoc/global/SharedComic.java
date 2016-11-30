package com.hiroshi.cimoc.global;

import com.hiroshi.cimoc.core.manager.ComicManager;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.MiniComic;

/**
 * Created by Hiroshi on 2016/11/14.
 */

public class SharedComic {

    private static SharedComic instance;

    private Comic mCurrentComic;
    private ComicManager mComicManager;

    private SharedComic() {
        mComicManager = ComicManager.getInstance();
    }

    public MiniComic minify() {
        return new MiniComic(mCurrentComic);
    }

    public Comic get() {
        return mCurrentComic;
    }

    public String last() {
        return mCurrentComic.getLast();
    }

    public Integer page() {
        return mCurrentComic.getPage();
    }

    public int source() {
        return mCurrentComic.getSource();
    }

    public String cid() {
        return mCurrentComic.getCid();
    }

    public String title() {
        return mCurrentComic.getTitle();
    }

    public boolean isDownload() {
        return mCurrentComic.getDownload() != null;
    }

    public boolean isFavorite() {
        return mCurrentComic.getFavorite() != null;
    }

    public boolean isHistory() {
        return mCurrentComic.getHistory() != null;
    }

    public boolean isHighLight() {
        return mCurrentComic.getHighlight();
    }

    public Long id() {
        return mCurrentComic.getId();
    }

    public void open(long id) {
        mCurrentComic = mComicManager.load(id);
    }

    public void open(int source, String cid) {
        mCurrentComic = mComicManager.load(source, cid);
    }

    public void open(Comic comic) {
        mCurrentComic = comic;
    }

    public void insert() {
        if (mCurrentComic.getId() == null) {
            long id = mComicManager.insert(mCurrentComic);
            mCurrentComic.setId(id);
        }
    }

    public void delete() {
        if (!isDownload() && !isHistory() && !isFavorite()) {
            mComicManager.delete(mCurrentComic);
            mCurrentComic.setId(null);
        }
    }

    public void update() {
        if (mCurrentComic.getId() != null) {
            mComicManager.update(mCurrentComic);
        }
    }

    public void updateOrInsert() {
        if (mCurrentComic.getId() != null) {
            mComicManager.update(mCurrentComic);
        } else {
            long id = mComicManager.insert(mCurrentComic);
            mCurrentComic.setId(id);
        }
    }

    public static SharedComic getInstance() {
        if (instance == null) {
            instance = new SharedComic();
        }
        return instance;
    }

}
